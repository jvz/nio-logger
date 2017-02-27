/*
 * Copyright 2017 Matt Sicker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.musigma.logging.layout;

import org.musigma.logging.impl.LogEvent;
import org.musigma.logging.util.Buffered;
import org.musigma.logging.util.CharSeq;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Date;

/**
 * Simple layout that encodes an event in the following format:
 * {@code [Sun Feb 26 12:46:54 CST 2017] Hello, world!\n}
 */
public class SimpleLayout implements Layout {

    private final Charset charset;
    private final ThreadLocal<CharsetEncoder> encoderLocal;
    private final ThreadLocal<CharBuffer> bufferLocal;

    public SimpleLayout(Charset charset) {
        this.charset = charset;
        this.encoderLocal = ThreadLocal.withInitial(charset::newEncoder);
        // using direct byte buffers to copy to mmap'd files is a lot faster!
        this.bufferLocal = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(8192).asCharBuffer());
    }

    @Override
    public ByteBuffer encode(LogEvent event) {
        CharSequence message = event.getMessage();
        String timestamp = new Date(event.getTimestamp()).toString();
        // "[" + timestamp + "] " + message + "\n"
        CharBuffer buf = CharBuffer.allocate(1 + timestamp.length() + 2 + message.length() + 1);
        buf.put('[').put(timestamp).put(']').put(' ');
        for (int i = 0; i < message.length(); i++) {
            buf.put(message.charAt(i));
        }
        buf.put('\n').flip();
        return charset.encode(buf);
    }

    @Override
    public void encode(LogEvent event, ByteBuffer dst) {
        CharsetEncoder encoder = encoderLocal.get();
        encoder.reset();
        CharSeq seq = new LogEventCharSeq(event);
        float expectedEncodedSize = seq.length() * encoder.averageBytesPerChar();
        int destinationSize = dst.remaining();
        if (destinationSize < expectedEncodedSize) {
            // TODO: a ByteBufferDestination pattern like in log4j could allow for buffering here
            throw new UnsupportedOperationException(
                "Cannot write to destination buffer as it's too small. Expected size: " + expectedEncodedSize + "; destination size: " + destinationSize);
        }

        CharBuffer buf = bufferLocal.get();
        // encode CharSeq in CharBuffer-sized chunks (too bad for those poor surrogate pairs)
        int cap = buf.capacity();
        int fullChunks = Math.floorDiv(seq.length(), cap);
        // first, we'll handle all the full-sized chunks
        for (int i = 0; i < fullChunks; i++) {
            int offset = i * cap;
            buf.clear();
            for (int j = 0; j < cap; j++) {
                buf.put(seq.charAt(j + offset));
            }
            buf.flip();
            // TODO: same note as above; we could drain the destination buffer and continue writing again
            if (encoder.encode(buf, dst, false).isOverflow()) {
                throw new UnsupportedOperationException(new BufferOverflowException());
            }
        }
        // encode last chunk
        int start = fullChunks * cap;
        int end = seq.length();
        buf.clear();
        for (int i = start; i < end; i++) {
            buf.put(seq.charAt(i));
        }
        buf.flip();
        if (encoder.encode(buf, dst, true).isOverflow()) {
            throw new BufferOverflowException();
        }
        if (encoder.flush(dst).isOverflow()) {
            throw new BufferOverflowException();
        }
    }

    @Override
    public void encode(LogEvent event, Buffered<ByteBuffer> destination) {
        throw new UnsupportedOperationException();
    }

    private static class LogEventCharSeq implements CharSeq {

        private final String timestamp;
        private final CharSequence message;

        private LogEventCharSeq(LogEvent event) {
            timestamp = new Date(event.getTimestamp()).toString();
            message = event.getMessage();
        }

        @Override
        public int length() {
            return 4 + timestamp.length() + message.length();
        }

        @Override
        public char charAt(int i) throws IndexOutOfBoundsException {
            if (i < 0) {
                throw new IndexOutOfBoundsException("Negative index: " + i);
            }
            if (i == 0) {
                return '[';
            }
            int startTimestamp = 1;
            int endTimestamp = startTimestamp + timestamp.length() - 1;
            if (i >= startTimestamp && i <= endTimestamp) {
                return timestamp.charAt(i - startTimestamp);
            }
            if (i == endTimestamp + 1) {
                return ']';
            }
            if (i == endTimestamp + 2) {
                return ' ';
            }
            int startMessage = endTimestamp + 3;
            int endMessage = startMessage + message.length() - 1;
            if (i >= startMessage && i <= endMessage) {
                return message.charAt(i - startMessage);
            }
            if (i == length() - 1) {
                return '\n';
            }
            throw new IndexOutOfBoundsException("Index too high: " + i + "; max: " + (length() - 1));
        }
    }

}

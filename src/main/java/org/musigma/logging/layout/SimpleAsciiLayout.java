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

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Simple layout using the same format as {@link SimpleLayout}, but this layout assumes all messages are encoded in
 * ASCII. This allows for more efficient character encoding. Invalid ASCII characters are mangled silently using the
 * Java rules of casting a {@code char} to a {@code byte}.
 */
public class SimpleAsciiLayout implements Layout {
    @Override
    public ByteBuffer encode(LogEvent event) {
        String timestamp = new Date(event.getTimestamp()).toString();
        CharSequence message = event.getMessage();
        ByteBuffer dst = ByteBuffer.allocate(1 + timestamp.length() + 2 + message.length() + 1);
        dst.put((byte) '[');
        for (int i = 0; i < timestamp.length(); i++) {
            dst.put((byte) timestamp.charAt(i));
        }
        dst.put((byte) ']').put((byte) ' ');
        for (int i = 0; i < message.length(); i++) {
            dst.put((byte) message.charAt(i));
        }
        dst.put((byte) '\n');
        dst.flip();
        return dst;
    }

    @Override
    public void encode(LogEvent event, ByteBuffer dst) {
        String timestamp = new Date(event.getTimestamp()).toString();
        CharSequence message = event.getMessage();
        if (4 + timestamp.length() + message.length() > dst.remaining()) {
            throw new BufferOverflowException();
        }
        dst.put((byte) '[');
        for (int i = 0; i < timestamp.length(); i++) {
            dst.put((byte) timestamp.charAt(i));
        }
        dst.put((byte) ']').put((byte) ' ');
        for (int i = 0; i < message.length(); i++) {
            dst.put((byte) message.charAt(i));
        }
        dst.put((byte) '\n');
    }

    @Override
    public void encode(LogEvent event, Buffered<ByteBuffer> destination) {
        // TODO: reusable StringBuilders (though using ThreadLocal may not work properly with AsyncFileChannelAppender)
        StringBuilder sb = new StringBuilder(32 + event.getMessage().length());
        // FIXME: while the layout can keep its own StringBuilders, why exactly does the LogEvent use it?
        event.formatTo(sb);
        int position = 0;
        int remaining = sb.length();
        while (remaining > 0) {
            ByteBuffer buf = destination.buffer();
            if (remaining > buf.remaining()) {
                buf = destination.drain();
            }
            int chunk = Math.min(remaining, buf.remaining());
            for (int i = 0; i < chunk; i++) {
                buf.put((byte) sb.charAt(position++));
            }
            remaining -= chunk;
        }
    }
}

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
package org.musigma.logging.appender;

import org.musigma.logging.impl.LogEvent;
import org.musigma.logging.layout.Layout;
import org.musigma.logging.util.Buffered;
import org.musigma.logging.util.Unsafe;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Appender using {@linkplain MappedByteBuffer memory mapped files}. The general code here is heavily adapted from
 * log4j-core's MemoryMappedFileManager.
 */
public class MemoryMappedFileAppender implements Appender, Buffered<ByteBuffer> {

    private final RandomAccessFile file;
    private final Layout layout;
    // length of mapped file region
    private final int capacity;
    // don't let the name fool you; we only update this position during remap() and close()
    private long position;
    private MappedByteBuffer buf;

    public MemoryMappedFileAppender(Path logFile, Layout layout, int capacity) {
        this.capacity = capacity;
        this.layout = layout;
        try {
            Files.deleteIfExists(logFile);
            this.file = new RandomAccessFile(logFile.toFile(), "rw");
            map(capacity);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void map(int length) throws IOException {
        FileChannel channel = file.getChannel();
        buf = channel.map(READ_WRITE, position, length);
        buf.order(ByteOrder.nativeOrder()); // TODO: not sure if this is needed?
    }

    private void remap() {
        position += buf.position();
        int length = capacity + buf.remaining();
        Unsafe.unmap(buf);
        buf = null;
        try {
            file.setLength(file.length() + capacity);
            map(length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void append(LogEvent event) {
        layout.encode(event, this);
    }

    @Override
    public synchronized void flush() throws IOException {
        buf.force();
    }

    @Override
    public void close() throws Exception {
        position += buf.position();
        try {
            flush();
            Unsafe.unmap(buf);
            file.setLength(position);
        } finally {
            file.close();
        }
    }

    @Override
    public ByteBuffer buffer() {
        return buf;
    }

    @Override
    public ByteBuffer drain() {
        remap();
        return buf;
    }
}

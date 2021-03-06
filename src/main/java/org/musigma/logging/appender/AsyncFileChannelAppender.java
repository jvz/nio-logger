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

import org.musigma.logging.layout.Layout;
import org.musigma.logging.impl.LogEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Appender based on {@link AsynchronousFileChannel}.
 */
public class AsyncFileChannelAppender implements Appender {

    private final AsynchronousFileChannel fileChannel;
    private final Layout layout;
    private final AtomicLong nextWritablePosition = new AtomicLong();
    private final Phaser phaser = new Phaser(1); // self is interested in phases to close when done

    public AsyncFileChannelAppender(Path logFile, Layout layout) {
        try {
            this.fileChannel = AsynchronousFileChannel.open(logFile, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            this.layout = layout;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void accept(LogEvent event) {
        ByteBuffer src = layout.encode(event);
        int remaining = src.remaining();
        long pos = nextWritablePosition.getAndAdd(remaining);
        new FileTransferAction(phaser, src, fileChannel, pos).run();
    }

    @Override
    public void flush() throws IOException {
        fileChannel.force(true);
    }

    @Override
    public void close() throws Exception {
        phaser.arriveAndAwaitAdvance();
        try {
            flush();
        } finally {
            fileChannel.close();
        }
    }
}

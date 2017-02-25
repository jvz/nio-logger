/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.musigma.logging;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class FileAppender implements Appender {

    private final AsynchronousFileChannel fileChannel;
    private final Layout layout;
    private final AtomicLong nextWritablePosition = new AtomicLong();
    private final Phaser phaser = new Phaser(1); // self is interested in phases to close when done

    public FileAppender(Path logFile, Layout layout) {
        try {
            this.fileChannel = AsynchronousFileChannel.open(logFile, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            this.layout = layout;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void append(LogEvent event) {
        ByteBuffer src = layout.encode(event);
        int remaining = src.remaining();
        long pos = nextWritablePosition.getAndAdd(remaining);
        new FileTransferAction(phaser, src, fileChannel, pos).run();
    }

    @Override
    public void close() throws Exception {
        phaser.arriveAndAwaitAdvance();
        try {
            fileChannel.force(false);
        } finally {
            fileChannel.close();
        }
    }

}

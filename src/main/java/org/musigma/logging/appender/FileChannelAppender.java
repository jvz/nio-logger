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
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

/**
 * Simple appender using {@link FileChannel}.
 */
public class FileChannelAppender implements Appender {

    private final FileChannel fileChannel;
    private final Layout layout;
//    private final ByteBuffer buf = ByteBuffer.allocateDirect(8192);

    public FileChannelAppender(Path logFile, Layout layout) {
        try {
            this.fileChannel = FileChannel.open(logFile, WRITE, CREATE, TRUNCATE_EXISTING);
            this.layout = layout;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void accept(LogEvent event) {
        ByteBuffer buf = layout.encode(event);
//        buf.clear();
//        layout.encode(event, buf);
        try {
            fileChannel.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        fileChannel.force(true);
    }

    @Override
    public void close() throws Exception {
        try {
            flush();
        } finally {
            fileChannel.close();
        }
    }
}

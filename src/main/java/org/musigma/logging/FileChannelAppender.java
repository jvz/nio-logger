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
package org.musigma.logging;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Simple appender using {@link FileChannel}.
 */
public class FileChannelAppender implements Appender {

    private final FileChannel fileChannel;
    private final Layout layout;

    public FileChannelAppender(Path logFile, Layout layout) {
        try {
            this.fileChannel = FileChannel.open(logFile, WRITE, CREATE, TRUNCATE_EXISTING);
            this.layout = layout;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void append(LogEvent event) {
        ByteBuffer buf = layout.encode(event);
        try {
            fileChannel.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        try {
            fileChannel.force(true);
        } finally {
            fileChannel.close();
        }
    }
}

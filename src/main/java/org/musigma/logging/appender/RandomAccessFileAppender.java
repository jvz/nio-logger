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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Simple appender using {@link RandomAccessFile}.
 */
public class RandomAccessFileAppender implements Appender {

    private final RandomAccessFile file;
    private final Layout layout;

    public RandomAccessFileAppender(Path logFile, Layout layout) {
        try {
            this.file = new RandomAccessFile(logFile.toFile(), "rw");
            this.layout = layout;
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void append(LogEvent event) {
        write(layout.encode(event));
    }

    private void write(ByteBuffer buf) {
        byte[] b = buf.array();
        try {
            file.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        file.getChannel().force(true);
    }

    @Override
    public void close() throws Exception {
        flush();
        file.close();
    }
}

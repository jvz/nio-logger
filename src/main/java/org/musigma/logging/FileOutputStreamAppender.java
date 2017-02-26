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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Simple appender based on {@link FileOutputStream} and {@link BufferedOutputStream}.
 */
public class FileOutputStreamAppender implements Appender {

    private final OutputStream out;
    private final Layout layout;

    public FileOutputStreamAppender(Path logFile, Layout layout) {
        try {
            this.out = new BufferedOutputStream(new FileOutputStream(logFile.toFile()));
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
            out.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        out.flush();
        out.close();
    }
}

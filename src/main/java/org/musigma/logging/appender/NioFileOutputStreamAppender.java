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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Simple appender using {@link Files#newOutputStream(Path, OpenOption...)} and {@link BufferedOutputStream}.
 */
public class NioFileOutputStreamAppender implements Appender {

    private final OutputStream out;
    private final Layout layout;
    // it's faster to use bulk write(byte[]) methods than to loop from our ByteBuffer
    private final byte[] writeBuffer = new byte[8192];
    private final ByteBuffer buf = ByteBuffer.allocateDirect(8192);

    public NioFileOutputStreamAppender(Path logFile, Layout layout) {
        try {
            this.out = new BufferedOutputStream(
                Files.newOutputStream(logFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING));
            this.layout = layout;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void accept(LogEvent event) {
        buf.clear();
        layout.encode(event, buf);
        buf.flip();
        // TODO: create a sort of BufferedOutputStream that takes write(ByteBuffer) methods
        int length = buf.remaining();
        buf.get(writeBuffer, 0, length);
        try {
            out.write(writeBuffer, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws Exception {
        flush();
        out.close();
    }
}

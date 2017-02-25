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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.Instant;

/**
 *
 */
public class SimpleLayout implements Layout {

    private final Charset charset;

    public SimpleLayout(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ByteBuffer encode(LogEvent event) {
        CharSequence message = event.getMessage();
        String timestamp = Instant.ofEpochMilli(event.getTimestamp()).toString();
        // "[" + timestamp + "] " + message + "\n"
        CharBuffer buf = CharBuffer.allocate(1 + timestamp.length() + 2 + message.length() + 1);
        buf.put('[').put(timestamp).put(']').put(' ');
        for (int i = 0; i < message.length(); i++) {
            buf.put(message.charAt(i));
        }
        buf.put('\n').flip();
        return charset.encode(buf);
    }
}

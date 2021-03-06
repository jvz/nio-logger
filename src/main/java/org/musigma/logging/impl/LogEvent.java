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
package org.musigma.logging.impl;

import org.musigma.logging.util.Formattable;

import java.util.Date;

/**
 * Any time a log message is created through a Logger, a LogEvent is created. This event is then handled by an Appender
 * which can encode the event via a Layout before writing to some underlying storage or network device.
 */
public class LogEvent implements Formattable {
    private final CharSequence message;
    private final long timestamp;
    // FIXME: is this even valid? we're not using it yet to be able to say
    private final ThreadLocal<StringBuilder> builderLocal = ThreadLocal.withInitial(StringBuilder::new);

    public LogEvent(CharSequence message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public CharSequence getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public CharSequence format() {
        String datetime = new Date(timestamp).toString();
        StringBuilder sb = builderLocal.get();
        sb.ensureCapacity(4 + datetime.length() + message.length());
        sb.append('[').append(datetime).append(']').append(' ').append(message).append('\n');
        return sb;
    }

    @Override
    public void formatTo(StringBuilder buf) {
        buf.append('[')
            .append(new Date(timestamp).toString()) // so it's not completely garbage-free; sue me
            .append(']')
            .append(' ')
            .append(message)
            .append('\n');
    }
}

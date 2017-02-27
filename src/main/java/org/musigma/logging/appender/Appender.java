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

import java.io.Flushable;

/**
 * An appender is used to append a log event to something. Generally, these will encode the log event into a
 * ByteBuffer which is written to something.
 */
public interface Appender extends AutoCloseable, Flushable {
    void append(LogEvent event);
}

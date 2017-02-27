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

import org.musigma.logging.Logger;
import org.musigma.logging.appender.Appender;

/**
 * Simple logger that writes messages into an {@link Appender}.
 */
public class SimpleLogger implements Logger {

    private final Appender appender;

    public SimpleLogger(Appender appender) {
        this.appender = appender;
    }

    @Override
    public void log(CharSequence msg) {
        appender.accept(new LogEvent(msg, System.currentTimeMillis()));
    }

}

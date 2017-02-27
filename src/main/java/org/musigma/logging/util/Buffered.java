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
package org.musigma.logging.util;

import java.nio.Buffer;

/**
 * Buffer abstraction to allow for buffered writing. Producers should obtain a buffer via {@link #buffer()} and continue
 * to write to it until its position hits its limit (i.e., {@link Buffer#remaining()} is 0). At this point, the producer
 * should call {@link #drain()} to drain the contents of the buffer and get a new buffer to continue writing to.
 */
public interface Buffered<T extends Buffer> {
    /**
     * Gets the buffer used for writing to this object.
     */
    T buffer();

    /**
     * Drains the contents of the underlying buffer and returns a (possibly different) buffer to continue writing to.
     */
    T drain();
}

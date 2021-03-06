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

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Provides access to JDK-internals that should really have a public API.
 */
public final class Unsafe {

    /**
     * Unmaps a {@link MappedByteBuffer}. Without using this, an mmap buffer will stay in memory until program exit!
     *
     * @param buf mmap buffer to unmap
     * @throws PrivilegedActionException if unable to access the Cleaner internals of MappedByteBuffer
     */
    public static void unmap(MappedByteBuffer buf) {
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
                Method getCleaner = buf.getClass().getMethod("cleaner");
                getCleaner.setAccessible(true);
                Object cleaner = getCleaner.invoke(buf);
                Method clean = cleaner.getClass().getMethod("clean");
                clean.invoke(cleaner);
                return null;
            });
        } catch (PrivilegedActionException e) {
            // well isn't that a shame? now we're going to leak memory
            e.printStackTrace();
        }
    }

    private Unsafe() {
    }
}

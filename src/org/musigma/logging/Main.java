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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Path logFile = Paths.get("test.log");
        Layout layout = new SimpleLayout(StandardCharsets.US_ASCII);
        try (Appender appender = new AsyncAppender(new FileAppender(logFile, layout))) {
            Logger logger = new SimpleLogger(appender);
            IntStream.range(0, 100000)
                .parallel()
                .forEach(i -> logger.log("Hello, world! Test message " + i));
        }
    }
}

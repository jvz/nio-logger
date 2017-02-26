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
package org.musigma.logging.jmh;

import org.musigma.logging.Appender;
import org.musigma.logging.AsyncFileChannelAppender;
import org.musigma.logging.Layout;
import org.musigma.logging.Logger;
import org.musigma.logging.SimpleAsciiLayout;
import org.musigma.logging.SimpleLogger;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
@State(Scope.Benchmark)
public class AsyncFileChannelAppenderBenchmark {

    private Logger logger;
    private Appender appender;

    @Setup(Level.Iteration)
    public void setup() {
        Path logFile = Paths.get("target", "test.log");
//        Layout layout = new SimpleLayout(StandardCharsets.ISO_8859_1);
        Layout layout = new SimpleAsciiLayout();
        appender = new AsyncFileChannelAppender(logFile, layout);
        logger = new SimpleLogger(appender);
    }

    @TearDown(Level.Iteration)
    public void teardown() throws Exception {
        appender.close();
    }

    @Benchmark
    public void logToFile() {
        logger.log("Test message");
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(AsyncFileChannelAppenderBenchmark.class.getName())
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(1)
            .build();

        new Runner(options).run();
    }

}

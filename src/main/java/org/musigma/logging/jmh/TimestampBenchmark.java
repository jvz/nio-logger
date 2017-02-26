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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Instant;
import java.util.Date;

/**
 * Benchmarks various ways of converting a millis timestamp into a datetime string.
 */
@State(Scope.Benchmark)
public class TimestampBenchmark {

    private long timestamp = System.currentTimeMillis();

    @Benchmark
    public String viaInstantToString() {
        return Instant.ofEpochMilli(timestamp).toString();
    }

    @Benchmark
    public String viaDateToString() {
        return new Date(timestamp).toString();
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder()
            .include(TimestampBenchmark.class.getName())
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(1)
            .build()).run();
    }
}

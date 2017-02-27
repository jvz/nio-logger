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

import org.musigma.logging.layout.Layout;
import org.musigma.logging.impl.LogEvent;
import org.musigma.logging.layout.SimpleAsciiLayout;
import org.musigma.logging.layout.SimpleLayout;
import org.musigma.logging.util.Buffered;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 *
 */
@State(Scope.Benchmark)
public class LayoutBenchmark {

    private Layout simpleLayout = new SimpleLayout(StandardCharsets.ISO_8859_1);
    private Layout asciiLayout = new SimpleAsciiLayout();
    private LogEvent event = new LogEvent("Test message", System.currentTimeMillis());

    @State(Scope.Thread)
    public static class ReusableBuffer {
        private ByteBuffer buf = ByteBuffer.allocateDirect(8192);
    }

    @State(Scope.Thread)
    public static class ExtensibleBuffer implements Buffered<ByteBuffer> {
        private ByteBuffer buf = ByteBuffer.allocateDirect(8192);

        @Override
        public ByteBuffer buffer() {
            return buf;
        }

        @Override
        public ByteBuffer drain() {
            buf.clear(); // nothing to drain to!
            return buf;
        }
    }

    @Benchmark
    public ByteBuffer simpleLayoutEncode1() {
        return simpleLayout.encode(event);
    }

    @Benchmark
    public void simpleLayoutEncode2(ReusableBuffer buffer) {
        simpleLayout.encode(event, buffer.buf);
        buffer.buf.clear();
    }

    @Benchmark
    public ByteBuffer asciiLayoutEncode1() {
        return asciiLayout.encode(event);
    }

    @Benchmark
    public void asciiLayoutEncode2(ReusableBuffer buffer) {
        asciiLayout.encode(event, buffer.buf);
        buffer.buf.clear();
    }

    @Benchmark
    public void asciiLayoutEncode3(ExtensibleBuffer buffer) {
        asciiLayout.encode(event, buffer);
        buffer.drain();
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(LayoutBenchmark.class.getName())
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(1)
            .build();

        new Runner(options).run();
    }
}

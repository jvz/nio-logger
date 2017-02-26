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
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Compares the performance of copying from different types of ByteBuffers into a byte array.
 */
@State(Scope.Benchmark)
public class ByteBufferBenchmark {

    private static final int BUFSIZ = 8192;
    private ByteBuffer heap = ByteBuffer.allocate(BUFSIZ);
    private ByteBuffer direct = ByteBuffer.allocateDirect(BUFSIZ);
    private byte[] buf = new byte[BUFSIZ];

    @Setup
    public void setup() {
        heap.clear();
        direct.clear();
        Random r = new Random();
        for (int i = 0; i < BUFSIZ; i++) {
            byte b = (byte) r.nextInt(256);
            heap.put(b);
            direct.put(b);
        }
    }

    @Benchmark
    public ByteBuffer copyViaHeap() {
        heap.rewind();
        return heap.get(buf, 0, BUFSIZ);
    }

    @Benchmark
    public ByteBuffer copyDirectly() {
        direct.rewind();
        return direct.get(buf, 0, BUFSIZ);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(ByteBufferBenchmark.class.getName())
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(1)
            .build();

        new Runner(options).run();
    }
}

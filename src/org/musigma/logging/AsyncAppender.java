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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 *
 */
public class AsyncAppender implements Appender {

    private final TransferQueue<LogEvent> queue = new LinkedTransferQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Future<?> consumer;

    public AsyncAppender(Appender delegate) {
        consumer = executorService.submit(new LogEventConsumer(delegate, queue));
    }

    @Override
    public void append(LogEvent event) {
        try {
            queue.transfer(event);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws Exception {
        consumer.cancel(true);
        executorService.shutdown();
    }

    private static class LogEventConsumer implements Runnable {

        private final Appender delegate;
        private final TransferQueue<LogEvent> queue;

        private LogEventConsumer(Appender delegate, TransferQueue<LogEvent> queue) {
            this.delegate = delegate;
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    LogEvent event = queue.take();
                    delegate.append(event);
                } catch (InterruptedException ie) {
                    try {
                        delegate.close();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    } finally {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}

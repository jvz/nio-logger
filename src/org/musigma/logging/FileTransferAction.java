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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.util.concurrent.Phaser;

/**
 *
 */
class FileTransferAction implements Runnable, CompletionHandler<Integer, Void> {
    private final Phaser phaser;
    private final ByteBuffer src;
    private final AsynchronousFileChannel dst;

    private long pos;
    private FileLock lock;

    FileTransferAction(Phaser phaser, ByteBuffer src, AsynchronousFileChannel dst, long pos) {
        this.phaser = phaser;
        this.src = src;
        this.dst = dst;
        this.pos = pos;
    }

    @Override
    public void run() {
        phaser.register();
        dst.lock(pos, src.remaining(), false, this, LockHandler.INSTANCE);
    }

    private FileTransferAction lock(FileLock lock) {
        this.lock = lock;
        return this;
    }

    private void transfer() {
        if (src.hasRemaining()) {
            dst.write(src, pos, null, this);
        } else {
            src.rewind();
            unlock();
        }
    }

    private void unlock() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    @Override
    public void completed(Integer result, Void attachment) {
        pos += result;
        transfer();
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
        exc.printStackTrace();
        unlock();
    }

    private enum LockHandler implements CompletionHandler<FileLock, FileTransferAction> {
        INSTANCE;

        @Override
        public void completed(FileLock lock, FileTransferAction channel) {
            channel.lock(lock).transfer();
        }

        @Override
        public void failed(Throwable exc, FileTransferAction channel) {
            exc.printStackTrace();
            channel.unlock();
        }
    }
}

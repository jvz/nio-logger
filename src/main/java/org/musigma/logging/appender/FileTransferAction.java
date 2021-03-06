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
package org.musigma.logging.appender;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Phaser;

/**
 *
 */
class FileTransferAction implements Runnable, CompletionHandler<Integer, Void> {
    private final Phaser phaser;
    private final ByteBuffer src;
    private final AsynchronousFileChannel dst;

    private long pos;

    FileTransferAction(Phaser phaser, ByteBuffer src, AsynchronousFileChannel dst, long pos) {
        this.phaser = phaser;
        this.src = src;
        this.dst = dst;
        this.pos = pos;
    }

    @Override
    public void run() {
        phaser.register();
        transfer();
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
        phaser.arriveAndDeregister();
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

}

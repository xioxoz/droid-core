/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;

public class LooperTest {

    @Test
    public void testCantPrepareTwice() throws InterruptedException {
        final BlockingQueue<Boolean> failed = new ArrayBlockingQueue<>(1);

        Thread thread = new Thread(() -> {
            try {
                Looper.prepare();
                try {
                    Looper.prepare();
                    failed.put(true);

                } catch (IllegalStateException ise) {
                    failed.put(false);
                }

            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });

        thread.start();
        assertFalse(failed.take());
        thread.join();
    }

    @Test
    public void testLooperCreation() throws InterruptedException {
        final BlockingQueue<Boolean> failed = new ArrayBlockingQueue<>(1);

        Thread thread = new Thread(() -> {
            try {
                if (Looper.myLooper() != null) {
                    failed.put(true);
                }
                Looper.prepare();
                if (Looper.myLooper() == null) {
                    failed.put(true);
                }
                failed.put(false);
            } catch (InterruptedException ie) {
                throw new IllegalStateException(ie);
            }
        });

        thread.start();
        assertFalse(failed.take());
        thread.join();
    }

    @Test
    public void testLooperQuit() throws InterruptedException {
        HandlerThread thread = new HandlerThread("tests");
        thread.start();
        thread.getLooper().quit();
        thread.join();
    }
}

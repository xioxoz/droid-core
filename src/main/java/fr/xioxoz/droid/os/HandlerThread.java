/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

/**
 * A Thread implementation that runs a Looper.
 * The embedded looper can be used to run Handler(s) on it.
 */
public class HandlerThread extends Thread {

    /**
     * The Looper that will run on this thread.
     */
    private Looper looper;

    public HandlerThread(String name) {
        super(name);
    }

    /**
     * Provides the Looper associated to this thread. Note: may block while
     * preparing the looper.
     * @return the looper or null if the thread is not started.
     */
    public Looper getLooper() {
        if (!isAlive()) {
            return null;
        }

        boolean wasInterrupted = false;

        // If the thread has been started, wait until the looper has been created.
        synchronized (this) {
            while (isAlive() && looper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    wasInterrupted = true;
                }
            }
        }

        // We may need to restore the thread's interrupted flag, because it may
        // have been cleared above since we eat InterruptedExceptions.
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        }

        return looper;
    }

    /**
     * Asks the thread to quit by terminating its Looper.
     */
    public void quit() {
        Looper l = getLooper();
        if (l != null) {
            l.quit();
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (this) {
            looper = Looper.myLooper();
            notifyAll();
        }
        Looper.loop();
    }
}

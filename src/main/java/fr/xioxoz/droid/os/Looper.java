/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

public class Looper {

    /**
     * Thread local instance of a Looper. There's only one Looper instance
     * per thread.
     */
    private static ThreadLocal<Looper> localLooper = new ThreadLocal<>();

    /**
     * Queue that holds the messages we will process in the Looper.
     */
    private final MessageQueue queue;

    private Looper() {
        queue = new MessageQueue();
    }

    public MessageQueue getQueue() {
        return queue;
    }

    public void quit() {
        queue.quit();
    }

    public static void prepare() {
        if (localLooper.get() != null) {
            throw new IllegalStateException("only one Looper may be created per thread");
        }
        localLooper.set(new Looper());
    }

    static Looper myLooper() {
        return localLooper.get();
    }

    public static void loop() {
        final Looper looper = myLooper();
        final MessageQueue queue = looper.getQueue();

        for(;;) {
            // Get the next available message, might be blocking.
            Message m = queue.next();
            if (m == null) {
                // We're quitting.
                return;
            }

            m.target.dispatchMessage(m);
        }
    }
}

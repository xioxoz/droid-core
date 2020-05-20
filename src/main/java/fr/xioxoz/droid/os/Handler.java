/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

public class Handler {

    /**
     * The Looper (thread) behind this handler.
     */
    private final Looper looper;

    /**
     * Callback interface you can provide when creating a Handler to avoid
     * extending it.
     */
    public interface Callback {
        /**
         * Called when a message is available for this handler.
         * @param m the message to process.
         * @return true when no further handling is desired.
         */
        boolean handleMessage(Message m);
    }

    /**
     * Callback that may be called instead or with handleMessage() to process
     * an incoming message.
     */
    private final Callback callback;

    // Only used for testing.
    Handler() {
        this(null, null);
    }

    public Handler(Looper l) {
        this(l, null);
    }

    public Handler(Looper l, Callback cb) {
        this.looper = l;
        this.callback = cb;
    }

    void dispatchMessage(Message m) {
        if (callback != null) {
            boolean handled = callback.handleMessage(m);
            if (handled) {
                return;
            }
        }
        handleMessage(m);
    }

    void handleMessage(Message m) {}

    public Message obtainMessage() {
        return Message.obtain()
                .withTarget(this);
    }

    public Message obtainMessage(int what) {
        return Message.obtain()
                .withTarget(this)
                .withWhat(what);
    }

    public final void sendMessage(Message m) {
        sendMessageDelayed(m, 0l);
    }

    public final void sendMessageDelayed(Message m, long delay) {
        sendMessageAtTime(m, System.currentTimeMillis()+delay);
    }

    public final void sendMessageAtTime(Message m, long time) {
        if (time < 0) {
            throw new IllegalArgumentException("can't send message with negative time");
        }

        MessageQueue q = looper.getQueue();
        if (q == null) {
            throw new IllegalStateException("posting Message on Looper with null queue");
        }

        q.enqueueMessage(m, time);
    }
}

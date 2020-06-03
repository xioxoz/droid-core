/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

/**
 * Defines a message containing a description and arbitrary data object that can be
 * sent to a {@link Handler}.
 */
public class Message<T> {
    /**
     * User-defined message code so that the recipient can identify
     * what this message is about. Each {@link Handler} has its own name-space
     * for message codes, so you do not need to worry about yours conflicting
     * with other handlers.
     */
    public int what;

    /**
     * The user payload the message is carrying.
     */
    public T payload;

    /**
     * Handler this message must be delivered to.
     */
    Handler target;

    /**
     * Date when the message must be delivered.
     */
    long when;

    public Message withWhat(int what) {
        this.what = what;
        return this;
    }

    public Message withPayload(T payload) {
        this.payload = payload;
        return this;
    }

    Message withTarget(Handler target) {
        this.target = target;
        return this;
    }

    Message withWhen(long when) {
        this.when = when;
        return this;
    }

    @Override
    public String toString() {
        return "Message{" +
                "what=" + what +
                ", payload=" + payload +
                ", target=" + target +
                ", when=" + when +
                '}';
    }

    private Message() {}

    public static Message obtain() {
        return new Message();
    }

    public static Message copyFrom(Message m) {
        return obtain()
                .withWhat(m.what)
                .withTarget(m.target)
                .withPayload(m.payload)
                .withWhen(m.when);
    }
}

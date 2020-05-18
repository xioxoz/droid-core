/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

public class Message {
    /**
     * User-defined message code so that the recipient can identify
     * what this message is about. Each {@link Handler} has its own name-space
     * for message codes, so you do not need to worry about yours conflicting
     * with other handlers.
     */
    public int what;

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

    Message withTarget(Handler target) {
        this.target = target;
        return this;
    }

    private Message() {}

    public static Message aMessage() {
        return new Message();
    }
}

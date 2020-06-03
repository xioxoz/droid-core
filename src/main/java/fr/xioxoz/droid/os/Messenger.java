/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

/**
 * A Messenger describes a component designed to receive Message objects and
 * handle them according to timing criteria.
 */
public interface Messenger {
    /**
     * Allocates or fetches a Message instance from a pool.
     * The delivered Message is ready to be used.
     * @return a fresh Message instance.
     */
    Message obtainMessage();

    /**
     * Allocates or fetches a Message instance from a pool and sets the Message
     * subject to <code>what</code> value.
     * @param what the Message subject.
     * @return a fresh Message instance with the defined what.
     */
    Message obtainMessage(int what);

    /**
     * Sends the given Message to the Messenger for delivery.
     * @param m the Message to send.
     */
    void sendMessage(Message m);

    /**
     * Sends the given Message to the Messenger for delivery in
     * <code>delay</code> milliseconds.
     * @param m the Message to deliver.
     * @param delay the delay after which the Message has to be delivered.
     */
    void sendMessageDelayed(Message m, long delay);

    /**
     * Sends the given Message to the Messenger but ensure it will be the next
     * to be delivered.
     * @param m the Message to deliver.
     */
    void sendMessageAtFrontOfQueue(Message m);

    /**
     * Sends the given Message for delivery at the specified <code>time</code>.
     * @param m the Message to deliver.
     * @param time the delivery time, expressed in milliseconds.
     */
    void sendMessageAtTime(Message m, long time);
}

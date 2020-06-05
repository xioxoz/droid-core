/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

import java.util.LinkedList;

/**
 * A MessageQueue is queue of Messages ordered by their "when" timestamp.
 * The interface/implementation is inspired from the android.os.MessageQueue
 * provided by Android Open Source Project.
 */
class MessageQueue {

    /**
     * The list of messages currently in the queue.
     */
    private final LinkedList<Message> messages = new LinkedList<>();

    /**
     * True when the queue is quitting.
     */
    private boolean quitting;

    MessageQueue() {}

    /**
     * Tells if a message is immediately available in the queue.
     * Returns true when the queue will immediately return a
     * Message if "next()" is called.
     */
    synchronized boolean isIdle() {
        final long now = System.currentTimeMillis();
        return messages.isEmpty() || now < messages.getFirst().when;
    }

    /**
     * Provides the next available message in the queue. As messages are
     * ordered and delivered according to their "when" value, the call may
     * block until the next message is available.
     */
    synchronized Message next() {
        long nextWaitingTime = 0L;

        for (;;) {
            // Wait for a message to be available.
            waitForMessagesLocked(nextWaitingTime);

            final long now = System.currentTimeMillis();
            if (messages.isEmpty()) {
                // No message, we have to wait until a new one is inserted.
                nextWaitingTime = -1L;

            } else if (now < messages.getFirst().when) {
                // The next message is not scheduled for now, wait.
                nextWaitingTime = Math.min(messages.getFirst().when - now, Long.MAX_VALUE);

            } else {
                // There's a message, deliver it.
                return messages.removeFirst();
            }

            // The queue is quitting and we have no more messages to deliver.
            if (nextWaitingTime == -1L && quitting) {
                return null;
            }
        }
    }

    /**
     * Wait for a period of time provided by the <time> parameter.
     * It blocks the following ways:
     *  - if time > 0, it waits for time ms,
     *  - if time == 0, it doesn't wait,
     *  - if time < 0, it waits until until someone notifies.
     */
    private void waitForMessagesLocked(long time) {
        try {
            if (time == 0) {
                return;

            } else if (time > 0) {
                wait(time);

            } else {
                wait();
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Adds a message <m> to the queue using <when> to insert the message at
     * the right place in the queue.
     */
    boolean enqueueMessage(Message m, long when) {
        if (m.target == null) {
            throw new IllegalArgumentException("can't enqueue Message without target Handler");
        }

        synchronized (this) {
            if (quitting) {
                // The queue is in quitting state, we won't accept new messages
                // and wait for the looper to empty us.
                return false;
            }

            m.when = when;
            if (when == 0 || messages.isEmpty() || when < messages.getFirst().when) {
                messages.addFirst(m);

            } else if (when >= messages.getLast().when) {
                messages.addLast(m);

            } else {
                // Insert the element at the right position in the list.
                int pos = 0;
                for (Message cur : messages) {
                    if (cur.when > when) {
                        break;
                    }
                    pos++;
                }
                messages.add(pos, m);
            }
            notify();
        }

        return true;
    }

    /**
     * Tells if a message described by the target <h> and the subject <what>
     * in the queue.
     */
    synchronized boolean hasMessages(Handler h, int what) {
        if (h == null) {
            return false;
        }

        synchronized (this) {
            for (Message m : messages) {
                if (m.target == h && m.what == what) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tells if a message described by the target <h> in the queue.
     */
    synchronized boolean hasMessages(Handler h) {
        if (h == null) {
            return false;
        }

        for (Message m : messages) {
            if (m.target == h) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes the messages described by the target <h> and the subject <what>
     * from the queue.
     */
    synchronized void removeMessages(Handler h, int what) {
        if (h == null) {
            throw new IllegalArgumentException("can't remove Message(s) without target handler");
        }

        messages.removeIf(m -> m.target == h && m.what == what);
    }

    synchronized void quit() {
        quitting = true;
        notifyAll();
    }
}

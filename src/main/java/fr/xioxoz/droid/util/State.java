/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.util;

import fr.xioxoz.droid.os.Message;

/**
 * State is the skeleton class for the StateMachine states implementation.
 */
public class State {
    /**
     * Called when the state machine is entering this state.
     */
    public void enter() {}

    /**
     * Called when the state is exiting this state.
     */
    public void exit() {}

    /**
     * Let this state handle a message.
     * @param m the Message to process.
     * @return true if the message has been handled, or false if we expect a
     *         parent state to process it.
     */
    public boolean processMessage(Message m) {
        // Default implementation: the message is ignored, let the parent (or
        // nobody) process it.
        return false;
    }
}

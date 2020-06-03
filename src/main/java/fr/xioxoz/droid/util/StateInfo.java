/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.util;

/**
 * A StateInfo describes a State in the state machine hierarchy.
 */
class StateInfo {

    /**
     * The state this info is related to.
     */
    final State state;

    /**
     * The parent of the state in the state machine hierarchy.
     */
    StateInfo parent;

    /**
     * Tells if the state is under execution now.
     */
    boolean active;

    State getState() {
        return state;
    }

    StateInfo getParent() {
        return parent;
    }

    boolean isActive() {
        return active;
    }

    StateInfo withParent(StateInfo parent) {
        this.parent = parent;
        return this;
    }

    StateInfo withActive(boolean active) {
        this.active = active;
        return this;
    }

    private StateInfo(State state) {
        this.state = state;
    }

    static StateInfo anInfoFor(State state) {
        return new StateInfo(state);
    }
}

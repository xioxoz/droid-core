/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.util;

import fr.xioxoz.droid.os.Handler;
import fr.xioxoz.droid.os.Looper;
import fr.xioxoz.droid.os.Message;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

class StateMachineHandler extends Handler {

    // Ask the handler to initialize the state machine.
    static final int CMD_INIT_STATE_MACHINE = -1;

    // Association between the States and the StateInfos that describe the hierarchy.
    private final Map<State, StateInfo> nodes = new HashMap<>();

    // The list of messages that will be replayed when the next state will be reached.
    private final Deque<Message> deferredMessages = new ArrayDeque<>();

    // Initial (starting) state of the machine.
    private StateInfo start;

    // State the machine is currently in.
    private StateInfo current;

    // State to reach when performing the transition. When next is not null,
    // it means we have a transition to perform.
    private StateInfo next;

    StateMachineHandler(Looper l) {
        super(l);
    }

    synchronized void addState(State state, State parent) {
        if (nodes.containsKey(state)) {
            throw new IllegalStateException("state already added");
        }

        StateInfo parentInfo = nodes.getOrDefault(parent, null);
        StateInfo info = StateInfo.anInfoFor(state)
                .withParent(parentInfo)
                .withActive(false);
        nodes.put(state, info);
    }

    synchronized void setInitialState(State state) {
        if (!nodes.containsKey(state)) {
            throw new IllegalStateException("state not added");
        }

        start = nodes.get(state);
    }

    synchronized void transitionTo(State state) {
        if (!nodes.containsKey(state)) {
            throw new IllegalStateException("state not added");
        }

        next = nodes.get(state);
    }

    synchronized void deferMessage(Message m) {
        deferredMessages.push(m);
    }

    @Override
    protected void handleMessage(Message m) {
        switch (m.what) {
            case CMD_INIT_STATE_MACHINE:
                initStateMachine();
                break;

            default:
                processWithStateMachine(m);
                break;
        }
    }

    private synchronized void initStateMachine() {
        // Prepare a transition to the initial state.
        next = start;
        Deque<StateInfo> stack = getNextToAncestorStackLocked();

        // Enter all the states of the new branch.
        while (stack.size() > 0) {
            current = stack.pop();
            current.active = true;
            current.state.enter();
        }
        next = null;
    }

    private synchronized void processWithStateMachine(Message m) {
        // Dispatch the message to states
        dispatchMessageLocked(m);

        // Perform transitions
        if (next != null) {
            while (current != next) {
                performTransitionLocked();
            }
            next = null;

            // Put deferred messages at the front of queue to ensure they will be replayed before the others.
            while (deferredMessages.size() > 0) {
                sendMessageAtFrontOfQueue(deferredMessages.pop());
            }
        }
    }

    private void dispatchMessageLocked(Message m) {
        StateInfo receiver = current;
        while (receiver != null) {
            boolean processed = receiver.state.processMessage(m);
            if (processed) {
                // The state correctly processed the message, no need to
                // dispatch to parent states.
                break;
            }
            receiver = receiver.parent;
        }
    }

    private void performTransitionLocked() {
        Deque<StateInfo> nextStack = getNextToAncestorStackLocked();
        StateInfo commonAncestor = null;
        if (nextStack.size() > 0) {
            commonAncestor = nextStack.peek().parent;
        }

        // Leave all the states from this branch.
        while (current != commonAncestor) {
            current.state.exit();
            current.active = false;
            current = current.parent;
        }

        // Enter all the states of the new branch.
        while (nextStack.size() > 0) {
            current = nextStack.pop();
            current.active = true;
            current.state.enter();
        }
    }

    private Deque<StateInfo> getNextToAncestorStackLocked() {
        Deque<StateInfo> stack = new ArrayDeque<>();
        for (StateInfo si = next; si != null && !si.active; si = si.parent) {
            stack.push(si);
        }
        return stack;
    }
}

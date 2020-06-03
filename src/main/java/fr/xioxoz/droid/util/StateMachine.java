/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.util;

import fr.xioxoz.droid.os.Looper;
import fr.xioxoz.droid.os.Message;
import fr.xioxoz.droid.os.Messenger;

/**
 * <p>StateMachine is a hierarchical state machine which processes messages
 * and can have states arranged hierarchically.</p>
 *
 * <p>A state is a <code>State</code> object and must implement
 * <code>processMessage</code> and optionally <code>enter/exit</code>.
 * The enter/exit methods are equivalent to the construction and destruction
 * in Object Oriented programming and are used to perform initialization and
 * cleanup of the state respectively.</p>
 *
 * <p>When a state machine is created, <code>addState</code> is used to build the
 * hierarchy and <code>setInitialState</code> is used to identify which of these
 * is the initial state. After construction the programmer calls <code>start</code>
 * which initializes and starts the state machine. The first action the StateMachine
 * is to the invoke <code>enter</code> for all of the initial state's hierarchy,
 * starting at its eldest parent. The calls to enter will be done in the context
 * of the StateMachine's Handler, not in the context of the call to start, and they
 * will be invoked before any messages are processed.</p>
 *
 * <p>After the state machine is created and started, messages are sent to a state
 * machine using <code>sendMessage</code> and the messages are created using
 * <code>obtainMessage</code>. When the state machine receives a message the
 * current state's <code>processMessage</code> is invoked.</p>
 *
 * <p>Each state in the state machine may have a zero or one parent states. If
 * a child state is unable to handle a message it may have the message processed
 * by its parent by returning false.</p>
 *
 * <p>If it is desirable to completely stop the state machine call <code>quit</code> or
 * <code>quitNow</code>. These will call <code>exit</code> of the current state and its parents
 * and then exit Thread/Loopers.</p>
 *
 * <p>In addition to <code>processMessage</code> each <code>State</code> has
 * an <code>enter</code> method and <code>exit</code> method which may be overridden.</p>
 *
 * <p>Since the states are arranged in a hierarchy transitioning to a new state
 * causes current states to be exited and new states to be entered. To determine
 * the list of states to be entered/exited the common parent closest to
 * the current state is found. We then exit from the current state and its
 * parent's up to but not including the common parent state and then enter all
 * of the new states below the common parent down to the destination state.
 * If there is no common parent all states are exited and then the new states
 * are entered.</p>
 *
 * <p>Two other methods that states can use are <code>deferMessage</code> and
 * <code>sendMessageAtFrontOfQueue</code>. The <code>sendMessageAtFrontOfQueue</code> sends
 * a message but places it on the front of the queue rather than the back. The
 * <code>deferMessage</code> causes the message to be saved on a list until a
 * transition is made to a new state. At which time all of the deferred messages
 * will be put on the front of the state machine queue with the oldest message
 * at the front. These will then be processed by the new current state before
 * any other messages that are on the queue or might be added later.</p>
 */
public class StateMachine implements Messenger {

    // Internal State Machine handler that will receive and process the messages.
    private final StateMachineHandler handler;

    /**
     * Creates a new state machine running on the provided Looper.
     * @param looper the thread to use.
     */
    public StateMachine(Looper looper) {
        handler = new StateMachineHandler(looper);
    }

    /**
     * Adds a state to the state machine. The state has no parent and is
     * considered as a hierarchy root. A state can only be added once to the
     * hierarchy.
     * @param state the state to add.
     */
    public final void addState(State state) {
        if (state == null) {
            throw new NullPointerException("cannot add null state");
        }
        handler.addState(state, null);
    }

    /**
     * Adds a state to the state machine as a child of the given parent.
     * A state can only be added once to the hierarchy.
     * @param state the state to add.
     * @param parent the parent state.
     */
    public final void addState(State state, State parent) {
        if (state == null || parent == null) {
            throw new NullPointerException("cannot add null state");
        }
        handler.addState(state, parent);
    }

    /**
     * Defines the initial state of the machine.
     * The state must have been added before.
     * @param state the initial state.
     */
    public final void setInitialState(State state) {
        if (state == null) {
            throw new NullPointerException("initial state cannot be null");
        }
        handler.setInitialState(state);
    }

    /**
     * Starts (initialize) the state machine.
     */
    public void start() {
        handler.sendMessage(handler.obtainMessage(StateMachineHandler.CMD_INIT_STATE_MACHINE));
    }

    /**
     * Tells the machine the state to reach before handling any new message.
     * @param state the state to transition to.
     */
    protected final void transitionTo(State state) {
        if (state == null) {
            throw new NullPointerException("cannot transition to null state");
        }
        handler.transitionTo(state);
    }

    /**
     * Defers a Message to be handled after the next transition.
     * @param m the Message to defer.
     */
    protected final void deferMessage(Message m) {
        if (m == null) {
            throw new NullPointerException("cannot defer null message");
        }
        handler.deferMessage(m);
    }

    @Override
    public final Message obtainMessage() {
        return handler.obtainMessage();
    }

    @Override
    public final Message obtainMessage(int what) {
        return handler.obtainMessage(what);
    }

    @Override
    public final void sendMessage(Message m) {
        if (m == null) {
            throw new NullPointerException("cannot send null message");
        }
        handler.sendMessage(m);
    }

    @Override
    public void sendMessageDelayed(Message m, long delay) {
        if (m == null) {
            throw new NullPointerException("cannot send null message");
        }
        if (delay < 0l) {
            throw new IllegalArgumentException("cannot send message in the past");
        }
        handler.sendMessageDelayed(m, delay);
    }

    @Override
    public void sendMessageAtFrontOfQueue(Message m) {
        if (m == null) {
            throw new NullPointerException("cannot send null message");
        }
        handler.sendMessageAtFrontOfQueue(m);
    }

    @Override
    public void sendMessageAtTime(Message m, long time) {
        if (m == null) {
            throw new NullPointerException("cannot send null message");
        }
        if (time < 0l) {
            throw new IllegalArgumentException("invalid time '" + time + "'");
        }
        handler.sendMessageAtTime(m, time);
    }

    public final void sendMessage(int what) {
        handler.sendMessage(handler.obtainMessage(what));
    }
}

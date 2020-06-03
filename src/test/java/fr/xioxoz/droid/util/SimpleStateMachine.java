/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.util;

import fr.xioxoz.droid.os.Looper;
import fr.xioxoz.droid.os.Message;

import java.util.Queue;

public class SimpleStateMachine extends StateMachine {

    public enum Result {
        ENTER_STATE1,
        HANDLE_STATE1,
        EXIT_STATE1,
        ENTER_STATE2,
        HANDLE_STATE2,
        EXIT_STATE2,
    }

    public static final int MOVE_TO_STATE1 = 0;
    public static final int MOVE_TO_STATE2 = 1;

    private class State1 extends State {
        @Override
        public void enter() {
            receiver.add(Result.ENTER_STATE1);
        }

        @Override
        public void exit() {
            receiver.add(Result.EXIT_STATE1);
        }

        @Override
        public boolean processMessage(Message m) {
            if (m.what == MOVE_TO_STATE2) {
                transitionTo(state2);
            }
            return receiver.add(Result.HANDLE_STATE1);
        }
    }

    private class State2 extends State {
        @Override
        public void enter() {
            receiver.add(Result.ENTER_STATE2);
        }

        @Override
        public void exit() {
            receiver.add(Result.EXIT_STATE2);
        }

        @Override
        public boolean processMessage(Message m) {
            if (m.what == MOVE_TO_STATE1) {
                transitionTo(state1);
            }
            return receiver.add(Result.HANDLE_STATE2);
        }
    }

    private final Queue<Result> receiver;
    private final State1 state1 = new State1();
    private final State2 state2 = new State2();

    public SimpleStateMachine(Looper looper, Queue<Result> listener) {
        super(looper);
        receiver = listener;
    }

    public void start() {
        addState(state1);
        addState(state2);
        setInitialState(state1);
        super.start();
    }
}

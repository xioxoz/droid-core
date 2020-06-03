/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.util;

import fr.xioxoz.droid.os.Looper;
import fr.xioxoz.droid.os.Message;

import java.util.Queue;

import static fr.xioxoz.droid.util.HierarchicalStateMachine.Result.*;

public class HierarchicalStateMachine extends StateMachine {

    public enum Result {
        ENTER_A,
        EXIT_A,
        HANDLE_A,
        ENTER_A1,
        EXIT_A1,
        HANDLE_A1,
        ENTER_A2,
        EXIT_A2,
        HANDLE_A2,
        ENTER_B,
        EXIT_B,
        HANDLE_B,
        ENTER_B1,
        EXIT_B1,
        HANDLE_B1
    }

    private static final int MSG_TRANSITION_TO = 0;

    private class TestState extends State {

        private final Result enter;
        private final Result exit;
        private final Result handle;
        private final boolean handled;

        private TestState(Result enter, Result exit, Result handle, boolean handled) {
            this.enter = enter;
            this.exit = exit;
            this.handle = handle;
            this.handled = handled;
        }

        @Override
        public void enter() {
            receiver.add(enter);
        }

        @Override
        public void exit() {
            receiver.add(exit);
        }

        @Override
        public boolean processMessage(Message m) {
            if (m.what == MSG_TRANSITION_TO) {
                transitionTo((State)m.payload);
                return true;
            }

            receiver.add(handle);
            return handled;
        }
    }

    private final Queue<Result> receiver;

    private final State A = new TestState(ENTER_A, EXIT_A, HANDLE_A, true);
    private final State A1 = new TestState(ENTER_A1, EXIT_A1, HANDLE_A1, true);
    private final State A2 = new TestState(ENTER_A2, EXIT_A2, HANDLE_A2, true);
    private final State B = new TestState(ENTER_B, EXIT_B, HANDLE_B, true);
    private final State B1 = new TestState(ENTER_B1, EXIT_B1, HANDLE_B1, false);

    public HierarchicalStateMachine(Looper looper, Queue<Result> listener) {
        super(looper);
        receiver = listener;
    }

    public void start() {
        addState(A);
            addState(A1, A);
            addState(A2, A);
        addState(B);
            addState(B1, B);
        setInitialState(A1);
        super.start();
    }

    public void transitionToA2() {
        sendMessage(obtainMessage(MSG_TRANSITION_TO)
                .withPayload(A2));
    }

    public void transitionToB1() {
        sendMessage(obtainMessage(MSG_TRANSITION_TO)
                .withPayload(B1));
    }
}

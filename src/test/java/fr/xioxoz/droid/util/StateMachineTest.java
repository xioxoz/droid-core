/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.util;

import fr.xioxoz.droid.os.HandlerThread;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StateMachineTest {

    private final Random random = new Random();

    @Test
    public void testStateMachine() throws InterruptedException {
        final BlockingQueue<SimpleStateMachine.Result> results = new ArrayBlockingQueue<>(16);
        HandlerThread thread = new HandlerThread("test-thread");
        thread.start();

        SimpleStateMachine sm = new SimpleStateMachine(thread.getLooper(), results);

        // We should enter the first state
        sm.start();
        assertEquals(SimpleStateMachine.Result.ENTER_STATE1, results.take());

        // Ensure messages are handled in state 1
        sm.sendMessage(random.nextInt(10000));
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE1, results.take());

        // Transition to state 2
        sm.sendMessage(SimpleStateMachine.MOVE_TO_STATE2);
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE1, results.take());
        assertEquals(SimpleStateMachine.Result.EXIT_STATE1, results.take());
        assertEquals(SimpleStateMachine.Result.ENTER_STATE2, results.take());

        // Ensure messages are handled in state 2
        sm.sendMessage(random.nextInt(10000));
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE2, results.take());

        // Transition back to 1
        sm.sendMessage(SimpleStateMachine.MOVE_TO_STATE1);
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE2, results.take());
        assertEquals(SimpleStateMachine.Result.EXIT_STATE2, results.take());
        assertEquals(SimpleStateMachine.Result.ENTER_STATE1, results.take());

        thread.getLooper().quit();
        thread.join();
    }

    @Test
    public void testVariousSendMessages() throws InterruptedException {
        final BlockingQueue<SimpleStateMachine.Result> results = new ArrayBlockingQueue<>(16);
        HandlerThread thread = new HandlerThread("test-thread");
        thread.start();

        SimpleStateMachine sm = new SimpleStateMachine(thread.getLooper(), results);

        // We should enter the first state
        sm.start();
        assertEquals(SimpleStateMachine.Result.ENTER_STATE1, results.take());

        // Try to send various messages and wait for them to be handled
        sm.sendMessage(random.nextInt(10000));
        sm.sendMessageAtFrontOfQueue(sm.obtainMessage().withWhat(random.nextInt(10000)));
        sm.sendMessageDelayed(sm.obtainMessage(random.nextInt(10000)), 100l);
        sm.sendMessageAtTime(sm.obtainMessage(random.nextInt(10000)), System.currentTimeMillis() + 1000l);
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE1, results.take());
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE1, results.take());
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE1, results.take());
        assertEquals(SimpleStateMachine.Result.HANDLE_STATE1, results.take());


        thread.getLooper().quit();
        thread.join();
    }

    @Test
    public void testFailingSendMessages() throws InterruptedException {
        final BlockingQueue<SimpleStateMachine.Result> results = new ArrayBlockingQueue<>(16);
        HandlerThread thread = new HandlerThread("test-thread");
        thread.start();

        SimpleStateMachine sm = new SimpleStateMachine(thread.getLooper(), results);

        // We should enter the first state
        sm.start();
        assertEquals(SimpleStateMachine.Result.ENTER_STATE1, results.take());

        // Try to send various messages and catch failures
        try { sm.sendMessage(null); fail(); } catch (NullPointerException npe) { /* OK */ }
        try { sm.sendMessageAtTime(null, System.currentTimeMillis()); fail(); } catch (NullPointerException npe) { /* OK */ }
        try { sm.sendMessageDelayed(null, 1000l); fail(); } catch (NullPointerException npe) { /* OK */ }
        try { sm.sendMessageAtFrontOfQueue(null); fail(); } catch (NullPointerException npe) { /* OK */ }
        try { sm.sendMessageDelayed(sm.obtainMessage(), -1l); fail(); } catch (IllegalArgumentException iae) { /* OK */ }
        try { sm.sendMessageAtTime(sm.obtainMessage(), -1l); fail(); } catch (IllegalArgumentException iae) { /* OK */ }

        thread.getLooper().quit();
        thread.join();
    }

    @Test
    public void testHierarchicalStateMachine() throws InterruptedException {
        final BlockingQueue<HierarchicalStateMachine.Result> results = new ArrayBlockingQueue<>(16);
        HandlerThread thread = new HandlerThread("test-thread");
        thread.start();

        HierarchicalStateMachine sm = new HierarchicalStateMachine(thread.getLooper(), results);

        sm.start();
        assertEquals(HierarchicalStateMachine.Result.ENTER_A, results.take());
        assertEquals(HierarchicalStateMachine.Result.ENTER_A1, results.take());

        sm.sendMessage(random.nextInt()+1);
        assertEquals(HierarchicalStateMachine.Result.HANDLE_A1, results.take());

        sm.transitionToA2();
        assertEquals(HierarchicalStateMachine.Result.EXIT_A1, results.take());
        assertEquals(HierarchicalStateMachine.Result.ENTER_A2, results.take());

        sm.sendMessage(random.nextInt()+1);
        assertEquals(HierarchicalStateMachine.Result.HANDLE_A2, results.take());

        sm.transitionToB1();
        assertEquals(HierarchicalStateMachine.Result.EXIT_A2, results.take());
        assertEquals(HierarchicalStateMachine.Result.EXIT_A, results.take());
        assertEquals(HierarchicalStateMachine.Result.ENTER_B, results.take());
        assertEquals(HierarchicalStateMachine.Result.ENTER_B1, results.take());

        sm.sendMessage(random.nextInt()+1);
        assertEquals(HierarchicalStateMachine.Result.HANDLE_B1, results.take());
        assertEquals(HierarchicalStateMachine.Result.HANDLE_B, results.take());

        thread.getLooper().quit();
        thread.join();
    }

    @Test
    public void testDeferringStateMachine() throws InterruptedException {
        final BlockingQueue<DeferringStateMachine.Result> results = new ArrayBlockingQueue<>(16);
        HandlerThread thread = new HandlerThread("test-thread");
        thread.start();

        DeferringStateMachine sm = new DeferringStateMachine(thread.getLooper(), results);

        // We should enter the first state
        sm.start();
        assertEquals(DeferringStateMachine.Result.ENTER_STATE1, results.take());

        // Ensure messages are handled in state 1
        sm.sendMessage(random.nextInt(10000)+5);
        assertEquals(DeferringStateMachine.Result.HANDLE_STATE1, results.take());

        // Transition to state 2
        sm.sendMessage(DeferringStateMachine.MOVE_TO_STATE2);
        assertEquals(DeferringStateMachine.Result.HANDLE_STATE1, results.take());
        assertEquals(DeferringStateMachine.Result.EXIT_STATE1, results.take());
        assertEquals(DeferringStateMachine.Result.ENTER_STATE2, results.take());

        // The message is handled in state 2
        assertEquals(DeferringStateMachine.Result.HANDLE_STATE2, results.take());

        thread.getLooper().quit();
        thread.join();
    }
}

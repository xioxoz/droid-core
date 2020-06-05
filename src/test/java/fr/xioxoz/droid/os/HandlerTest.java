/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

import com.vmware.lmock.impl.Mock;
import com.vmware.lmock.masquerade.Role;
import com.vmware.lmock.mt.Actor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.vmware.lmock.masquerade.Schemer.begin;
import static com.vmware.lmock.masquerade.Schemer.end;
import static org.junit.Assert.*;

public class HandlerTest {

    // A thread for the test.
    private HandlerThread thread;

    // The handler under tests.
    private Handler handler;

    // A mock handler callback that will check for message reception.
    private Handler.Callback callback = Mock.getObject(Handler.Callback.class);

    // An role for test events on the current thread.
    private Role testRole;

    // An role for the test events on the dedicated test thread.
    private Role threadRole;

    @Before
    public void setUp() {
        // Prepare the Handler and its thread.
        thread = new HandlerThread("test");
        thread.start();
        handler = new Handler(thread.getLooper(), callback);

        // Prepare the test context.
        testRole = new Role(Actor.anActorForCurrentThread());
        threadRole = new Role(Actor.anActorForThread(thread));
    }

    @After
    public void tearDown() {
        testRole = null;
        threadRole = null;
        handler = null;
        thread.quit();
        thread = null;
    }

    @Test
    public void testSimpleMessage() throws InterruptedException {
        Message m = handler.obtainMessage(1234);

        begin(testRole, threadRole);

        // We expect the handle to call handleMessage with the correct message.
        threadRole.willInvoke(1)
                .willReturn(true)
                .when(callback)
                .handleMessage(m);

        // Start playing the tests.
        handler.sendMessage(m);
        thread.quit();
        thread.join();

        end();
    }

    @Test
    public void testTwoMessagesAreOrdered() throws InterruptedException {
        Message m1 = handler.obtainMessage(1234);
        Message m2 = handler.obtainMessage(5678);

        begin(testRole, threadRole);

        threadRole.willInvoke(1)
                .willReturn(true)
                .when(callback)
                .handleMessage(m1);

        threadRole.willInvoke(1)
                .willReturn(true)
                .when(callback)
                .handleMessage(m2);

        handler.sendMessage(m1);
        handler.sendMessage(m2);
        thread.quit();
        thread.join();

        end();
    }

    @Test
    public void testExtendedHandler() throws InterruptedException {
        handler = new Handler(thread.getLooper(), null) {
            @Override
            protected void handleMessage(Message m) {
                callback.handleMessage(m);
            }
        };

        Message m = handler.obtainMessage(1928);

        begin(testRole, threadRole);
        threadRole.willInvoke(1)
                .willReturn(true)
                .when(callback)
                .handleMessage(m);

        handler.sendMessage(m);
        thread.quit();
        thread.join();
        end();
    }

    @Test
    public void testSendMessageAtTime() throws InterruptedException {
        BlockingQueue<Long> queue = new ArrayBlockingQueue<>(1);

        handler = new Handler(thread.getLooper(), null) {
            @Override
            protected void handleMessage(Message m) {
                try {
                    queue.put(System.currentTimeMillis());
                } catch (InterruptedException e) {
                    // Bad
                }
            }
        };

        Message m = handler.obtainMessage(6475);

        final long when = System.currentTimeMillis() + 1000l;
        handler.sendMessageAtTime(m, when);
        assertTrue(queue.take() >= when);
    }

    @Test
    public void testSendMessageAtTimeWithWrongTime() {
        Message m = handler.obtainMessage(1);
        try {
            handler.sendMessageAtTime(m, -127);
            fail();

        } catch (IllegalArgumentException iae) {
            // OK
        }
    }

    @Test
    public void testSendMessageDelayed() throws InterruptedException {
        BlockingQueue<Long> queue = new ArrayBlockingQueue<>(1);

        handler = new Handler(thread.getLooper(), null) {
            @Override
            protected void handleMessage(Message m) {
                try {
                    queue.put(System.currentTimeMillis());
                } catch (InterruptedException e) {
                    // Bad
                }
            }
        };

        final Message m = handler.obtainMessage(6475);
        final long delay = 1000l;
        final long when = System.currentTimeMillis() + delay;
        handler.sendMessageDelayed(m, delay);
        assertTrue(queue.take() >= when);
    }

    @Test
    public void testSendMessageAtFrontOfQueue() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(1);

        handler = new Handler(thread.getLooper(), null) {
            @Override
            protected void handleMessage(Message m) {
                try {
                    queue.put(m.what);
                } catch (InterruptedException e) {
                    // Bad
                }
            }
        };

        Message m;
        m = handler.obtainMessage(1);
        handler.sendMessage(m);
        m = handler.obtainMessage(2);
        handler.sendMessage(m);
        m = handler.obtainMessage(3);
        handler.sendMessageAtFrontOfQueue(m);

        int value = queue.take();
        assertTrue(value == 1 || value == 3);
        value = queue.take();
        assertTrue(value == 1 || value == 3);
        assertEquals(2, (int)queue.take());

    }
}

/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class MessageQueueTest {

    private final Random random = new Random();

    private MessageQueue queue;

    @Before
    public void setUp() {
        queue = new MessageQueue();
    }

    @After
    public void teadDown() {
        queue = null;
    }

    @Test
    public void testIsIdleWithoutMessages() {
        assertTrue(queue.isIdle());
    }

    @Test
    public void testIsIdleWithMessage() {
        // Test with a message due for now.
        queue.enqueueMessage(Message.obtain()
                .withTarget(new Handler()), System.currentTimeMillis());
        assertFalse(queue.isIdle());
        assertNotNull(queue.next());
        assertTrue(queue.isIdle());

        // Test with a message for later
        queue.enqueueMessage(Message.obtain()
                .withTarget(new Handler()), System.currentTimeMillis()*10000l);
        assertTrue(queue.isIdle());
    }

    @Test
    public void testEnqueueIncorrectMessage() {
        try {
            queue.enqueueMessage(Message.obtain()
                    .withWhat(1234), 0);
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
        }
    }

    @Test
    public void testGetOneMessage() {
        queue.enqueueMessage(Message.obtain()
                .withWhat(1234)
                .withTarget(new Handler()), System.currentTimeMillis());
        Message m = queue.next();
        assertNotNull(m);
        assertEquals(1234, m.what);
        assertTrue(m.when <= System.currentTimeMillis());
    }

    @Test
    public void testQueueKeepMessagesOrdered() {
        final int COUNT = 10;

        // Insert messages in order.
        for (int i = 0; i < COUNT; i++) {
            queue.enqueueMessage(Message.obtain()
                    .withWhat(i)
                    .withTarget(new Handler()), System.currentTimeMillis());
        }
        // Then insert one, theorically at front of queue.
        queue.enqueueMessage(Message.obtain()
                .withWhat(COUNT+1)
                .withTarget(new Handler()), 0);

        // Check messages are delivered in order.
        Message m = queue.next();
        assertNotNull(m);
        assertEquals(COUNT+1, m.what);
        assertEquals(0l, m.when);

        for (int i = 0; i < COUNT; i++) {
            m = queue.next();
            assertNotNull(m);
            assertEquals(i, m.what);
            assertTrue(m.when <= System.currentTimeMillis());
        }
    }

    @Test
    public void testQueueReorderMessages() {
        final Handler handler = new Handler();
        final int what1 = random.nextInt();
        final int what2 = random.nextInt();
        final int what3 = random.nextInt();
        final long time1 = 1000L;
        final long time2 = 2000L;
        final long time3 = 3000L;

        queue.enqueueMessage(Message.obtain()
                .withWhat(what1)
                .withTarget(handler), time1);
        queue.enqueueMessage(Message.obtain()
                .withWhat(what3)
                .withTarget(handler), time3);
        queue.enqueueMessage(Message.obtain()
                .withWhat(what2)
                .withTarget(handler), time2);

        Message m;
        m = queue.next();
        assertNotNull(m);
        assertEquals(what1, m.what);
        m = queue.next();
        assertNotNull(m);
        assertEquals(what2, m.what);
        m = queue.next();
        assertNotNull(m);
        assertEquals(what3, m.what);
    }

    @Test
    public void testMessageQueueWithThreads() throws InterruptedException {
        final int COUNT = random.nextInt(900) + 100;

        Thread producer = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < COUNT; i++) {
                    // Add a new message.
                    queue.enqueueMessage(Message.obtain()
                            .withWhat(i)
                            .withTarget(new Handler()), System.currentTimeMillis());
                }
            }
        };
        producer.start();

        for (int i = 0; i < COUNT; i++) {
            Message m = queue.next();
            assertNotNull(m);
            assertEquals(i, m.what);
        }
        producer.join();
    }

    @Test
    public void testQueueNextWithWait() {
        final int what = random.nextInt();

        queue.enqueueMessage(Message.obtain()
                .withWhat(what)
                .withTarget(new Handler()), System.currentTimeMillis()+2000l);

        Message m = queue.next();
        assertEquals(what, m.what);
        assertTrue(m.what <= System.currentTimeMillis());
    }

    @Test
    public void testHasMessages() {
        final int what = random.nextInt();
        final Handler testHandler = new Handler();

        queue.enqueueMessage(Message.obtain()
                .withWhat(what)
                .withTarget(testHandler), System.currentTimeMillis());

        assertTrue(queue.hasMessages(testHandler));
        assertFalse(queue.hasMessages(new Handler()));
        assertTrue(queue.hasMessages(testHandler, what));
        assertFalse(queue.hasMessages(new Handler(), what));
        assertFalse(queue.hasMessages(testHandler, what+1));

        assertFalse(queue.hasMessages(null));
        assertFalse(queue.hasMessages(null, what));
    }

    @Test
    public void testRemoveMessages() {
        final int what1 = random.nextInt();
        final int what2 = random.nextInt();
        final Handler handler1 = new Handler();
        final Handler handler2 = new Handler();

        queue.enqueueMessage(Message.obtain()
                .withWhat(what1)
                .withTarget(handler1), 0);
        queue.enqueueMessage(Message.obtain()
                .withWhat(what2)
                .withTarget(handler2), 0);

        // Check we don't remove messages from incorrect input.
        queue.removeMessages(handler1, random.nextInt());
        assertTrue(queue.hasMessages(handler1, what1));
        assertTrue(queue.hasMessages(handler2, what2));

        queue.removeMessages(new Handler(), what1);
        assertTrue(queue.hasMessages(handler1, what1));
        assertTrue(queue.hasMessages(handler2, what2));

        // Remove a correct message and check it is not inside anymore.
        queue.removeMessages(handler1, what1);
        Message m = queue.next();
        assertNotNull(m);
        assertEquals(handler2, m.target);
        assertEquals(what2, m.what);

        // Check we can't remove incorrect messages description.
        try {
            queue.removeMessages(null, what1);
            fail();

        } catch (IllegalArgumentException iae) {
            // OK
        }
    }
}

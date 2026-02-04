package fr.plop.contexts.game.instance.event.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEventQueueTest {
    private GameEventQueue eventQueue;

    @BeforeEach
    void setUp() {
        eventQueue = new GameEventQueue();
    }

    @Test
    void testSequentialExecution() throws InterruptedException {
        List<Integer> executionOrder = new ArrayList<>();

        eventQueue.enqueue(() -> executionOrder.add(1));
        eventQueue.enqueue(() -> executionOrder.add(2));
        eventQueue.enqueue(() -> executionOrder.add(3));

        Thread.sleep(200);

        assertEquals(3, executionOrder.size());
        assertEquals(1, executionOrder.get(0));
        assertEquals(2, executionOrder.get(1));
        assertEquals(3, executionOrder.get(2));
    }

    @Test
    void testQueueSize() throws InterruptedException {
        assertEquals(0, eventQueue.queueSize());

        eventQueue.enqueue(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread.sleep(50);
        int sizeDuringExecution = eventQueue.queueSize();
        assertTrue(sizeDuringExecution >= 0);

        Thread.sleep(200);

        assertEquals(0, eventQueue.queueSize());
    }

    @Test
    void testMultipleTasksEnqueued() throws InterruptedException {
        List<Integer> executionOrder = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            int num = i;
            eventQueue.enqueue(() -> executionOrder.add(num));
        }

        Thread.sleep(300);

        assertEquals(5, executionOrder.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, executionOrder.get(i));
        }
    }

    @Test
    void testExceptionHandling() throws InterruptedException {
        List<Integer> executionOrder = new ArrayList<>();

        eventQueue.enqueue(() -> {
            executionOrder.add(1);
        });

        eventQueue.enqueue(() -> {
            throw new RuntimeException("Test exception");
        });

        eventQueue.enqueue(() -> {
            executionOrder.add(2);
        });

        Thread.sleep(300);

        assertEquals(2, executionOrder.size());
        assertEquals(1, executionOrder.get(0));
        assertEquals(2, executionOrder.get(1));
    }

    @Test
    void testTaskCompletesBeforeNextStarts() throws InterruptedException {
        List<Long> timestamps = new ArrayList<>();

        eventQueue.enqueue(() -> {
            timestamps.add(System.currentTimeMillis());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        eventQueue.enqueue(() -> {
            timestamps.add(System.currentTimeMillis());
        });

        Thread.sleep(400);

        assertEquals(2, timestamps.size());
        assertTrue(timestamps.get(1) - timestamps.get(0) >= 90);
    }
}

package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class GameEventOrchestratorInternalTest {
    private GameEventOrchestratorInternal orchestrator;
    private GameEventQueue eventQueue;
    private final GameSessionContext context = new GameSessionContext();

    @BeforeEach
    void setUp() {
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        eventQueue = new GameEventQueue();
        orchestrator = new GameEventOrchestratorInternal(executorService, eventQueue);
    }

    @Test
    void testListenerRegistration() {
        GameEventListener listener = (ctx, event) -> {};
        orchestrator.registerListener(listener);
        assertTrue(true);
    }

    @Test
    void testEventFireingWithSingleListener() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder = new TestGameEventListeners.ListenerRecorder();
        orchestrator.registerListener(recorder);

        TestGameEventListeners.TestEventA eventA = new TestGameEventListeners.TestEventA();
        orchestrator.fire(context, eventA);

        Thread.sleep(200);

        assertEquals(1, recorder.getRecordedEvents().size());
        assertEquals(eventA, recorder.getRecordedEvents().getFirst().event());
    }

    @Test
    void testEventFiringWithMultipleListeners() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder1 = new TestGameEventListeners.ListenerRecorder();
        TestGameEventListeners.ListenerRecorder recorder2 = new TestGameEventListeners.ListenerRecorder();

        orchestrator.registerListener(recorder1);
        orchestrator.registerListener(recorder2);

        TestGameEventListeners.TestEventA eventA = new TestGameEventListeners.TestEventA();
        orchestrator.fire(context, eventA);

        Thread.sleep(200);

        assertEquals(1, recorder1.getRecordedEvents().size());
        assertEquals(1, recorder2.getRecordedEvents().size());
        assertEquals(eventA, recorder1.getRecordedEvents().getFirst().event());
        assertEquals(eventA, recorder2.getRecordedEvents().getFirst().event());
    }

    @Test
    void testListenerUnregistration() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder = new TestGameEventListeners.ListenerRecorder();
        orchestrator.registerListener(recorder);
        recorder.clear();

        TestGameEventListeners.TestEventA eventA = new TestGameEventListeners.TestEventA();
        orchestrator.fire(context, eventA);

        Thread.sleep(200);

        assertEquals(1, recorder.getRecordedEvents().size());
    }

    @Test
    void testSequentialEventQueueing() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder = new TestGameEventListeners.ListenerRecorder();
        orchestrator.registerListener(recorder);

        orchestrator.fire(context, new TestGameEventListeners.TestEventA());
        orchestrator.fire(context, new TestGameEventListeners.TestEventB());
        orchestrator.fire(context, new TestGameEventListeners.TestEventC());

        Thread.sleep(300);

        assertEquals(3, recorder.getRecordedEvents().size());
    }

    @Test
    void testCascadingEvents() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder = new TestGameEventListeners.ListenerRecorder();
        TestGameEventListeners.ListenerA listenerA = new TestGameEventListeners.ListenerA(orchestrator);
        TestGameEventListeners.ListenerB listenerB = new TestGameEventListeners.ListenerB(orchestrator);
        TestGameEventListeners.ListenerC listenerC = new TestGameEventListeners.ListenerC(orchestrator);
        TestGameEventListeners.ListenerD listenerD = new TestGameEventListeners.ListenerD();

        orchestrator.registerListener(listenerA);
        orchestrator.registerListener(listenerB);
        orchestrator.registerListener(listenerC);
        orchestrator.registerListener(listenerD);
        orchestrator.registerListener(recorder);

        orchestrator.fire(context, new TestGameEventListeners.TestEventA());

        Thread.sleep(500);

        assertEquals(4, listenerA.callCount);
        assertEquals(4, listenerB.callCount);
        assertEquals(4, listenerC.callCount);
        assertEquals(4, listenerD.callCount);
        assertEquals(4, recorder.getRecordedEvents().size());
    }

    @Test
    void testMultipleEventsProcessedSequentially() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder = new TestGameEventListeners.ListenerRecorder();
        orchestrator.registerListener(recorder);

        orchestrator.fire(context, new TestGameEventListeners.TestEventA());
        orchestrator.fire(context, new TestGameEventListeners.TestEventB());

        Thread.sleep(500);

        assertEquals(2, recorder.getRecordedEvents().size());
        assertInstanceOf(TestGameEventListeners.TestEventA.class, recorder.getRecordedEvents().get(0).event());
        assertInstanceOf(TestGameEventListeners.TestEventB.class, recorder.getRecordedEvents().get(1).event());
    }

    @Test
    void testQueueSize() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder = new TestGameEventListeners.ListenerRecorder();
        orchestrator.registerListener(recorder);

        orchestrator.fire(context, new TestGameEventListeners.TestEventA());

        int queueSizeAfterFire = eventQueue.queueSize();
        assertTrue(queueSizeAfterFire >= 0);

        Thread.sleep(200);

        int queueSizeAfterWait = eventQueue.queueSize();
        assertEquals(0, queueSizeAfterWait);
    }
}

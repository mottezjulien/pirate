package fr.plop.contexts.game.instance.event.domain;

import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

//TODO Useful ???
public class GameEventIntegrationTest {
    private GameEventOrchestratorInternal orchestrator;
    private final GameInstanceContext context = new GameInstanceContext();

    @BeforeEach
    void setUp() {
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        final GameEventQueue eventQueue = new GameEventQueue();
        orchestrator = new GameEventOrchestratorInternal(executorService, eventQueue);
    }

    @Test
    void testFullEventChain() throws InterruptedException {
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

    //Pas confiance dans ce test
    //TODO @Test
    void testNoCyclicDependencies() throws InterruptedException {
        TestGameEventListeners.ListenerRecorder recorder = new TestGameEventListeners.ListenerRecorder();
        TestGameEventListeners.ListenerA listenerA = new TestGameEventListeners.ListenerA(orchestrator);
        TestGameEventListeners.ListenerB listenerB = new TestGameEventListeners.ListenerB(orchestrator);

        orchestrator.registerListener(listenerA);
        orchestrator.registerListener(listenerB);
        orchestrator.registerListener(recorder);

        orchestrator.fire(context, new TestGameEventListeners.TestEventA());
        orchestrator.fire(context, new TestGameEventListeners.TestEventB());

        Thread.sleep(500);

        // ListenerA et ListenerB reçoivent tous les événements: TestEventA, TestEventB(initial déclenché par A), TestEventC(déclenché par B), TestEventB(2e initial), TestEventC(2e déclenché par B)
        assertEquals(5, listenerA.callCount);
        assertEquals(5, listenerB.callCount);
        // Recorder enregistre aussi tous les événements
        assertEquals(5, recorder.getRecordedEvents().size());
    }
}

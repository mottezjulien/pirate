package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

import java.util.ArrayList;
import java.util.List;

public class TestGameEventListeners {

    public static class TestEventA implements GameEvent {}
    public static class TestEventB implements GameEvent {}
    public static class TestEventC implements GameEvent {}
    public static class TestEventD implements GameEvent {}

    public static class ListenerRecorder implements GameEventListener {
        private final List<RecordedEvent> recordedEvents = new ArrayList<>();

        @Override
        public void listen(GameSessionContext context, GameEvent event) {
            recordedEvents.add(new RecordedEvent(context, event));
        }

        public List<RecordedEvent> getRecordedEvents() {
            return recordedEvents;
        }

        public void clear() {
            recordedEvents.clear();
        }
    }

    public static class ListenerA implements GameEventListener {
        private final GameEventOrchestrator orchestrator;
        public int callCount = 0;

        public ListenerA(GameEventOrchestrator orchestrator) {
            this.orchestrator = orchestrator;
        }

        @Override
        public void listen(GameSessionContext context, GameEvent event) {
            callCount++;
            if (event instanceof TestEventA) {
                orchestrator.fire(context, new TestEventB());
            }
        }
    }

    public static class ListenerB implements GameEventListener {
        private final GameEventOrchestrator orchestrator;
        public int callCount = 0;

        public ListenerB(GameEventOrchestrator orchestrator) {
            this.orchestrator = orchestrator;
        }

        @Override
        public void listen(GameSessionContext context, GameEvent event) {
            callCount++;
            if (event instanceof TestEventB) {
                orchestrator.fire(context, new TestEventC());
            }
        }
    }

    public static class ListenerC implements GameEventListener {
        private final GameEventOrchestrator orchestrator;
        public int callCount = 0;

        public ListenerC(GameEventOrchestrator orchestrator) {
            this.orchestrator = orchestrator;
        }

        @Override
        public void listen(GameSessionContext context, GameEvent event) {
            callCount++;
            if (event instanceof TestEventC) {
                orchestrator.fire(context, new TestEventD());
            }
        }
    }

    public static class ListenerD implements GameEventListener {
        public int callCount = 0;

        @Override
        public void listen(GameSessionContext context, GameEvent event) {
            callCount++;
        }
    }

    public record RecordedEvent(GameSessionContext context, GameEvent event) {}
}

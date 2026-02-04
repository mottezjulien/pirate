package fr.plop.contexts.game.instance.event.domain;

import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class GameEventOrchestratorInternal implements GameEventOrchestrator {
    private final ExecutorService executorService;
    
    public final GameEventQueue eventQueue;

    private final List<GameEventListener> listeners = new ArrayList<>();

    public GameEventOrchestratorInternal(ExecutorService executorService, GameEventQueue eventQueue) {
        this.executorService = executorService;
        this.eventQueue = eventQueue;
    }

    public void registerListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void fire(GameInstanceContext context, GameEvent event) {
        Runnable runnable = () -> listeners
                .forEach(listener -> listener.listen(context, event));
        eventQueue.enqueue(() -> CompletableFuture.runAsync(runnable, executorService));
    }

    @Override
    public void fireAndWait(GameInstanceContext context, GameEvent event) {
        listeners.forEach(listener -> listener.listen(context, event));
    }
}

package fr.plop.contexts.game.instance.event.domain;

import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import org.springframework.stereotype.Component;

@Component
public class GameEventOrchestratorLazy implements GameEventOrchestrator {

    private GameEventOrchestrator orchestrator;

    public void fire(GameInstanceContext context, GameEvent event) {
        orchestrator.fire(context, event);
    }

    @Override
    public void fireAndWait(GameInstanceContext context, GameEvent event) {
        orchestrator.fireAndWait(context, event);
    }

    public void set(GameEventOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

}

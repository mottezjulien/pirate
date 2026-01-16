package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import org.springframework.stereotype.Component;

@Component
public class GameEventOrchestratorLazy implements GameEventOrchestrator {

    private GameEventOrchestrator orchestrator;

    public void fire(GameSessionContext context, GameEvent event) {
        orchestrator.fire(context, event);
    }

    @Override
    public void fireAndWait(GameSessionContext context, GameEvent event) {
        orchestrator.fireAndWait(context, event);
    }

    public void set(GameEventOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

}

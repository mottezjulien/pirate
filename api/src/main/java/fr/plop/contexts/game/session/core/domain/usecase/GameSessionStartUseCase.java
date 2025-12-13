package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.port.GameSessionGetPort;
import fr.plop.contexts.game.session.scenario.domain.GameSessionScenarioGoalPort;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.time.GameSessionTimer;

public class GameSessionStartUseCase {

    public interface Port {
        void active(GameSession.Id sessionId);
    }

    private final Port port;

    private final GameConfigCache cache;
    private final GameSessionGetPort get;
    private final GameSessionTimer timer;

    private final GameSessionScenarioGoalPort scenarioGoalPort;


    public GameSessionStartUseCase(Port port, GameConfigCache cache, GameSessionGetPort get, GameSessionTimer timer, GameSessionScenarioGoalPort scenarioGoalPort) {
        this.port = port;
        this.cache = cache;
        this.get = get;
        this.timer = timer;
        this.scenarioGoalPort = scenarioGoalPort;
    }

    public GameSession.Id apply(GameSessionContext context) throws GameException {

        final GameSession.Id session = get.findById(context.sessionId())
                .orElseThrow(() -> new GameException(GameException.Type.SESSION_NOT_FOUND));

        //TODO check if player can start in the session
        //TODO check if session is active or not
        timer.start(context.sessionId());
        //TODO notify other players that the game has started
        port.active(context.sessionId());
        ScenarioConfig.Step step = cache.scenario(context.sessionId()).firstStep();
        scenarioGoalPort.saveStep(context, step.id(), ScenarioSessionState.ACTIVE);

        return session;
    }

}

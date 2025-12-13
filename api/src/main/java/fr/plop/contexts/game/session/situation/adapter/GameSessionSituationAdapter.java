package fr.plop.contexts.game.session.situation.adapter;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.scenario.domain.GameSessionScenarioGoalPort;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.contexts.game.session.time.GameSessionTimerGet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameSessionSituationAdapter implements GameSessionSituationGetPort {

    private final GamePlayerGetPort gamePlayerGetPort;
    private final GameSessionScenarioGoalPort scenarioGoalPort;
    private final GameSessionTimerGet timerProvider;

    public GameSessionSituationAdapter(GamePlayerGetPort gamePlayerGetPort, GameSessionScenarioGoalPort scenarioGoalPort, GameSessionTimerGet timerProvider) {
        this.gamePlayerGetPort = gamePlayerGetPort;
        this.scenarioGoalPort = scenarioGoalPort;
        this.timerProvider = timerProvider;
    }


    @Override
    public GameSessionSituation get(GameSessionContext context) {
        GameSessionSituation.Time time = new GameSessionSituation.Time(timerProvider.current(context.sessionId()));
        return new GameSessionSituation(board(context.playerId()), scenario(context.playerId()), time);
    }

    private GameSessionSituation.Board board(GamePlayer.Id playerId) {
        List<BoardSpace.Id> spaceIds = gamePlayerGetPort.findSpaceIdsByPlayerId(playerId);
        return new GameSessionSituation.Board(spaceIds);

    }

    private GameSessionSituation.Scenario scenario(GamePlayer.Id playerId) {
        List<ScenarioConfig.Step.Id> stepIds = scenarioGoalPort.findActiveSteps(playerId);
        List<ScenarioConfig.Target.Id> targetIds = scenarioGoalPort.findActiveTargets(playerId);
        return new GameSessionSituation.Scenario(stepIds, targetIds);
    }

}

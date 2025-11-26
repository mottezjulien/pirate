package fr.plop.contexts.game.session.situation.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.scenario.domain.usecase.ScenarioSessionPlayerGetUseCase;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.contexts.game.session.time.GameSessionTimerGet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GameSessionSituationAdapter implements GameSessionSituationGetPort {

    private final GamePlayerGetPort gamePlayerGetPort;
    private final ScenarioSessionPlayerGetUseCase scenarioSessionPlayerGetUseCase;
    private final GameSessionTimerGet timerProvider;

    public GameSessionSituationAdapter(GamePlayerGetPort gamePlayerGetPort, ScenarioSessionPlayerGetUseCase scenarioSessionPlayerGetUseCase, GameSessionTimerGet timerProvider) {
        this.gamePlayerGetPort = gamePlayerGetPort;
        this.scenarioSessionPlayerGetUseCase = scenarioSessionPlayerGetUseCase;
        this.timerProvider = timerProvider;
    }

    @Override
    public GameSessionSituation get(GameContext context) {
        GameSessionSituation.Time time = new GameSessionSituation.Time(timerProvider.current(context.sessionId()));
        return new GameSessionSituation(board(context.playerId()), scenario(context.playerId()), time);
    }

    @Override
    public GameSessionSituation get(GameSession.Id sessionId, GamePlayer player) {
        GameSessionSituation.Time time = new GameSessionSituation.Time(timerProvider.current(sessionId));
        return new GameSessionSituation(new GameSessionSituation.Board(player.spaceIds()), scenario(player.id()), time);
    }

    private GameSessionSituation.Board board(GamePlayer.Id playerId) {
        return  gamePlayerGetPort.findById(playerId)
                .map(player -> new GameSessionSituation.Board(player.spaceIds()))
                .orElseGet(() -> new GameSessionSituation.Board(List.of()));
    }

    private GameSessionSituation.Scenario scenario(GamePlayer.Id playerId) {
        ScenarioSessionPlayer scenarioSessionPlayer = scenarioSessionPlayerGetUseCase.findByPlayerId(playerId);
        List<ScenarioConfig.Step.Id> stepIds = scenarioSessionPlayer.bySteps().entrySet()
                .stream().filter(entry -> entry.getValue() == ScenarioSessionState.ACTIVE)
                .map(Map.Entry::getKey).toList();
        List<ScenarioConfig.Target.Id> targetIds = scenarioSessionPlayer.byTargets().entrySet()
                .stream().filter(entry -> entry.getValue() == ScenarioSessionState.ACTIVE)
                .map(Map.Entry::getKey).toList();
        return new GameSessionSituation.Scenario(stepIds, targetIds);
    }

}

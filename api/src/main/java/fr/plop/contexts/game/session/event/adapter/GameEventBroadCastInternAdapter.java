package fr.plop.contexts.game.session.event.adapter;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.condition.GameSessionSituation;
import fr.plop.contexts.game.config.condition.Situation;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.scenario.domain.usecase.ScenarioSessionPlayerGetUseCase;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.contexts.game.session.time.GameSessionTimerGet;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class GameEventBroadCastInternAdapter implements GameEventBroadCastIntern.Port {

    //TODO split ?? -> Split findAction & doAction

    private final GameConfigCache cache;
    private final GamePlayerGetPort playerGet;
    private final ScenarioSessionPlayerGetUseCase scenarioSessionPlayerGetUseCase;
    private final GamePlayerActionRepository actionRepository;
    private final GameOverUseCase gameOverUseCase;
    private final GameSessionTimerGet timerProvider;

    public GameEventBroadCastInternAdapter(GameConfigCache cache, GamePlayerGetPort playerGet, ScenarioSessionPlayerGetUseCase scenarioSessionPlayerGetUseCase, GamePlayerActionRepository actionRepository, GameOverUseCase gameOverUseCase, GameSessionTimerGet timerProvider) {
        this.cache = cache;
        this.playerGet = playerGet;
        this.scenarioSessionPlayerGetUseCase = scenarioSessionPlayerGetUseCase;
        this.actionRepository = actionRepository;
        this.gameOverUseCase = gameOverUseCase;
        this.timerProvider = timerProvider;
    }

    @Override
    public Stream<Possibility> findPossibilities(GameContext context) {
        List<ScenarioConfig.Step.Id> goalSteps = scenarioSessionPlayerGetUseCase.findActiveStepIdsByPlayerId(context.playerId());
        ScenarioConfig cacheScenario = cache.scenario(context.sessionId());
        return cacheScenario.steps().stream()
                .filter(step -> goalSteps.contains(step.id()))
                .flatMap(step -> step.possibilities().stream());
    }

    @Override
    public void doGameOver(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.SessionEnd consequence) {
        gameOverUseCase.apply(sessionId, playerId, consequence.gameOver());
    }

    @Override
    public void saveAction(GamePlayer.Id playerId, Possibility.Id possibilityId, GameSessionTimeUnit timeClick) {
        GamePlayerActionEntity entity = new GamePlayerActionEntity();
        entity.setId(StringTools.generate());
        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);
        ScenarioPossibilityEntity possibilityEntity = new ScenarioPossibilityEntity();
        possibilityEntity.setId(possibilityId.value());
        entity.setPossibility(possibilityEntity);
        entity.setDate(Instant.now());
        entity.setTimeInMinutes(timeClick.toMinutes());
        actionRepository.save(entity);
    }

    @Override
    public List<GameAction> findActions(GamePlayer.Id id) {
        return actionRepository.fullByPlayerId(id.value())
                .stream().map(GamePlayerActionEntity::toModel).toList();
    }

    @Override
    public GameSessionTimeUnit current(GameSession.Id sessionId) {
        return timerProvider.current(sessionId);
    }

    @Override
    public GameSessionSituation generateSituation(GameContext context) {
        Situation.Board board = situationBoard(context);
        Situation.Scenario scenario = situationScenario(context);
        Situation.Time time = new Situation.Time(timerProvider.current(context.sessionId()));
        return new GameSessionSituation(board, scenario, time);
    }

    private Situation.Board situationBoard(GameContext context) {
        return playerGet.findById(context.playerId())
                .map(player -> new Situation.Board(player.spaceIds()))
                .orElseGet(() -> new Situation.Board(List.of()));
    }

    private Situation.Scenario situationScenario(GameContext context) {
        ScenarioSessionPlayer scenarioSessionPlayer = scenarioSessionPlayerGetUseCase.findByPlayerId(context);
        List<ScenarioConfig.Step.Id> stepIds = scenarioSessionPlayer.bySteps().entrySet()
                .stream().filter(entry -> entry.getValue() == ScenarioSessionState.ACTIVE)
                .map(Map.Entry::getKey).toList();
        List<ScenarioConfig.Target.Id> targetIds = scenarioSessionPlayer.byTargets().entrySet()
                .stream().filter(entry -> entry.getValue() == ScenarioSessionState.ACTIVE)
                .map(Map.Entry::getKey).toList();
        return new Situation.Scenario(stepIds, targetIds);
    }

}

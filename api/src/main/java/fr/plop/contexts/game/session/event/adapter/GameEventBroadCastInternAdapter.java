package fr.plop.contexts.game.session.event.adapter;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.scenario.domain.usecase.ScenarioSessionPlayerGetUseCase;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.contexts.game.session.time.GameSessionTimerGet;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Component
public class GameEventBroadCastInternAdapter implements GameEventBroadCastIntern.Port {

    //TODO split ?? -> Split findAction & doAction
    private final GameConfigCache cache;

    private final ScenarioSessionPlayerGetUseCase scenarioSessionPlayerGetUseCase;
    private final GamePlayerActionRepository actionRepository;
    private final GameOverUseCase gameOverUseCase;
    private final GameSessionTimerGet timerProvider;

    public GameEventBroadCastInternAdapter(GameConfigCache cache, ScenarioSessionPlayerGetUseCase scenarioSessionPlayerGetUseCase, GamePlayerActionRepository actionRepository, GameOverUseCase gameOverUseCase, GameSessionTimerGet timerProvider) {
        this.cache = cache;
        this.scenarioSessionPlayerGetUseCase = scenarioSessionPlayerGetUseCase;
        this.actionRepository = actionRepository;
        this.gameOverUseCase = gameOverUseCase;
        this.timerProvider = timerProvider;
    }

    @Override
    public Stream<Possibility> findPossibilities(GameSessionContext context) {
        List<ScenarioConfig.Step.Id> goalSteps = scenarioSessionPlayerGetUseCase.findActiveStepIdsByPlayerId(context.playerId());
        ScenarioConfig cacheScenario = cache.scenario(context.sessionId());
        return cacheScenario.steps().stream()
                .filter(step -> goalSteps.contains(step.id()))
                .flatMap(step -> step.possibilities().stream());
    }

    @Override
    public void doGameOver(GameSessionContext context, Consequence.SessionEnd consequence) {
        gameOverUseCase.apply(context, consequence.gameOver());
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


}

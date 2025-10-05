package fr.plop.contexts.game.session.event.adapter;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.event.adapter.action.GameEventMessage;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.scenario.adapter.GameEventScenarioAdapter;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import fr.plop.contexts.game.session.time.GameSessionTimer;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.tools.StringTools;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Component
public class GameEventBroadCastInternAdapter implements GameEventBroadCastIntern.OutPort {

    private final ScenarioGoalRepository goalRepository;
    private final GamePlayerActionRepository actionRepository;
    private final GameEventScenarioAdapter scenarioAdapter;
    private final GameOverUseCase gameOverUseCase;
    private final GameEventMessage messageAction;
    private final GameSessionTimer timer;

    public GameEventBroadCastInternAdapter(ScenarioGoalRepository goalRepository, GamePlayerActionRepository actionRepository, GameEventScenarioAdapter scenarioAdapter,
                                           GameOverUseCase gameOverUseCase, GameEventMessage messageAction, @Lazy GameSessionTimer timer) {
        this.goalRepository = goalRepository;
        this.actionRepository = actionRepository;
        this.scenarioAdapter = scenarioAdapter;
        this.gameOverUseCase = gameOverUseCase;
        this.messageAction = messageAction;
        this.timer = timer;
    }

    //TODO Cache ??
    @Override
    public Stream<Possibility> findPossibilities(GameSession.Id sessionId, GamePlayer.Id playerId) {
        return goalRepository.fullByPlayerId(playerId.value())
                .stream()
                .flatMap(goalEntity -> goalEntity.getStep().toModel().possibilities().stream());
    }

    @Override
    public void doGoal(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.ScenarioStep consequence) {
        // Only persist/update state here; domain will orchestrate follow-up events
        scenarioAdapter.updateStateOrCreateGoal(playerId, consequence);
    }

    @Override
    public void doGoalTarget(GamePlayer.Id playerId, Consequence.ScenarioTarget consequence) {
        scenarioAdapter.updateStateOrCreateGoalTarget(playerId, consequence);
    }

    @Override
    public void doGameOver(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.SessionEnd consequence) {
        gameOverUseCase.apply(sessionId, playerId, consequence.gameOver());
    }

    @Override
    public void doMessage(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.DisplayMessage message) {
        messageAction.apply(sessionId, playerId, message.value());
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
        return timer.current(sessionId);
    }

}

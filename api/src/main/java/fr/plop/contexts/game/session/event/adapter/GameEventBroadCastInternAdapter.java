package fr.plop.contexts.game.session.event.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.persistence.possibility.ScenarioPossibilityEntity;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerActionRepository;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionMessage;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.scenario.adapter.GameEventScenarioAdapter;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import fr.plop.contexts.game.session.time.TimeClick;
import fr.plop.generic.tools.StringTools;
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
    private final GameEventActionMessage messageAction;

    public GameEventBroadCastInternAdapter(ScenarioGoalRepository goalRepository, GamePlayerActionRepository actionRepository, GameEventScenarioAdapter scenarioAdapter,
                                           GameOverUseCase gameOverUseCase, GameEventActionMessage messageAction) {
        this.goalRepository = goalRepository;
        this.actionRepository = actionRepository;
        this.scenarioAdapter = scenarioAdapter;
        this.gameOverUseCase = gameOverUseCase;
        this.messageAction = messageAction;
    }

    //TODO Cache ??
    @Override
    public Stream<Possibility> findPossibilities(GameSession.Id gameId, GamePlayer.Id playerId) {
        return goalRepository.fullByPlayerId(playerId.value())
                .stream()
                .flatMap(goalEntity -> goalEntity.getStep().toModel().possibilities().stream());
    }

    @Override
    public void doGoal(GamePlayer.Id playerId, PossibilityConsequence.Goal consequence) {
        scenarioAdapter.updateStateOrCreateGoal(playerId, consequence);
    }

    @Override
    public void doGoalTarget(GamePlayer.Id playerId, PossibilityConsequence.GoalTarget consequence) {
        scenarioAdapter.updateStateOrCreateGoalTarget(playerId, consequence);
    }

    @Override
    public void doGameOver(GameSession.Id sessionId, GamePlayer.Id playerId, PossibilityConsequence.GameOver consequence) {
        gameOverUseCase.apply(sessionId, playerId, consequence.gameOver());
    }

    @Override
    public void doAlert(GameSession.Id sessionId, GamePlayer.Id playerId, PossibilityConsequence.Alert consequence) {
        messageAction.alert(sessionId, playerId, consequence);
    }

    @Override
    public void saveAction(GamePlayer.Id playerId, Possibility.Id possibilityId, TimeClick timeClick) {
        GamePlayerActionEntity entity = new GamePlayerActionEntity();
        entity.setId(StringTools.generate());
        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);
        ScenarioPossibilityEntity possibilityEntity = new ScenarioPossibilityEntity();
        possibilityEntity.setId(possibilityId.value());
        entity.setPossibility(possibilityEntity);
        entity.setDate(Instant.now());
        entity.setTimeClickMinute(timeClick.minutes());
        actionRepository.save(entity);
    }

    @Override
    public List<GameAction> findActions(GamePlayer.Id id) {
        return actionRepository.fullByPlayerId(id.value())
                .stream().map(GamePlayerActionEntity::toModel).toList();
    }

}

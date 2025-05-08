package fr.plop.contexts.game.session.event.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionGame;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionMessage;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionScenarioGoal;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class GameEventBroadCastInternAdapter implements GameEventBroadCastIntern.OutPort {

    private final ScenarioGoalRepository goalRepository;
    private final GameEventActionScenarioGoal goalAction;
    private final GameEventActionGame gameAction;
    private final GameEventActionMessage messageAction;

    public GameEventBroadCastInternAdapter(ScenarioGoalRepository goalRepository, GameEventActionScenarioGoal goalAction, GameEventActionGame gameAction, GameEventActionMessage messageAction) {
        this.goalRepository = goalRepository;
        this.goalAction = goalAction;
        this.gameAction = gameAction;
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
    public void doGoal(GamePlayer.Id id, PossibilityConsequence.Goal consequence) {
        goalAction.updateOrCreate(id, consequence);
    }

    @Override
    public void doGameOver(GamePlayer.Id id, PossibilityConsequence.GameOver consequence) {
        gameAction.over(id);
    }

    @Override
    public void doAlert(GamePlayer.Id id, PossibilityConsequence.Alert consequence) {
        messageAction.alert(id, consequence);
    }

}

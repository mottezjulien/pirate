package fr.plop.contexts.game.session.event.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionScenarioSuccessGoalAdapter;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCastIntern;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class GameEventBroadCastInternAdapter implements GameEventBroadCastIntern.OutPort {

    private final ScenarioGoalRepository goalRepository;
    private final GameEventActionScenarioSuccessGoalAdapter successGoalAction;

    public GameEventBroadCastInternAdapter(ScenarioGoalRepository goalRepository, GameEventActionScenarioSuccessGoalAdapter successGoalAction) {
        this.goalRepository = goalRepository;
        this.successGoalAction = successGoalAction;
    }

    //TODO Cache ??
    @Override
    public Stream<Possibility> findPossibilities(GameSession.Id gameId, GamePlayer.Id playerId) {
        return goalRepository.fullByPlayerId(playerId.value())
                .stream()
                .flatMap(goalEntity -> goalEntity.getStep().toModel().possibilities().stream());
    }

    @Override
    public void doSuccessGoal(GamePlayer.Id id, PossibilityConsequence.SuccessGoal successGoal) {
        successGoalAction.apply(id, successGoal);
    }

}

package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import org.springframework.stereotype.Component;

@Component
public class GameEventActionScenarioSuccessGoalAdapter {

    private final ScenarioGoalRepository goalRepository;

    public GameEventActionScenarioSuccessGoalAdapter(ScenarioGoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public void apply(GamePlayer.Id playerId, PossibilityConsequence.SuccessGoal consequence) {
        goalRepository.byPlayerIdAndStepId(playerId.value(), consequence.stepId().value())
                .ifPresent(goalEntity -> {
                    goalEntity.setState(ScenarioGoal.State.SUCCESS);
                    goalRepository.save(goalEntity);
                });
    }

}

package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalStepRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class GameEventActionScenarioAdapter {
    private final ScenarioGoalStepRepository goalStepRepository;
    private final ScenarioGoalTargetRepository goalTargetRepository;

    public GameEventActionScenarioAdapter(ScenarioGoalStepRepository goalStepRepository, ScenarioGoalTargetRepository goalTargetRepository) {
        this.goalStepRepository = goalStepRepository;
        this.goalTargetRepository = goalTargetRepository;
    }

    public void updateStateOrCreateGoalStep(GamePlayer.Id playerId, Consequence.ScenarioStep consequence) {
        Optional<ScenarioGoalStepEntity> optEntity = goalStepRepository.byPlayerIdAndStepId(playerId.value(), consequence.stepId().value());
        optEntity.ifPresentOrElse(entity -> {
            entity.setState(consequence.state());
            goalStepRepository.save(entity);
        }, () -> goalStepRepository.save(ScenarioGoalStepEntity.build(playerId, consequence.stepId(), consequence.state())));
    }


    public void updateStateOrCreateGoalTarget(GamePlayer.Id playerId, Consequence.ScenarioTarget consequence) {
        Optional<ScenarioGoalTargetEntity> optEntity = goalTargetRepository.byPlayerIdAndTargetId(playerId.value(), consequence.targetId().value());
        optEntity.ifPresentOrElse(entity -> {
            entity.setState(consequence.state());
            goalTargetRepository.save(entity);
        }, () -> goalTargetRepository.save(ScenarioGoalTargetEntity.build(playerId, consequence.targetId(), consequence.state())));
    }


}

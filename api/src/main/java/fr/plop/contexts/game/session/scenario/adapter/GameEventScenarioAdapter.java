package fr.plop.contexts.game.session.scenario.adapter;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalTargetRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class GameEventScenarioAdapter {
    private final ScenarioGoalRepository goalRepository;
    private final ScenarioGoalTargetRepository goalTargetRepository;

    public GameEventScenarioAdapter(ScenarioGoalRepository goalRepository, ScenarioGoalTargetRepository goalTargetRepository) {
        this.goalRepository = goalRepository;
        this.goalTargetRepository = goalTargetRepository;
    }

    public void updateStateOrCreateGoal(GamePlayer.Id playerId, PossibilityConsequence.Goal consequence) {
        Optional<ScenarioGoalEntity> optEntity = goalRepository.byPlayerIdAndStepId(playerId.value(), consequence.stepId().value());
        optEntity.ifPresentOrElse(entity -> {
            entity.setState(consequence.state());
            goalRepository.save(entity);
        }, () -> createGoal(playerId, consequence));
    }

    public void updateStateOrCreateGoalTarget(GamePlayer.Id playerId, PossibilityConsequence.GoalTarget consequence) {
        Optional<ScenarioGoalTargetEntity> optEntity = goalTargetRepository.byPlayerIdAndTargetId(playerId.value(), consequence.targetId().value());
        optEntity.ifPresentOrElse(entity -> {
            entity.setState(consequence.state());
            goalTargetRepository.save(entity);
        }, () -> createGoalTarget(playerId, consequence));
    }

    private void createGoalTarget(GamePlayer.Id playerId, PossibilityConsequence.GoalTarget goalTarget) {
        ScenarioGoalEntity goalEntity = goalRepository.byPlayerIdAndStepId(playerId.value(), goalTarget.stepId().value())
                .orElseGet(() -> createGoal(playerId, new PossibilityConsequence.Goal(null, goalTarget.stepId(), ScenarioGoal.State.ACTIVE)));

        ScenarioGoalTargetEntity goalTargetEntity = new ScenarioGoalTargetEntity();
        goalTargetEntity.setId(StringTools.generate());
        goalTargetEntity.setGoal(goalEntity);
        goalTargetEntity.setState(goalTarget.state());

        ScenarioTargetEntity targetEntity = new ScenarioTargetEntity();
        targetEntity.setId(goalTarget.targetId().value());
        goalTargetEntity.setTarget(targetEntity);

        goalTargetRepository.save(goalTargetEntity);
    }


    private ScenarioGoalEntity createGoal(GamePlayer.Id playerId, PossibilityConsequence.Goal goal) {
        ScenarioGoalEntity entity = new ScenarioGoalEntity();
        entity.setId(StringTools.generate());
        entity.setState(goal.state());

        GamePlayerEntity playerEntity = new GamePlayerEntity();
        playerEntity.setId(playerId.value());
        entity.setPlayer(playerEntity);

        ScenarioStepEntity stepEntity = new ScenarioStepEntity();
        stepEntity.setId(goal.stepId().value());
        entity.setStep(stepEntity);

        return goalRepository.save(entity);
    }

}

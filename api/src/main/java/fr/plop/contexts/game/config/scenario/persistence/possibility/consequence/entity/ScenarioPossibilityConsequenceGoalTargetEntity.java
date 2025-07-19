package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("GOAL_TARGET")
public final class ScenarioPossibilityConsequenceGoalTargetEntity extends
        ScenarioPossibilityConsequenceAbstractEntity {

    @Column(name = "step_id")
    private String stepId;

    @Column(name = "target_id")
    private String targetId;

    @Enumerated(EnumType.STRING)
    private ScenarioGoal.State state;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public ScenarioGoal.State getState() {
        return state;
    }

    public void setState(ScenarioGoal.State state) {
        this.state = state;
    }

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.GoalTarget(new PossibilityConsequence.Id(id), new ScenarioConfig.Step.Id(stepId),
                new ScenarioConfig.Target.Id(targetId), state);
    }

}

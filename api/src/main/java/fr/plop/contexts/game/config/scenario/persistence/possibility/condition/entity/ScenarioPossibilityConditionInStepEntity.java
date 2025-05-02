package fr.plop.contexts.game.config.scenario.persistence.possibility.condition.entity;


import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("IN_STEP")
public final class ScenarioPossibilityConditionInStepEntity
        extends ScenarioPossibilityConditionAbstractEntity {

    @Column(name = "in_step_id")
    private String stepId;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public PossibilityCondition toModel() {
        return new PossibilityCondition.InStep(new PossibilityCondition.Id(id), new ScenarioConfig.Step.Id(stepId));
    }
}

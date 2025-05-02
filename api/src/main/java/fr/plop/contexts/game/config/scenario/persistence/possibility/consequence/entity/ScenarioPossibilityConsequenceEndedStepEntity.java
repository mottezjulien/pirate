package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ENDED_STEP")
public final class ScenarioPossibilityConsequenceEndedStepEntity extends
        ScenarioPossibilityConsequenceAbstractEntity {

    @Column(name = "ended_step_id")
    private String stepId;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.SuccessGoal(new PossibilityConsequence.Id(id), new ScenarioConfig.Step.Id(stepId));
    }

}

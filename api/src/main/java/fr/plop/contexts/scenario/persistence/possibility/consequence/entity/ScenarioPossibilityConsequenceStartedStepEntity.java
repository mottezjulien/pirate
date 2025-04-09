package fr.plop.contexts.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.scenario.domain.model.Scenario;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STARTED_STEP")
public final class ScenarioPossibilityConsequenceStartedStepEntity extends
        ScenarioPossibilityConsequenceAbstractEntity {

    @Column(name = "started_step_id")
    private String stepId;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.StartedStep(new PossibilityConsequence.Id(id), new Scenario.Step.Id(stepId));
    }
}

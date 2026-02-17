package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioState;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("GOAL")
public final class ConsequenceGoalEntity extends
        ConsequenceAbstractEntity {

    @Column(name = "step_id")
    private String stepId;

    @Enumerated(EnumType.STRING)
    private ScenarioState state;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public ScenarioState getState() {
        return state;
    }

    public void setState(ScenarioState state) {
        this.state = state;
    }

    public Consequence toModel() {
        return new Consequence.ScenarioStep(new Consequence.Id(id), new ScenarioConfig.Step.Id(stepId), state);
    }

}

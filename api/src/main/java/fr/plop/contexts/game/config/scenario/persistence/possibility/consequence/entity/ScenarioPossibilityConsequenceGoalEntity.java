package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("GOAL")
public final class ScenarioPossibilityConsequenceGoalEntity extends
        ScenarioPossibilityConsequenceAbstractEntity {

    @Column(name = "step_id")
    private String stepId;

    @Enumerated(EnumType.STRING)
    private ScenarioGoal.State state;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public ScenarioGoal.State getState() {
        return state;
    }

    public void setState(ScenarioGoal.State state) {
        this.state = state;
    }

    public Consequence toModel() {
        return new Consequence.ScenarioStep(new Consequence.Id(id), new ScenarioConfig.Step.Id(stepId), state);
    }

}

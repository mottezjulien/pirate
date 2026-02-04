package fr.plop.contexts.game.config.scenario.persistence.possibility;

import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("STEP")
public class ScenarioPossibilityStepEntity extends ScenarioPossibilityAbstractEntity {

    @ManyToOne
    @JoinColumn(name = "step_id")
    private ScenarioStepEntity step;

    public ScenarioStepEntity getStep() {
        return step;
    }

    public void setStep(ScenarioStepEntity step) {
        this.step = step;
    }
}

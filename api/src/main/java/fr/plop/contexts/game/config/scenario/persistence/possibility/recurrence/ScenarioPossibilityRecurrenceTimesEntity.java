package fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TIMES")
public final class ScenarioPossibilityRecurrenceTimesEntity extends
        ScenarioPossibilityRecurrenceAbstractEntity {

    @Column(name = "times_value")
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int times) {
        this.value = times;
    }

}

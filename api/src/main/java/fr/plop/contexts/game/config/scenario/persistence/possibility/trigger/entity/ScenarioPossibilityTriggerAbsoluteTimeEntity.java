package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.Duration;

@Entity
@DiscriminatorValue(value = "ABSOLUTE_TIME")
public final class ScenarioPossibilityTriggerAbsoluteTimeEntity extends ScenarioPossibilityTriggerAbstractEntity {

    @Column(name = "absolute_time_in_minutes")
    private int minutes;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int absoluteTimeInMinute) {
        this.minutes = absoluteTimeInMinute;
    }

    public PossibilityTrigger toModel() {
        return new PossibilityTrigger.AbsoluteTime(new PossibilityTrigger.Id(id), Duration.ofMinutes(minutes));
    }

}

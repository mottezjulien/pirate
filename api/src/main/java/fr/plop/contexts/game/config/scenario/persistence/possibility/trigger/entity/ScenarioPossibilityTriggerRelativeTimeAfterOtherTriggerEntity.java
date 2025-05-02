package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.Duration;

@Entity
@DiscriminatorValue(value = "RELATIVE_TIME_AFTER_OTHER_TRIGGER")
public final class ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity extends ScenarioPossibilityTriggerAbstractEntity {

    @Column(name = "relative_time_after_other_trigger_in_minutes")
    private int minutes;

    @Column(name = "relative_time_after_other_trigger_id")
    private String otherTriggerId;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int absoluteTimeInMinute) {
        this.minutes = absoluteTimeInMinute;
    }

    public String getOtherTriggerId() {
        return otherTriggerId;
    }

    public void setOtherTriggerId(String otherTriggerId) {
        this.otherTriggerId = otherTriggerId;
    }

    public PossibilityTrigger toModel() {
        return new PossibilityTrigger.RelativeTimeAfterOtherTrigger(new PossibilityTrigger.Id(id),
                new PossibilityTrigger.Id(otherTriggerId),
                Duration.ofMinutes(minutes));
    }
}

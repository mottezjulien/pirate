package fr.plop.contexts.game.config.scenario.persistence.possibility.condition.entity;


import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.Duration;

@Entity
@DiscriminatorValue("RELATIVE_TIME_AFTER_OTHER_TRIGGER")
public final class ScenarioPossibilityConditionRelativeTimeAfterOtherTriggerEntity
        extends ScenarioPossibilityConditionAbstractEntity {

    @Column(name = "relative_time_after_other_trigger_in_minutes")
    private int minutes;

    @Column(name = "relative_time_after_other_trigger_id")
    private String otherTriggerId;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getOtherTriggerId() {
        return otherTriggerId;
    }

    public void setOtherTriggerId(String triggerId) {
        this.otherTriggerId = triggerId;
    }

    public PossibilityCondition toModel() {
        return new PossibilityCondition.RelativeTimeAfterOtherTrigger(
                new PossibilityCondition.Id(id),
                new PossibilityCondition.Id(otherTriggerId),
                Duration.ofMinutes(minutes)
        );
    }
}

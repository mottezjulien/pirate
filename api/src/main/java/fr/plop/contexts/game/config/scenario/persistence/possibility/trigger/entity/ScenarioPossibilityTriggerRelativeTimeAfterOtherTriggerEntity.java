package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.session.time.TimeClick;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "RELATIVE_TIME_AFTER_OTHER_POSSIBILITY")
public final class ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity extends ScenarioPossibilityTriggerAbstractEntity {

    @Column(name = "relative_time_after_other_possibility_in_minutes")
    private int minutes;

    @Column(name = "relative_time_after_other_possibility_id")
    private String otherPossibilityId;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int absoluteTimeInMinute) {
        this.minutes = absoluteTimeInMinute;
    }

    public String getOtherPossibilityId() {
        return otherPossibilityId;
    }

    public void setOtherPossibilityId(String otherTriggerId) {
        this.otherPossibilityId = otherTriggerId;
    }

    public PossibilityTrigger toModel() {
        return new PossibilityTrigger.RelativeTimeAfterOtherPossibility(new PossibilityTrigger.Id(id),
                new Possibility.Id(otherPossibilityId),
                TimeClick.ofMinutes(minutes));
    }
}

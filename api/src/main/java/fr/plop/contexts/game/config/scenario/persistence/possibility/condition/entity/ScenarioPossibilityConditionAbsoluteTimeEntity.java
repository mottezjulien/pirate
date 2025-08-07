package fr.plop.contexts.game.config.scenario.persistence.possibility.condition.entity;


import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import fr.plop.generic.enumerate.BeforeOrAfter;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.Duration;

@Entity
@DiscriminatorValue("ABSOLUTE_TIME")
public final class ScenarioPossibilityConditionAbsoluteTimeEntity
        extends ScenarioPossibilityConditionAbstractEntity {

    @Column(name = "absolute_time_in_minutes")
    private int minutes;

    @Column(name = "before_or_after")
    private BeforeOrAfter beforeOrAfter;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public BeforeOrAfter getBeforeOrAfter() {
        return beforeOrAfter;
    }

    public void setBeforeOrAfter(BeforeOrAfter beforeOrAfter) {
        this.beforeOrAfter = beforeOrAfter;
    }

    public PossibilityCondition toModel() {
        return new PossibilityCondition.AbsoluteTime(new PossibilityCondition.Id(id), Duration.ofMinutes(minutes), beforeOrAfter);
    }
}

package fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ALWAYS")
public final class ScenarioPossibilityRecurrenceAlwaysEntity extends
        ScenarioPossibilityRecurrenceAbstractEntity {

}

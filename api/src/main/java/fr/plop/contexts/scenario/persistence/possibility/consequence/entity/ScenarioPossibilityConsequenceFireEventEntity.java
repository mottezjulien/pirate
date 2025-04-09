package fr.plop.contexts.scenario.persistence.possibility.consequence.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FIRE_EVENT")
public final class ScenarioPossibilityConsequenceFireEventEntity
        extends ScenarioPossibilityConsequenceAbstractEntity {

}

package fr.plop.contexts.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GAME_OVER")
public final class ScenarioPossibilityConsequenceGameOverEntity extends
        ScenarioPossibilityConsequenceAbstractEntity {

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.GameOver(new PossibilityConsequence.Id(id));
    }

}

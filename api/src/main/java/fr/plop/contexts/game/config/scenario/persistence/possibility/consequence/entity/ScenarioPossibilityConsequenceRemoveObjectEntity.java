package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REMOVE_OBJECT")
public final class ScenarioPossibilityConsequenceRemoveObjectEntity
        extends ScenarioPossibilityConsequenceAbstractEntity {

    @Column(name = "remove_object_id")
    private String objetId;

    public String getObjetId() {
        return objetId;
    }

    public void setObjetId(String objetId) {
        this.objetId = objetId;
    }

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.
                RemoveObjet(new PossibilityConsequence.Id(id), objetId);
    }
}

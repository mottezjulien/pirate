package fr.plop.contexts.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADD_OBJECT")
public final class ScenarioPossibilityConsequenceAddObjectEntity
        extends ScenarioPossibilityConsequenceAbstractEntity {

    @Column(name = "add_object_id")
    private String objetId;

    public String getObjetId() {
        return objetId;
    }

    public void setObjetId(String objetId) {
        this.objetId = objetId;
    }

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.
                AddObjet(new PossibilityConsequence.Id(id), objetId);
    }
}

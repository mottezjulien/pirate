package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.consequence.Consequence;
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

    public Consequence toModel() {
        return new Consequence.ObjetRemove(new Consequence.Id(id), objetId);
    }
}

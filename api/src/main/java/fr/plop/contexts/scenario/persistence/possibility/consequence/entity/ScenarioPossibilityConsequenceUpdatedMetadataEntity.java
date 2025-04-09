package fr.plop.contexts.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;
import jakarta.persistence.Column;

public final class ScenarioPossibilityConsequenceUpdatedMetadataEntity
        extends ScenarioPossibilityConsequenceAbstractEntity {

    @Column(name = "updated_metadata_id")
    private String metadataId;

    @Column(name = "updated_metadata_value")
    private float value;

    public String getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.
                UpdatedMetadata(new PossibilityConsequence.Id(id), metadataId, value);
    }
}

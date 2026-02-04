package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.consequence.Consequence;
import jakarta.persistence.Column;

public final class ConsequenceUpdatedMetadataEntity
        extends ConsequenceAbstractEntity {

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

    public Consequence toModel() {
        return new Consequence.
                UpdatedMetadata(new Consequence.Id(id), metadataId, value);
    }
}

package fr.plop.contexts.game.config.scenario.persistence.possibility;

import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioConfigEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("CONFIG")
public class ScenarioPossibilityConfigEntity extends ScenarioPossibilityAbstractEntity {

    @ManyToOne
    @JoinColumn(name = "config_id")
    private ScenarioConfigEntity config;

    public ScenarioConfigEntity getConfig() {
        return config;
    }

    public void setConfig(ScenarioConfigEntity config) {
        this.config = config;
    }
}

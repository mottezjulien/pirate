package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.talk.TalkOptionsEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("TALK_OPTIONS")
public final class ScenarioPossibilityConsequenceTalkOptionsEntity
        extends ScenarioPossibilityConsequenceAbstractEntity {
    @ManyToOne
    @JoinColumn(name = "talk_options_id")
    private TalkOptionsEntity value;

    public TalkOptionsEntity getValue() {
        return value;
    }

    public void setValue(TalkOptionsEntity value) {
        this.value = value;
    }
}

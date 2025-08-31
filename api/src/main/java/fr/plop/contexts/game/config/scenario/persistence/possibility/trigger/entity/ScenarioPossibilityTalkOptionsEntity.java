package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "TALK_OPTIONS")
public class ScenarioPossibilityTalkOptionsEntity extends ScenarioPossibilityTriggerAbstractEntity {

    @Column(name = "talk_options_id")
    private String talkOptionsId;


    public String getTalkOptionsId() {
        return talkOptionsId;
    }

    public void setTalkOptionsId(String talkOptionId) {
        this.talkOptionsId = talkOptionId;
    }
}

package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.persistence.TalkItemEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("TALK")
public final class ConsequenceTalkEntity
        extends ConsequenceAbstractEntity {
    @ManyToOne
    @JoinColumn(name = "talk_id")
    private TalkItemEntity talk;

    public TalkItemEntity getTalk() {
        return talk;
    }

    public void setTalk(TalkItemEntity talk) {
        this.talk = talk;
    }

    public Consequence toModel() {
        TalkItem.Id talkId = new TalkItem.Id(talk.getId());
        return new Consequence.DisplayTalk(new Consequence.Id(id), talkId);
    }

}

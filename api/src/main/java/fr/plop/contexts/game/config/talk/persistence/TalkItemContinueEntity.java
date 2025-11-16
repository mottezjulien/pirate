package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CONTINUE")
public class TalkItemContinueEntity extends TalkItemEntity {

    @Column(name = "next_id")
    private String nextId;

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    @Override
    public TalkItem toModel() {
        return new TalkItem.Continue(new TalkItem.Id(id), value.toModel(), characterModel(), new TalkItem.Id(nextId));
    }

}

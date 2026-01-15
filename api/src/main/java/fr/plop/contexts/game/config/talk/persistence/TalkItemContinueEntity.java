package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CONTINUE")
public class TalkItemContinueEntity extends TalkItemEntity {

    @Column(name = "next_id")
    private String nextId;

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    @Override
    public TalkItem toModel() {
        return TalkItem.continueItem(new TalkItem.Id(id), TalkItemOut.fixed(value.toModel()), characterReference.toModel(), new TalkItem.Id(nextId));
    }

}

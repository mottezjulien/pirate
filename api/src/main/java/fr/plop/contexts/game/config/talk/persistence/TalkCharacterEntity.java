package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_TALK_CHARACTER")
public class TalkCharacterEntity {

    @Id
    private String id;

    private String name;

    public void setId(String id) {
        this.id = id;
    }

    public TalkCharacter toModel() {
        return new TalkCharacter(new TalkCharacter.Id(id), name);
    }

    public static TalkCharacterEntity fromModel(TalkCharacter model) {
        TalkCharacterEntity entity = new TalkCharacterEntity();
        entity.id = model.id().value();
        entity.name = model.name();
        return entity;
    }

}

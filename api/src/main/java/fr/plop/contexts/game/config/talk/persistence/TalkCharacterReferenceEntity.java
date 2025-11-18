package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.subs.image.Image;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_TALK_CHARACTER_REFERENCE")
public class TalkCharacterReferenceEntity {

    @Id
    private String id;

    @Column(name = "_value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "character_id")
    private TalkCharacterEntity character;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private Image.Type imageType;

    @Column(name = "image_value")
    private String imageValue;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public TalkCharacter.Reference toModel() {
        TalkCharacter.Reference.Id referenceId = new TalkCharacter.Reference.Id(id);
        Image image = new Image(imageType, imageValue);
        return new TalkCharacter.Reference(referenceId, character.toModel(), value, image);
    }

    public static TalkCharacterReferenceEntity fromModel(TalkCharacter.Id characterId, TalkCharacter.Reference reference) {
        TalkCharacterReferenceEntity entity = new TalkCharacterReferenceEntity();
        entity.id = reference.id().value();
        entity.value = reference.value();
        TalkCharacterEntity characterEntity = new TalkCharacterEntity();
        characterEntity.setId(characterId.value());
        entity.character = characterEntity;
        entity.imageType = reference.image().type();
        entity.imageValue = reference.image().value();
        return entity;
    }

}

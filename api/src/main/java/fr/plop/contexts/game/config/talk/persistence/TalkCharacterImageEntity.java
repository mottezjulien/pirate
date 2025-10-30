package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.subs.image.Image;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_TALK_CHARACTER_IMAGE")
public class TalkCharacterImageEntity {

    @Id
    private String id;

    private String label;

    @ManyToOne
    @JoinColumn(name = "character_id")
    private TalkCharacterEntity character;

    @Column(name = "asset_url")
    private String assetUrl;

    public TalkCharacter toModel() {
        return new TalkCharacter(character.getName(), new Image(Image.Type.ASSET, assetUrl));
    }

}

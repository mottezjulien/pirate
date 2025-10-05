package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Optional;

@Entity
@Table(name = "TEST2_TALK_ITEM")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("ONE")
public class TalkItemEntity {

    @Id
    protected String id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    protected TalkConfigEntity config;

    @ManyToOne
    @JoinColumn(name = "value_i18n_id")
    protected I18nEntity value;

    @ManyToOne
    @JoinColumn(name = "character_image_id")
    protected TalkCharacterImageEntity characterImage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TalkConfigEntity getConfig() {
        return config;
    }

    public void setConfig(TalkConfigEntity config) {
        this.config = config;
    }

    public I18nEntity getValue() {
        return value;
    }

    public void setValue(I18nEntity value) {
        this.value = value;
    }

    public TalkCharacterImageEntity getCharacterImage() {
        return characterImage;
    }

    public void setCharacterImage(TalkCharacterImageEntity characterImage) {
        this.characterImage = characterImage;
    }

    public TalkItem toModel() {
        return new TalkItem.Simple(new TalkItem.Id(id), value.toModel(), characterModel());
    }

    protected TalkCharacter characterModel() {
        return Optional.ofNullable(characterImage)
                .map(TalkCharacterImageEntity::toModel)
                .orElseGet(TalkCharacter::nobody);
    }
}

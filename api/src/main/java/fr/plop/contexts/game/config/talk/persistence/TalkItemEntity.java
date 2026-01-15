package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "TEST2_TALK_ITEM")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("SIMPLE")
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
    @JoinColumn(name = "character_reference_id")
    protected TalkCharacterReferenceEntity characterReference;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setConfig(TalkConfigEntity config) {
        this.config = config;
    }

    public void setValue(I18nEntity value) {
        this.value = value;
    }


    public void setCharacterReference(TalkCharacterReferenceEntity characterReference) {
        this.characterReference = characterReference;
    }

    public TalkItem toModel() {
        return TalkItem.simple(new TalkItem.Id(id), TalkItemOut.fixed(value.toModel()), characterReference.toModel());
    }

}

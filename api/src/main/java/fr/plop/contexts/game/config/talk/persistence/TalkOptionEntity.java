package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_TALK_OPTION_ITEM")
public class TalkOptionEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "value_i18n_id")
    private I18nEntity value;

    @Column(name = "next_id")
    private String nullableNextId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public I18nEntity getValue() {
        return value;
    }

    public void setValue(I18nEntity label) {
        this.value = label;
    }

    public TalkItem.Options.Option toModel() {
        TalkItem.Options.Option.Id optionalId = new TalkItem.Options.Option.Id(id);
        I18n i18n = value.toModel();
        if(nullableNextId != null) {
            return new TalkItem.Options.Option(optionalId, i18n, new TalkItem.Id(nullableNextId));
        }
        return new TalkItem.Options.Option(optionalId, i18n);

    }
}

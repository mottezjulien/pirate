package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.subs.i18n.persistence.I18nEntity;
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

    public TalkItem.MultipleOptions.Option toModel() {
        return new TalkItem.MultipleOptions.Option(new TalkItem.MultipleOptions.Option.Id(id), value.toModel());
    }
}

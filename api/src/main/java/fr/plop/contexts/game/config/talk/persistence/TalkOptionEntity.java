package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Optional;

@Entity
@Table(name = "TEST2_TALK_OPTION_ITEM")
public class TalkOptionEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "value_i18n_id")
    @Fetch(FetchMode.JOIN) //TODO GOOD idea ?
    private I18nEntity value;

    @Column(name = "next_id")
    private String nullableNextId;

    @Column(name = "_order")
    private int order;

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(I18nEntity label) {
        this.value = label;
    }

    public void setNullableNextId(String nullableNextId) {
        this.nullableNextId = nullableNextId;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public TalkItem.Options.Option toModel() {
        TalkItem.Options.Option.Id optionalId = new TalkItem.Options.Option.Id(id);
        I18n i18n = value.toModel();
        if(nullableNextId != null) {
            return new TalkItem.Options.Option(optionalId, order, i18n, Optional.of(new TalkItem.Id(nullableNextId)));
        }
        return new TalkItem.Options.Option(optionalId, order, i18n, Optional.empty());
    }
}

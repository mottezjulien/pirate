package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
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

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity nullableCondition;

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

    public void setNullableCondition(ConditionEntity nullableCondition) {
        this.nullableCondition = nullableCondition;
    }

    public TalkItemNext.Options.Option toModel() {
        TalkItemNext.Options.Option.Id optionalId = new TalkItemNext.Options.Option.Id(id);
        I18n i18n = value.toModel();
        Optional<TalkItem.Id> optNextId = Optional.ofNullable(nullableNextId).map(TalkItem.Id::new);
        Optional<Condition> optCondition = Optional.ofNullable(nullableCondition).map(ConditionEntity::toModel);
        return new TalkItemNext.Options.Option(optionalId, order, i18n, optNextId, optCondition);
    }
}

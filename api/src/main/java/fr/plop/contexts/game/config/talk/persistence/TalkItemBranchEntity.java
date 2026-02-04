package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.condition.persistence.ConditionEntity;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_TALK_ITEM_BRANCH")
public class TalkItemBranchEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "talk_item_id")
    private TalkItemEntity talkItem;

    @ManyToOne
    @JoinColumn(name = "value_i18n_id")
    private I18nEntity value;

    @Column(name = "_order")
    private int order;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    private ConditionEntity condition;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TalkItemEntity getTalkItem() {
        return talkItem;
    }

    public void setTalkItem(TalkItemEntity talkItem) {
        this.talkItem = talkItem;
    }

    public I18nEntity getValue() {
        return value;
    }

    public void setValue(I18nEntity value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ConditionEntity getCondition() {
        return condition;
    }

    public void setCondition(ConditionEntity condition) {
        this.condition = condition;
    }
}

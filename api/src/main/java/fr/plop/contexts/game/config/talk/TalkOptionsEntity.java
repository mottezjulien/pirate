package fr.plop.contexts.game.config.talk;

import fr.plop.contexts.i18n.persistence.I18nEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_TALK_OPTIONS")
public class TalkOptionsEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "label_i18n_id")
    private I18nEntity label;

    @ManyToMany
    @JoinTable(name = "TEST2_TALK_JOIN_OPTION_ITEM",
            joinColumns = @JoinColumn(name = "options_id"),
            inverseJoinColumns = @JoinColumn(name = "option_item_id"))
    private Set<TalkOptionItemEntity> items = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public I18nEntity getLabel() {
        return label;
    }

    public void setLabel(I18nEntity label) {
        this.label = label;
    }

    public Set<TalkOptionItemEntity> getItems() {
        return items;
    }

    public void setItems(Set<TalkOptionItemEntity> items) {
        this.items = items;
    }

}

package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_TALK_CONFIG")
public class TalkConfigEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "config")
    private Set<TalkItemEntity> items = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<TalkItemEntity> getItems() {
        return items;
    }

    public void setItems(Set<TalkItemEntity> items) {
        this.items = items;
    }

    public TalkConfig toModel() {
        return new TalkConfig(new TalkConfig.Id(id), items.stream()
                .map(TalkItemEntity::toModel)
                .toList());
    }
}
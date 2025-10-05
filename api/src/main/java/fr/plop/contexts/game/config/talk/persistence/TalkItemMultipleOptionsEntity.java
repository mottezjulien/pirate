package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("MULTIPLE_OPTIONS")
public class TalkItemMultipleOptionsEntity extends TalkItemEntity {

    @ManyToMany
    @JoinTable(name = "TEST2_TALK_MULTIPLE_OPTIONS_JOIN",
            joinColumns = @JoinColumn(name = "multiple_options_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id"))
    private Set<TalkOptionEntity> options = new HashSet<>();

    public Set<TalkOptionEntity> getOptions() {
        return options;
    }

    public void setOptions(Set<TalkOptionEntity> items) {
        this.options = items;
    }

    @Override
    public TalkItem toModel() {
        return new TalkItem.MultipleOptions(
                new TalkItem.Id(id), value.toModel(), characterModel(),
                options.stream().map(TalkOptionEntity::toModel).toList());
    }
}

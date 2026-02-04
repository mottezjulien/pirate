package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("MULTIPLE_OPTIONS")
public class TalkItemMultipleOptionsEntity extends TalkItemEntity {

    @ManyToMany
    @JoinTable(name = "LO_TALK_MULTIPLE_OPTIONS_JOIN",
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
    protected TalkItemNext toModelNext() {
        return new TalkItemNext.Options(options.stream().map(TalkOptionEntity::toModel).toList());
    }
}

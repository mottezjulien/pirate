package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("MULTIPLE_OPTIONS")
public class TalkItemMultipleOptionsEntity extends TalkItemEntity {

    @ManyToMany
    @JoinTable(name = "TEST2_TALK_MULTIPLE_OPTIONS_JOIN",
            joinColumns = @JoinColumn(name = "multiple_options_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id"))
    @Fetch(FetchMode.JOIN) //TODO GOOD idea ?
    private Set<TalkOptionEntity> options = new HashSet<>();

    public Set<TalkOptionEntity> getOptions() {
        return options;
    }

    public void setOptions(Set<TalkOptionEntity> items) {
        this.options = items;
    }

    @Override
    public TalkItem toModel() {
        return TalkItem.options(new TalkItem.Id(id), TalkItemOut.fixed(value.toModel()), characterReference.toModel(),
                options.stream().map(TalkOptionEntity::toModel).toList());
    }
}

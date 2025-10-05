package fr.plop.contexts.game.config.talk.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEST2_TALK_CHARACTER")
public class TalkCharacterEntity {

    @Id
    private String id;

    private String name;

    @OneToMany(mappedBy = "character")
    private Set<TalkCharacterImageEntity> images = new HashSet<>();

    public String getName() {
        return name;
    }
}

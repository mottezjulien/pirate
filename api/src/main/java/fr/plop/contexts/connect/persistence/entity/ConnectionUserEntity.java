package fr.plop.contexts.connect.persistence.entity;


import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.subs.i18n.domain.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "TEST2_CONNECTION_USER")
public class ConnectionUserEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private Language language;

    @OneToMany(mappedBy = "user")
    private Set<GamePlayerEntity> players = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Set<GamePlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(Set<GamePlayerEntity> players) {
        this.players = players;
    }

    public ConnectUser toModel(GamePlayer nullablePlayer) {
        return new ConnectUser(new ConnectUser.Id(id), language, Optional.ofNullable(nullablePlayer));
    }

}

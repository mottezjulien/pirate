package fr.plop.contexts.connect.persistence;


import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.i18n.domain.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Optional;

@Entity
@Table(name = "TEST2_CONNECTION_USER")
public class ConnectionUserEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String surname;

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

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public ConnectUser toModel(GamePlayer nullablePlayer) {
        return new ConnectUser(new ConnectUser.Id(id), language, Optional.ofNullable(nullablePlayer));
    }



}

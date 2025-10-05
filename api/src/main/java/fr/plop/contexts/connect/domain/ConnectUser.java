package fr.plop.contexts.connect.domain;


import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.subs.i18n.domain.Language;

import java.util.Optional;

//TODO un user ne peut jouer uniquement à une partie en cours ???
public record ConnectUser(Id id, Language language, Optional<GamePlayer> player) {

    public record Id(String value) {

    }

    public ConnectUser(Id id) {
        this(id, Language.byDefault(), Optional.empty());
    }

}

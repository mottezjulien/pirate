package fr.plop.contexts.connect.domain;


import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.subs.i18n.domain.Language;

import java.util.Optional;

public record ConnectUser(Id id, Language language, Optional<GamePlayer.Id> playerId) {

    public record Id(String value) {

    }

    public ConnectUser(Id id) {
        this(id, Language.byDefault(), Optional.empty());
    }


    public ConnectUser withPlayerId(GamePlayer.Id playerId) {
        return new ConnectUser(id, language, Optional.of(playerId));
    }

}

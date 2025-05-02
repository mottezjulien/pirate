package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;

import java.util.List;

public record GamePlayer(Atom atom, ConnectUser.Id userId, List<BoardSpace.Id> positions) {

    public record Id(String value) {

    }

    public record Atom(Id id, GameSession.Id sessionId) {

    }

    public Id id() {
        return atom.id();
    }

}

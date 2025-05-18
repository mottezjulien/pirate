package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.generic.tools.StringTools;

import java.util.List;

public record GamePlayer(Atom atom, ConnectUser.Id userId, List<BoardSpace.Id> spaceIds) {

    public record Id(String value) {

        public Id() {
            this(StringTools.generate());
        }

    }

    public record Atom(Id id, GameSession.Id sessionId) {

    }

    public Id id() {
        return atom.id();
    }

}

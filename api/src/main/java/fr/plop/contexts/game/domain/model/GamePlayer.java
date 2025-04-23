package fr.plop.contexts.game.domain.model;

import fr.plop.contexts.connect.domain.ConnectUser;

public record GamePlayer(Atom atom, ConnectUser.Id userId) {

    public record Id(String value) {

    }

    public record Atom(Id id, Game.Id gameId) {

    }

    public Id id() {
        return atom.id();
    }



}

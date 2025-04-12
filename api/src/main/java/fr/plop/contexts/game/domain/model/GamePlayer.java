package fr.plop.contexts.game.domain.model;

public record GamePlayer(Atom atom) {

    public record Id(String value) {

    }

    public record Atom(Id id, Game.Id gameId) {

    }


}

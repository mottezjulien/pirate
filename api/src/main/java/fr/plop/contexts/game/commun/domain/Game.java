package fr.plop.contexts.game.commun.domain;

public record Game(Id id, GameProject.Id projectId, Version version) {

    public record Id(String value) {

    }

    public record Version(String value) {

    }

}

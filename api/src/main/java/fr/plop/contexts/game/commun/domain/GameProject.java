package fr.plop.contexts.game.commun.domain;

import java.util.Optional;

public record GameProject(Id id, Code code, Optional<Game.Id> activeId) {

    public record Id(String value) {

    }

    public record Code(String value) {

    }

}

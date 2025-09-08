package fr.plop.contexts.game.config.board.domain.model;


import fr.plop.generic.position.Point;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.stream.Stream;

public record BoardConfig(Id id, List<BoardSpace> spaces) {


    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public BoardConfig() {
        this(List.of());
    }

    public BoardConfig(List<BoardSpace> spaces) {
        this(new Id(), spaces);
    }

    public Id id() {
        return id;
    }

    public Stream<BoardSpace> spacesByPoint(Point position) {
        return spaces.stream()
                .filter(space -> space.in(position));
    }

}

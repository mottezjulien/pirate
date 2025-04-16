package fr.plop.contexts.board.domain.model;


import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.stream.Stream;

public class Board {



    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    private final Id id;
    private final List<BoardSpace> spaces;

    public Board() {
        this(List.of());
    }

    public Board(List<BoardSpace> spaces) {
        this(new Id(), spaces);
    }

    public Board(Id id, List<BoardSpace> spaces) {
        this.id = id;
        this.spaces = spaces;
    }


    public Id id() {
        return id;
    }

    public Stream<BoardSpace> spaces() {
        return spaces.stream();
    }

    public BoardSpace space(int index) {
        return spaces.get(index);
    }

    /*

    public List<BoardSpace> spaces(GamePlayer inGamePlayer) {
        Optional<BoardSpace.Point> optPoint = inGamePlayer.position();
        return optPoint.stream()
                .flatMap(point -> spaces.stream().filter(space -> space.is(point)))
                .toList();
    }*/

}

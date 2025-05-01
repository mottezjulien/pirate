package fr.plop.contexts.board.domain.model;


import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.generic.tools.StringTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Board {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    private final Id id;
    private final List<BoardSpace> spaces;

    private final Map<GamePlayer.Id, List<BoardSpace>> positions = new HashMap<>();

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

    public Stream<BoardSpace> spacesByPosition(BoardSpace.Point position) {
        return spaces()
                .filter(space -> space.in(position));
    }

    public List<BoardSpace> spacesByPlayerId(GamePlayer.Id playerId) {
        return positions.getOrDefault(playerId, List.of());
    }

    public void putPositions(GamePlayer.Id playerId, List<BoardSpace> current) {
        positions.put(playerId, current);
    }

}

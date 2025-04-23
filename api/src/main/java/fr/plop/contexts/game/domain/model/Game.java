package fr.plop.contexts.game.domain.model;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.scenario.domain.model.Scenario;

import java.util.List;

public record Game(Atom atom, State state, List<GamePlayer> players, Scenario scenario, Board board) {

    public record Id(String value) {

    }

    public record Atom(Id id, String label) {

    }

    public enum State {
        INIT, PLAYING, OVER, PAUSED
    }

    public Id id() {
        return atom.id();
    }

    public GamePlayer playerByUserId(ConnectUser.Id userId) throws GameException {
        return players.stream()
                .filter(player -> player.userId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new GameException(GameException.Type.PLAYER_NOT_FOUND));
    }



}

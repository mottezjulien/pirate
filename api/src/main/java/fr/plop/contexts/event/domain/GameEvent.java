package fr.plop.contexts.event.domain;

import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;

public interface GameEvent {

    Game.Id gameId();

    GamePlayer.Id playerId();

    record GoIn(Game.Id gameId, GamePlayer.Id playerId, BoardSpace space) implements GameEvent {

    }

    record GoOut(Game.Id gameId, GamePlayer.Id playerId, BoardSpace space) implements GameEvent {

    }




}

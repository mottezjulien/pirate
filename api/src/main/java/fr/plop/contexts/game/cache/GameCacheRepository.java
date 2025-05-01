package fr.plop.contexts.game.cache;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.board.persistence.adapter.BoardGroupAdapterRepository;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.game.persistence.GamePlayerRepository;
import fr.plop.contexts.game.persistence.GameRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GameCacheRepository {

    //TODO Anneau/Repo unique, mon précieux
    //TODO clear cache

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final BoardGroupAdapterRepository boardRepository;

    private final List<Game> games = new ArrayList<>();

    public GameCacheRepository(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, BoardGroupAdapterRepository boardRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.boardRepository = boardRepository;
    }

    public Optional<Game> findById(Game.Id gameId) {
        return games.stream()
                .filter(game -> game.id().equals(gameId))
                .findFirst();
    }

    public void savePosition(Game.Id gameId, Board board, GamePlayer.Id playerId) {
        Optional<Game> optGame = findById(gameId);
        optGame.ifPresent(game -> {
            boardRepository.savePosition(board, playerId);
            games.remove(game);
            games.add(game.copyWithBoard(board));
        });
    }
}

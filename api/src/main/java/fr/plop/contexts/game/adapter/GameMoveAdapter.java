package fr.plop.contexts.game.adapter;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.cache.GameCacheRepository;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.game.domain.usecase.GameMoveUseCase;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class GameMoveAdapter implements GameMoveUseCase.OutPort {

    private final GameCacheRepository gameCacheRepository;

    public GameMoveAdapter(GameCacheRepository gameCacheRepository) {
        this.gameCacheRepository = gameCacheRepository;
    }

    @Override
    public Optional<GamePlayer> findByGameIdAndUserId(Game.Id gameId, ConnectUser.Id userId) {
        Optional<Game> optGame = gameCacheRepository.findById(gameId);
        return optGame.flatMap(game -> game.playerByUserId(userId));
    }

    @Override
    public Optional<Board> findByGameId(Game.Id gameId) {
        Optional<Game> optGame = gameCacheRepository.findById(gameId);
        return optGame.map(Game::board);
    }

    @Override
    public void savePosition(Game.Id gameId, Board board, GamePlayer.Id playerId) {
        gameCacheRepository.savePosition(gameId, board, playerId);
    }


}

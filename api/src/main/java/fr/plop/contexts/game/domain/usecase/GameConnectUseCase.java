package fr.plop.contexts.game.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.game.domain.model.GamePlayer;

import java.util.Optional;

public class GameConnectUseCase {

    public interface OutPort {
        Optional<GamePlayer> findByUserId(ConnectUser.Id id);
    }

    private final OutPort port;

    public GameConnectUseCase(OutPort port) {
        this.port = port;
    }

    public GamePlayer.Atom findByUserId(ConnectUser.Id id) throws GameException {
        GamePlayer player = port.findByUserId(id)
                .orElseThrow(() -> new GameException(GameException.Type.PLAYER_NOT_FOUND));
        return player.atom();
    }

}

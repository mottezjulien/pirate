package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

import java.util.Optional;

public class GameSessionCreateUseCase {



    public interface Port {
        Optional<GameSessionContext> findCurrentGameSession(ConnectUser.Id userId);

        Optional<Template> findTemplateByCode(Template.Code code);

        GameSession create(Template template);

        GamePlayer.Id insertUserId(GameSession.Id gameId, ConnectUser.Id userId);

    }

    private final Port port;
    private final GameConfigCache cache;

    public GameSessionCreateUseCase(Port port, GameConfigCache cache) {
        this.port = port;
        this.cache = cache;
    }

    public GameSessionContext apply(Template.Code code, ConnectUser.Id userId) throws GameException {
        Optional<GameSessionContext> findInGame = port.findCurrentGameSession(userId);
        if (findInGame.isPresent()) {
            return findInGame.get();
        }
        return createNewGame(code, userId);
    }


    private GameSessionContext createNewGame(Template.Code code, ConnectUser.Id userId) throws GameException {
        Template template = port.findTemplateByCode(code)
                .orElseThrow(() -> new GameException(GameException.Type.TEMPLATE_NOT_FOUND));

        GameSession session = port.create(template);
        GamePlayer.Id playerId = port.insertUserId(session.id(), userId);

        session = session.insertPlayerId(playerId);
        cache.insert(session);

        return new GameSessionContext(session.id(), playerId);
    }

}

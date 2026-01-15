package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.user.User;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

import java.util.Optional;

public class GameSessionUseCase {



    public interface Port {
        Optional<GameSessionContext> findCurrentGameSession(User.Id userId);

        Optional<Template> findTemplateById(Template.Id id);

        GameSession create(Template template);

        GamePlayer.Id insertUserId(GameSession.Id gameId, User.Id userId);

    }

    private final Port port;
    private final GameConfigCache cache;

    public GameSessionUseCase(Port port, GameConfigCache cache) {
        this.port = port;
        this.cache = cache;
    }

    public Optional<GameSessionContext> find(User.Id userId) {
        return port.findCurrentGameSession(userId);
    }

    public GameSessionContext create(Template.Id templateId, User.Id userId) throws GameException {
        Optional<GameSessionContext> findInGame = find(userId);
        if (findInGame.isPresent()) {
            return findInGame.get();
        }
        return createNewGame(templateId, userId);
    }

    private GameSessionContext createNewGame(Template.Id templateId, User.Id userId) throws GameException {
        Template template = port.findTemplateById(templateId)
                .orElseThrow(() -> new GameException(GameException.Type.TEMPLATE_NOT_FOUND));

        GameSession session = port.create(template);
        GamePlayer.Id playerId = port.insertUserId(session.id(), userId);

        session = session.insertPlayerId(playerId);
        cache.insert(session);

        return new GameSessionContext(session.id(), playerId);
    }

}

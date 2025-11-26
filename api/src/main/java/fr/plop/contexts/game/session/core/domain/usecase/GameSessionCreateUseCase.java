package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;

import java.util.Optional;

public class GameSessionCreateUseCase {



    public interface Port {
        Optional<GameSession.Atom> findCurrentGameSession(ConnectUser.Id userId);

        Optional<Template> findTemplateByCode(Template.Code code);

        GameSession create(Template template);

        GamePlayer.Id insert(GameSession.Id gameId, ConnectUser.Id userId);

        void insertScenarioSessionPlayer(GamePlayer.Id playerId, ScenarioSessionPlayer sessionPlayer);
    }

    private final Port port;
    private final GameConfigCache cache;

    public GameSessionCreateUseCase(Port port, GameConfigCache cache) {
        this.port = port;
        this.cache = cache;
    }

    public GameSession.Atom apply(Template.Code code, ConnectUser.Id userId) throws GameException {
        Optional<GameSession.Atom> findInGame = port.findCurrentGameSession(userId);
        if (findInGame.isPresent()) {
            return findInGame.get();
        }
        return createNewGame(code, userId);
    }


    private GameSession.Atom createNewGame(Template.Code code, ConnectUser.Id userId) throws GameException {
        Template template = port.findTemplateByCode(code)
                .orElseThrow(() -> new GameException(GameException.Type.TEMPLATE_NOT_FOUND));

        GameSession session = port.create(template);
        GamePlayer.Id playerId = port.insert(session.id(), userId);

        ScenarioSessionPlayer sessionPlayer = session.scenarioPlayer(playerId);
        port.insertScenarioSessionPlayer(playerId, sessionPlayer);
        cache.insert(session);
        return session.atom();
    }

}

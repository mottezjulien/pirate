package fr.plop.contexts.game.instance.core.domain.usecase;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.user.User;

import java.util.Optional;
import java.util.stream.Stream;

public class GameInstanceUseCase {

    public interface Port {
        Stream<GameInstance.Atom> findOpenedByUserId(User.Id userId);

        Optional<GameInstance.Atom> findById(GameInstance.Id instanceId);

        Optional<Template> findTemplateById(Template.Id id);

        GameInstance create(Template template);

        GamePlayer insertUserId(GameInstance.Id gameId, User.Id userId);


    }

    private final Port port;
    private final GameConfigCache cache;

    public GameInstanceUseCase(Port port, GameConfigCache cache) {
        this.port = port;
        this.cache = cache;
    }

    public Optional<GameInstance.Atom> current(User.Id userId) {
        return port.findOpenedByUserId(userId).findFirst();
    }
    public Optional<GameInstance.Atom> find(GameInstance.Id instanceId) {
        return port.findById(instanceId);
    }

    public GameInstance.Atom create(Template.Id templateId, User.Id userId) throws GameInstanceException {
        Optional<GameInstance.Atom> findInGame = current(userId);
        if (findInGame.isPresent()) {
            return findInGame.get();
        }
        return createNewGame(templateId, userId);
    }

    private GameInstance.Atom createNewGame(Template.Id templateId, User.Id userId) throws GameInstanceException {
        Template template = port.findTemplateById(templateId)
                .orElseThrow(() -> new GameInstanceException(GameInstanceException.Type.TEMPLATE_NOT_FOUND));

        GameInstance instance = port.create(template);
        GamePlayer player = port.insertUserId(instance.id(), userId);

        instance = instance.insertPlayer(player);
        cache.insert(instance);

        return instance.atom();
    }

}

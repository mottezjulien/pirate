package fr.plop.contexts.game.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.template.domain.model.Template;

import java.util.Optional;

public class GameCreateUseCase {

    public interface DataOutput {
        Optional<Template> findTemplateByCode(Template.Code code);
        Game.Atom create(Template template);
        void insert(Game.Id gameId, ConnectUser.Id userId);
    }

    private final DataOutput dataOutput;

    public GameCreateUseCase(DataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    public Game.Atom apply(Template.Code code, ConnectUser.Id userId) throws GameException {
        Template template = dataOutput.findTemplateByCode(code)
                .orElseThrow(() -> new GameException(GameException.Type.TEMPLATE_NOT_FOUND));
        Game.Atom game = dataOutput.create(template);
        dataOutput.insert(game.id(), userId);
        return game;
    }

}

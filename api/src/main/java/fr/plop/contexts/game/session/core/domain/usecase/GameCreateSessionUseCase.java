package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

import java.util.List;
import java.util.Optional;

public class GameCreateSessionUseCase {

    public interface DataOutput {
        Optional<Template> findTemplateByCode(Template.Code code);
        GameSession create(Template template);
        GamePlayer.Id insert(GameSession.Id gameId, ConnectUser.Id userId);
        void insertGoal(ScenarioGoal goal);

    }

    private final DataOutput dataOutput;

    public GameCreateSessionUseCase(DataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    public GameSession.Atom apply(Template.Code code, ConnectUser.Id userId) throws GameException {
        Template template = dataOutput.findTemplateByCode(code)
                .orElseThrow(() -> new GameException(GameException.Type.TEMPLATE_NOT_FOUND));

        GameSession session = dataOutput.create(template);



        GamePlayer.Id playerId = dataOutput.insert(session.id(), userId);

        session.init(playerId);
        session.goals(playerId)
                .forEach(dataOutput::insertGoal);

        return session.atom();
    }

}

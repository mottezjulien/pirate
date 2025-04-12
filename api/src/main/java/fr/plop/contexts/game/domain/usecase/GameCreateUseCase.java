package fr.plop.contexts.game.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.scenario.domain.model.Scenario;
import fr.plop.contexts.template.domain.model.Template;

import java.util.Optional;
import java.util.stream.Stream;

public class GameCreateUseCase {

    public interface DataOutput {
        Optional<Template> findTemplateByCode(Template.Code code);
        Game create(Template template);
        GamePlayer.Id insert(Game.Id gameId, ConnectUser.Id userId);
        void insertGoal(GamePlayer.Id playerId, Scenario.Step.Id id);
    }

    private final DataOutput dataOutput;

    public GameCreateUseCase(DataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    public Game.Atom apply(Template.Code code, ConnectUser.Id userId) throws GameException {
        Template template = dataOutput.findTemplateByCode(code)
                .orElseThrow(() -> new GameException(GameException.Type.TEMPLATE_NOT_FOUND));
        Game game = dataOutput.create(template);
        GamePlayer.Id playerId = dataOutput.insert(game.id(), userId);

        initScenarioGoal(game, playerId);
        return game.atom();
    }

    private void initScenarioGoal(Game game, GamePlayer.Id playerId) {
        Scenario scenario = game.scenario();
        Stream<Scenario.Step> steps = scenario.firstSteps();
        steps.forEach(step -> dataOutput.insertGoal(playerId, step.id()));
    }

}

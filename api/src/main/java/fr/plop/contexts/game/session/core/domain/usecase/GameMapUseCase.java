package fr.plop.contexts.game.session.core.domain.usecase;


import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

import java.util.List;

//TODO MOVE ??
public class GameMapUseCase {

    public interface OutputPort {
        List<Map> findMaps(GameSession.Id sessionId, List<ScenarioConfig.Step.Id> ids);
    }

    public record Response(List<Map> maps) {

    }

    private final OutputPort output;


    public GameMapUseCase(OutputPort output) {
        this.output = output;
    }


    public Response apply(GameSession.Id sessionId, GamePlayer player) {
        List<Map> maps = output.findMaps(sessionId, player.stepActiveIds());
        return new Response(maps);
    }


}

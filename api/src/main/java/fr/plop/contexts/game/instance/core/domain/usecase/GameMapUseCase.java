package fr.plop.contexts.game.instance.core.domain.usecase;


import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;

import java.util.List;
/*
//TODO MOVE ??
public class GameMapUseCase {

    public interface OutputPort {
        List<MapItem> findMaps(GameInstance.Id instanceId, List<ScenarioConfig.Step.Id> ids);
    }

    public record Response(List<MapItem> maps) {

    }

    private final OutputPort output;


    public GameMapUseCase(OutputPort output) {
        this.output = output;
    }


    public Response apply(GameInstance.Id instanceId, GamePlayer player) {
        List<MapItem> maps = output.findMaps(instanceId, player.activeStepIds());
        return new Response(maps);
    }


}*/

package fr.plop.contexts.game.instance.core.domain.usecase;

import fr.plop.contexts.connect.domain.ConnectAuthGameInstance;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.scenario.domain.GameInstanceScenarioGoalPort;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioState;
import fr.plop.contexts.game.instance.time.GameInstanceTimer;

import java.util.Optional;

public class GameInstanceStartUseCase {

    public interface Port {
        Optional<GameInstance.Atom> find(GameInstance.Id id);
        void active(GameInstance.Id instanceId);
        void active(GamePlayer.Id id);
    }

    private final Port port;

    private final GameConfigCache cache;
    private final GameInstanceTimer timer;

    private final GameInstanceScenarioGoalPort scenarioGoalPort;


    public GameInstanceStartUseCase(Port port, GameConfigCache cache, GameInstanceTimer timer, GameInstanceScenarioGoalPort scenarioGoalPort) {
        this.port = port;
        this.cache = cache;
        this.timer = timer;
        this.scenarioGoalPort = scenarioGoalPort;
    }

    public void apply(ConnectAuthGameInstance authGameInstance) throws GameInstanceException {

        final GameInstanceContext context = authGameInstance.context();
        Optional<GameInstance.Atom> opt = port.find(context.instanceId());

        final GameInstance.Atom instance = opt
                .orElseThrow(() -> new GameInstanceException(GameInstanceException.Type.INSTANCE_NOT_FOUND));
        if(!instance.state().equals(GameInstance.State.INIT)) {
            throw new GameInstanceException(GameInstanceException.Type.INSTANCE_INVALID);
        }
        Optional<GamePlayer> optPlayer = instance.byPlayerId(context.playerId());
        final GamePlayer player = optPlayer
                .orElseThrow(() -> new GameInstanceException(GameInstanceException.Type.PLAYER_NOT_FOUND));
        if(!player.state().equals(GamePlayer.State.INIT)) {
            throw new GameInstanceException(GameInstanceException.Type.PLAYER_INVALID);
        }

        timer.start(context.instanceId());

        port.active(context.instanceId());

        port.active(context.playerId());

        ScenarioConfig scenario = cache.scenario(context.instanceId());
        ScenarioConfig.Step step = scenario.firstStep();
        scenarioGoalPort.saveStep(context, step.id(), ScenarioState.ACTIVE);
        
        step.targets().forEach(target ->
            scenarioGoalPort.saveTarget(context.playerId(), target.id(), ScenarioState.ACTIVE)
        );

    }

}

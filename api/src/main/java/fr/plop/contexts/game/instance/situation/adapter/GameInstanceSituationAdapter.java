package fr.plop.contexts.game.instance.situation.adapter;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.port.GamePlayerGetPort;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryUseCase;
import fr.plop.contexts.game.instance.scenario.domain.GameInstanceScenarioGoalPort;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.contexts.game.instance.situation.domain.port.GameInstanceSituationGetPort;
import fr.plop.contexts.game.instance.talk.GameInstanceTalkUseCase;
import fr.plop.contexts.game.instance.time.GameInstanceTimerGet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class GameInstanceSituationAdapter implements GameInstanceSituationGetPort {

    private final GamePlayerGetPort gamePlayerGetPort;
    private final GameInstanceScenarioGoalPort scenarioGoalPort;
    private final GameInstanceTalkUseCase talkUseCase;
    private final GameInstanceTimerGet timerProvider;
    private final GameInstanceInventoryUseCase.Port inventoryPort;


    public GameInstanceSituationAdapter(GamePlayerGetPort gamePlayerGetPort, GameInstanceScenarioGoalPort scenarioGoalPort, GameInstanceTalkUseCase talkUseCase, GameInstanceTimerGet timerProvider, GameInstanceInventoryUseCase.Port inventoryPort) {
        this.gamePlayerGetPort = gamePlayerGetPort;
        this.scenarioGoalPort = scenarioGoalPort;
        this.talkUseCase = talkUseCase;
        this.timerProvider = timerProvider;
        this.inventoryPort = inventoryPort;
    }


    @Override
    public GameInstanceSituation get(GameInstanceContext context) {
        GameInstanceSituation.Time time = new GameInstanceSituation.Time(timerProvider.current(context.instanceId()));
        return new GameInstanceSituation(board(context.playerId()), scenario(context.playerId()), talk(context.playerId()), inventory(context.playerId()), time);
    }

    private GameInstanceSituation.Board board(GamePlayer.Id playerId) {
        List<BoardSpace.Id> spaceIds = gamePlayerGetPort.findSpaceIdsByPlayerId(playerId);
        return new GameInstanceSituation.Board(spaceIds);

    }

    private GameInstanceSituation.Scenario scenario(GamePlayer.Id playerId) {
        List<ScenarioConfig.Step.Id> stepIds = scenarioGoalPort.findActiveSteps(playerId);
        List<ScenarioConfig.Target.Id> targetIds = scenarioGoalPort.findActiveTargets(playerId);
        return new GameInstanceSituation.Scenario(stepIds, targetIds);
    }
    private GameInstanceSituation.Talk talk(GamePlayer.Id playerId) {
        Map<TalkCharacter.Id, List<TalkItem.Id>> read = talkUseCase.read(playerId);
        return new GameInstanceSituation.Talk(read);
    }

    private GameInstanceSituation.Inventory inventory(GamePlayer.Id playerId) {
        Stream<GameInstanceInventoryUseCase.ItemRaw> rawStream = inventoryPort.inventory(playerId);
        return new GameInstanceSituation.Inventory(rawStream.map(GameInstanceInventoryUseCase.ItemRaw::configId).toList());
    }


}

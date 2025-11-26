package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionPushAdapter;
import fr.plop.contexts.game.session.event.adapter.action.GameEventActionScenarioAdapter;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import java.util.List;
import java.util.stream.Stream;

public class GameEventBroadCastIntern implements GameEventBroadCast {
    public interface Port {
        Stream<Possibility> findPossibilities(GameContext context);
        void doGameOver(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.SessionEnd consequence);
        void saveAction(GamePlayer.Id playerId, Possibility.Id possibilityId, GameSessionTimeUnit timeClick);

        List<GameAction> findActions(GamePlayer.Id id);
        GameSessionTimeUnit current(GameSession.Id sessionId);
    }

    private final Port port;
    private final GameSessionSituationGetPort situationGetPort;
    private final GameEventActionPushAdapter pushAdapter;
    private final GameEventActionScenarioAdapter scenarioAdapter;

    public GameEventBroadCastIntern(Port port, GameSessionSituationGetPort situationGetPort, GameEventActionPushAdapter pushAdapter, GameEventActionScenarioAdapter scenarioAdapter) {
        this.port = port;
        this.situationGetPort = situationGetPort;
        this.pushAdapter = pushAdapter;
        this.scenarioAdapter = scenarioAdapter;
    }

    //TODO ASYNC
    @Override
    public void fire(GameEvent event, GameContext context) {
        //TODO Game in cache ?? In repo cache ?? Utile ??
        Stream<Possibility> possibilities = select(event, context);
        possibilities.forEach(possibility -> doAction(event, context, possibility));
    }

    private Stream<Possibility> select(GameEvent event, GameContext context) {
        List<GameAction> previousActions = port.findActions(context.playerId());
        GameSessionSituation situation = situationGetPort.get(context);
        return port.findPossibilities(context)
                .filter(possibility -> possibility.accept(event, previousActions, situation));
    }

    private void doAction(GameEvent event, GameContext context, Possibility possibility) {
        possibility.consequences()
                .forEach(consequence -> _do(event, context, consequence));
        port.saveAction(context.playerId(), possibility.id(), port.current(context.sessionId()));
    }

    private void _do(GameEvent event, GameContext context, Consequence consequence) {
        switch (consequence) {
            case Consequence.ScenarioStep goal -> {
                scenarioAdapter.updateStateOrCreateGoalStep(context.playerId(), goal);
                if (goal.state() == ScenarioSessionState.ACTIVE) {
                    this.fire(new GameEvent.GoalActive(goal.stepId()), context);
                }
            }
            case Consequence.ScenarioTarget goalTarget -> scenarioAdapter.updateStateOrCreateGoalTarget(context.playerId(), goalTarget);

            case Consequence.DisplayMessage message -> pushAdapter.message(context.sessionId(), context.playerId(), message.value());
            case Consequence.DisplayTalk talk -> pushAdapter.talk(context.sessionId(), context.playerId(), talk.talkId());

            case Consequence.SessionEnd gameOver -> port.doGameOver(context.sessionId(), context.playerId(), gameOver);

            //TODO
            case Consequence.ObjetAdd addObjet -> { }
            case Consequence.ObjetRemove removeObjet -> { }
            case Consequence.UpdatedMetadata updatedMetadata -> { }
        }

    }


}

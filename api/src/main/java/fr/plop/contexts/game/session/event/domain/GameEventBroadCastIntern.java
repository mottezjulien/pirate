package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import java.util.List;
import java.util.stream.Stream;

public class GameEventBroadCastIntern implements GameEventBroadCast {

    public interface OutPort {
        Stream<Possibility> findPossibilities(GameSession.Id gameId, GamePlayer.Id playerId);

        void doGoal(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.ScenarioStep goal);

        void doGoalTarget(GamePlayer.Id playerId, Consequence.ScenarioTarget goalTarget);

        void doGameOver(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.SessionEnd consequence);

        void doMessage(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.DisplayMessage message);

        void saveAction(GamePlayer.Id playerId, Possibility.Id possibilityId, GameSessionTimeUnit timeClick);

        List<GameAction> findActions(GamePlayer.Id id);

        GameSessionTimeUnit current(GameSession.Id sessionId);

    }

    private final OutPort outPort;

    public GameEventBroadCastIntern(OutPort outPort) {
        this.outPort = outPort;
    }


    //TODO ASYNC
    @Override
    public void fire(GameEvent event, GameEventContext context) {
        //TODO Game in cache ?? In repo cache ?? Utile ??
        Stream<Possibility> possibilities = select(event, context);
        possibilities.forEach(possibility -> doAction(event, context, possibility));
    }

    private Stream<Possibility> select(GameEvent event, GameEventContext context) {
        List<GameAction> actions = outPort.findActions(context.playerId());
        return outPort.findPossibilities(context.sessionId(), context.playerId())
                .filter(possibility -> possibility.accept(event, actions));
    }

    private void doAction(GameEvent event, GameEventContext context, Possibility possibility) {
        possibility.consequences()
                .forEach(consequence -> _do(event, context, consequence));
        outPort.saveAction(context.playerId(), possibility.id(), outPort.current(context.sessionId()));
    }

    private void _do(GameEvent event, GameEventContext context, Consequence consequence) {
        switch (consequence) {
            case Consequence.ScenarioStep goal -> {
                outPort.doGoal(context.sessionId(), context.playerId(), goal);
                if (goal.state() == ScenarioGoal.State.ACTIVE) {
                    this.fire(new GameEvent.GoalActive(goal.stepId()), context);
                }
            }
            case Consequence.ScenarioTarget goalTarget -> outPort.doGoalTarget(context.playerId(), goalTarget);
            case Consequence.DisplayMessage message -> outPort.doMessage(context.sessionId(), context.playerId(), message);
            case Consequence.SessionEnd gameOver -> outPort.doGameOver(context.sessionId(), context.playerId(), gameOver);

            //TODO
            case Consequence.DisplayTalk talk -> { }
            case Consequence.ObjetAdd addObjet -> { }
            case Consequence.ObjetRemove removeObjet -> { }
            case Consequence.UpdatedMetadata updatedMetadata -> { }
        }

    }


}

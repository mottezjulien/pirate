package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.time.TimeUnit;

import java.util.List;
import java.util.stream.Stream;

public class GameEventBroadCastIntern implements GameEventBroadCast {

    public interface OutPort {
        Stream<Possibility> findPossibilities(GameSession.Id gameId, GamePlayer.Id playerId);

        void doGoal(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.ScenarioStep goal);

        void doGoalTarget(GamePlayer.Id playerId, Consequence.ScenarioTarget goalTarget);

        void doGameOver(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.SessionEnd consequence);

        void doAlert(GameSession.Id sessionId, GamePlayer.Id id, Consequence.DisplayTalkAlert alert);

        void saveAction(GamePlayer.Id playerId, Possibility.Id possibilityId, TimeUnit timeClick);

        List<GameAction> findActions(GamePlayer.Id id);
    }

    private final OutPort outPort;

    public GameEventBroadCastIntern(OutPort outPort) {
        this.outPort = outPort;
    }


    //TODO ASYNC
    @Override
    public void fire(GameEvent event) {
        //TODO Game in cache ?? In repo cache ?? Utile ??
        Stream<Possibility> possibilities = select(event);
        possibilities.forEach(possibility -> doAction(event, possibility));
    }

    private Stream<Possibility> select(GameEvent event) {
        List<GameAction> actions = outPort.findActions(event.playerId());
        return outPort.findPossibilities(event.sessionId(), event.playerId())
                .filter(possibility -> possibility.accept(event, actions));
    }

    private void doAction(GameEvent event, Possibility possibility) {
        possibility.consequences()
                .forEach(consequence -> _do(event, consequence));
        outPort.saveAction(event.playerId(), possibility.id(), event.timeUnit());
    }

    private void _do(GameEvent event, Consequence consequence) {
        switch (consequence) {
            case Consequence.ScenarioStep goal -> {
                outPort.doGoal(event.sessionId(), event.playerId(), goal);
                if (goal.state() == ScenarioGoal.State.ACTIVE) {
                    this.fire(new GameEvent.GoalActive(event.sessionId(), event.playerId(), event.timeUnit(), goal.stepId()));
                }
            }
            case Consequence.ScenarioTarget goalTarget -> outPort.doGoalTarget(event.playerId(), goalTarget);
            case Consequence.DisplayTalkAlert alert -> outPort.doAlert(event.sessionId(), event.playerId(), alert);
            case Consequence.SessionEnd gameOver -> outPort.doGameOver(event.sessionId(), event.playerId(), gameOver);

            //TODO
            case Consequence.DisplayTalkOptions messageConfirm -> { }
            case Consequence.ObjetAdd addObjet -> { }
            case Consequence.ObjetRemove removeObjet -> { }
            case Consequence.UpdatedMetadata updatedMetadata -> { }
        }

    }


}

package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

import java.util.stream.Stream;

public class GameEventBroadCastIntern implements GameEventBroadCast {

    public interface OutPort {
        Stream<Possibility> findPossibilities(GameSession.Id gameId, GamePlayer.Id playerId);

        void doGoal(GamePlayer.Id playerId, PossibilityConsequence.Goal goal);

        void doGoalTarget(GamePlayer.Id playerId, PossibilityConsequence.GoalTarget goalTarget);

        void doGameOver(GameSession.Id sessionId, GamePlayer.Id playerId, PossibilityConsequence.GameOver consequence);

        void doAlert(GameSession.Id sessionId, GamePlayer.Id id, PossibilityConsequence.Alert alert);


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
        return outPort.findPossibilities(event.sessionId(), event.playerId())
                .filter(possibility -> possibility.trigger().accept(event));
    }

    private void doAction(GameEvent event, Possibility possibility) {
        possibility.consequences()
                .forEach(consequence -> _do(event.sessionId(), event.playerId(), consequence));
    }

    private void _do(GameSession.Id sessionId, GamePlayer.Id playerId, PossibilityConsequence consequence) {
        switch (consequence) {
            case PossibilityConsequence.Goal goal -> outPort.doGoal(playerId, goal);

            case PossibilityConsequence.GoalTarget goalTarget -> outPort.doGoalTarget(playerId, goalTarget);

            case PossibilityConsequence.Alert alert -> outPort.doAlert(sessionId, playerId, alert);

            case PossibilityConsequence.GameOver gameOver -> outPort.doGameOver(sessionId, playerId, gameOver);

            //TODO
            case PossibilityConsequence.AddObjet addObjet -> {

            }
            case PossibilityConsequence.RemoveObjet removeObjet -> {

            }
            case PossibilityConsequence.UpdatedMetadata updatedMetadata -> {

            }
        }

    }


}

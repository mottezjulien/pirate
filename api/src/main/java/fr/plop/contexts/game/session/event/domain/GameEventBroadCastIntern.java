package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;

import java.util.stream.Stream;

public class GameEventBroadCastIntern implements GameEventBroadCast {

    public interface OutPort {
        Stream<Possibility> findPossibilities(GameSession.Id gameId, GamePlayer.Id playerId);

        void doGoal(GamePlayer.Id id, PossibilityConsequence.Goal activeStep);
        void doGameOver(GamePlayer.Id id, PossibilityConsequence.GameOver gameOver);
        void doAlert(GamePlayer.Id id, PossibilityConsequence.Alert alert);


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
        return outPort.findPossibilities(event.gameId(), event.playerId())
                .filter(possibility -> possibility.trigger().accept(event));
    }

    private void doAction(GameEvent event, Possibility possibility) {
        possibility.consequences()
                .forEach(consequence -> _do(event.playerId(), consequence));
    }

    private void _do(GamePlayer.Id id, PossibilityConsequence consequence) {
       switch (consequence) {
           case PossibilityConsequence.Goal goal -> outPort.doGoal(id, goal);

           case PossibilityConsequence.AddObjet addObjet -> {}//TODO _doAddObjet(game, id, addObjet.objetId());
           case PossibilityConsequence.Alert alert -> outPort.doAlert(id, alert);

           case PossibilityConsequence.GameOver gameOver -> outPort.doGameOver(id, gameOver);
           case PossibilityConsequence.RemoveObjet removeObjet -> {}//TODO _doAddObjet(game, id, removeObjet.objetId());

           case PossibilityConsequence.UpdatedMetadata updatedMetadata -> {} //TODO _doUpdatedMetadata(game, id, updatedMetadata.metadataId(), updatedMetadata.value());
       }

    }



}

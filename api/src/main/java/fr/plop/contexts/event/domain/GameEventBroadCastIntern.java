package fr.plop.contexts.event.domain;

import fr.plop.contexts.event.domain.usecase.action.GameEventScenarioSuccessGoalAction;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.scenario.domain.model.Possibility;
import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;

import java.util.stream.Stream;


//TODO pouet

public class GameEventBroadCastIntern implements GameEventBroadCast {

    public interface OutPort {
        Stream<Possibility> findPossibilities(Game.Id gameId, GamePlayer.Id playerId);
    }

    private final OutPort outPort;

    private final GameEventScenarioSuccessGoalAction successGoalAction;

    public GameEventBroadCastIntern(OutPort outPort, GameEventScenarioSuccessGoalAction successGoalAction) {
        this.outPort = outPort;
        this.successGoalAction = successGoalAction;
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
           case PossibilityConsequence.AddObjet addObjet -> {}//TODO _doAddObjet(game, id, addObjet.objetId());
           case PossibilityConsequence.Alert alert -> {} //TODO _doAlert(game, id, alert.message());
           case PossibilityConsequence.SuccessGoal successGoal -> successGoalAction.apply(successGoal, id);
           case PossibilityConsequence.GameOver gameOver -> {}//TODO_doGameOver(game, id);
           case PossibilityConsequence.RemoveObjet removeObjet -> {}//TODO _doAddObjet(game, id, removeObjet.objetId());
           case PossibilityConsequence.ActiveGoal activeStep -> {}//TODO_doStartedStep(game, id, activeStep.stepId());
           case PossibilityConsequence.UpdatedMetadata updatedMetadata -> {} //TODO _doUpdatedMetadata(game, id, updatedMetadata.metadataId(), updatedMetadata.value());
       }

    }



}

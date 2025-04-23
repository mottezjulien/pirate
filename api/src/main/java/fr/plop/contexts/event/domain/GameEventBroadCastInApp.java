package fr.plop.contexts.event.domain;

import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.scenario.domain.model.Possibility;
import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.scenario.domain.model.Scenario;
import fr.plop.contexts.scenario.domain.model.ScenarioGoal;

import java.util.List;
import java.util.Optional;


pouet

public class GameEventBroadCastInApp implements GameEventBroadCast {

    public interface OutPort {

        Optional<ScenarioGoal> findActiveGoal(Scenario.Step.Id stepId, GamePlayer.Id playerId);

        void setSuccess(ScenarioGoal goal);

        Optional<Game> findById(Game.Id id);
    }

    private final OutPort outPort;

    private final GameEventTrigger trigger;

    public GameEventBroadCastInApp(OutPort outPort, GameEventTrigger trigger) {
        this.outPort = outPort;
        this.trigger = trigger;
    }




    //TODO ASYNC
    @Override
    public void fire(GameEvent event) {

        //TODO Game in cache ?? In repo cache ?? Utile ??
        /*Game game = cache.stream()
                .filter(each -> each.id().equals(event.gameId()))
                .findFirst()
                .orElseThrow();*/
        //Game game = outPort.findById(event.gameId()).orElseThrow();
        action(event);
    }

    private void action(GameEvent event) {
        List<Possibility> possibilities = trigger.pouet(event.gameId(), event);
        possibilities.forEach(possibility -> doAction(event, possibility));
    }

    private void doAction(GameEvent event, Possibility possibility) {
        possibility.consequences()
                .forEach(consequence -> _do(event.playerId(), consequence));
    }

    private void _do(GamePlayer.Id id, PossibilityConsequence consequence) {
       switch (consequence) {
           case PossibilityConsequence.AddObjet addObjet -> {}//TODO _doAddObjet(game, id, addObjet.objetId());
           case PossibilityConsequence.Alert alert -> {} //TODO _doAlert(game, id, alert.message());
           case PossibilityConsequence.SuccessGoal successGoal -> _doSuccessGoal(id, successGoal.stepId());
           case PossibilityConsequence.GameOver gameOver -> {}//TODO_doGameOver(game, id);
           case PossibilityConsequence.RemoveObjet removeObjet -> {}//TODO _doAddObjet(game, id, removeObjet.objetId());
           case PossibilityConsequence.ActiveGoal activeStep -> {}//TODO_doStartedStep(game, id, activeStep.stepId());
           case PossibilityConsequence.UpdatedMetadata updatedMetadata -> {} //TODO _doUpdatedMetadata(game, id, updatedMetadata.metadataId(), updatedMetadata.value());
       }

    }

    private void _doSuccessGoal(GamePlayer.Id playerId, Scenario.Step.Id stepId) {
        Optional<ScenarioGoal> optGoal = outPort.findActiveGoal(stepId, playerId);
        optGoal.ifPresent(goal -> outPort.setSuccess(goal));
    }

}

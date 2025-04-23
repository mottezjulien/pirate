package fr.plop.contexts.event.domain;

import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.scenario.domain.model.Possibility;
import fr.plop.contexts.scenario.domain.model.Scenario;

import java.util.ArrayList;
import java.util.List;

public class GameEventTrigger {

    public interface DataOutput {
        List<Scenario.Step> steps(Game.Id gameId, GamePlayer.Id playerId);

    }

    private final DataOutput dataOutput;

   GameEventTrigger(DataOutput dataOutput) {
       this.dataOutput = dataOutput;
   }

   //TODO Pouet
    public List<Possibility> pouet(Game.Id gameId, GameEvent event) {
        List<Scenario.Step> steps = dataOutput.steps(gameId, event.playerId());
        List<Possibility> result = new ArrayList<>();
        for(Scenario.Step step : steps) {
            for(Possibility possibility: step.possibilities()) {
                if(possibility.trigger().accept(event)) {
                    result.add(possibility);
                }
            }
        }
        return result;
    }



}

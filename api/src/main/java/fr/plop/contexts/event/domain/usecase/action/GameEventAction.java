package fr.plop.contexts.event.domain.usecase.action;

import fr.plop.contexts.game.domain.model.GamePlayer;
import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;

public interface GameEventAction<Consequence extends PossibilityConsequence> {

    void apply(Consequence consequence, GamePlayer.Id playerId);

}

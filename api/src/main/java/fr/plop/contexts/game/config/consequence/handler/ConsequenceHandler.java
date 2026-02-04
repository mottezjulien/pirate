package fr.plop.contexts.game.config.consequence.handler;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;

public interface ConsequenceHandler {
    boolean supports(Consequence consequence);
    void handle(GameInstanceContext context, Consequence consequence);
}

package fr.plop.contexts.game.config.consequence.handler;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

public interface ConsequenceHandler {
    boolean supports(Consequence consequence);
    void handle(GameSessionContext context, Consequence consequence);
}

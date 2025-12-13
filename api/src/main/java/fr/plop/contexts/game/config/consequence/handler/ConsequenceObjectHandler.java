package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import org.springframework.stereotype.Component;

@Component
public class ConsequenceObjectHandler implements ConsequenceHandler {
    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.ObjetAdd || consequence instanceof Consequence.ObjetRemove;
    }

    @Override
    public void handle(GameSessionContext context, Consequence consequence) {

    }
}

package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import org.springframework.stereotype.Component;

@Component
public class ConsequenceMetadataHandler implements ConsequenceHandler {
    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.UpdatedMetadata;
    }

    @Override
    public void handle(GameInstanceContext context, Consequence consequence) {

    }

}

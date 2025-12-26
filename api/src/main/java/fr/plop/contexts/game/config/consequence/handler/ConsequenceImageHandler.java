package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ConsequenceImageHandler implements ConsequenceHandler {

    private final PushPort pushPort;

    public ConsequenceImageHandler(PushPort pushPort) {
        this.pushPort = pushPort;
    }

    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.DisplayImage;
    }

    @Override
    public void handle(GameSessionContext context, Consequence consequence) {
        if (Objects.requireNonNull(consequence) instanceof Consequence.DisplayImage image) {
            pushPort.push(new PushEvent.Image(context, image.imageId()));
        } else {
            throw new IllegalStateException("Unexpected value: " + consequence);
        }
    }

}

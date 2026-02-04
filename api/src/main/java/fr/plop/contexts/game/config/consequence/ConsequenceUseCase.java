package fr.plop.contexts.game.config.consequence;

import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.config.consequence.handler.ConsequenceHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsequenceUseCase {

    private final List<ConsequenceHandler> handlers = Collections.synchronizedList(new ArrayList<>());

    public void registerHandler(ConsequenceHandler handler) {
        handlers.add(handler);
    }

    public void action(GameInstanceContext context, Consequence consequence) {
        handlers.stream()
            .filter(handler -> handler.supports(consequence))
            .forEach(handler -> {
                try {
                    handler.handle(context, consequence);
                } catch (Exception e) {
                    throw new ConsequenceException("Error dispatching consequence: " + consequence.getClass().getName(), e);
                }
            });
    }



}

package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class ConsequenceObjectHandler implements ConsequenceHandler {

    private final GameSessionInventoryUseCase inventoryUseCase;

    public ConsequenceObjectHandler(GameSessionInventoryUseCase inventoryUseCase) {
        this.inventoryUseCase = inventoryUseCase;
    }

    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.ObjetAdd || consequence instanceof Consequence.ObjetRemove;
    }

    @Override
    public void handle(GameSessionContext context, Consequence consequence) {
        switch (consequence) {
            case Consequence.ObjetAdd add -> {
                GameConfigInventoryItem.Id itemId = new GameConfigInventoryItem.Id(add.objetId());
                inventoryUseCase.addItem(context, itemId, 1);
            }
            case Consequence.ObjetRemove remove -> {
                // TODO: implémenter la suppression d'item
            }
            default -> { }
        }
    }
}

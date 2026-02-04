package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryException;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class ConsequenceInventoryHandler implements ConsequenceHandler {

    private final GameInstanceInventoryUseCase useCase;

    public ConsequenceInventoryHandler(GameInstanceInventoryUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.InventoryAddItem || consequence instanceof Consequence.InventoryRemoveItem;
    }

    @Override
    public void handle(GameInstanceContext context, Consequence consequence) {
        switch (consequence) {
            case Consequence.InventoryAddItem add -> {
                try {
                    useCase.insertOne(context, add.itemId());
                } catch (GameInstanceInventoryException e) {
                    throw new RuntimeException(e);
                }
            }
            case Consequence.InventoryRemoveItem remove -> {
                try {
                    useCase.deleteOne(context, remove.itemId());
                } catch (GameInstanceInventoryException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> { }
        }
    }
}

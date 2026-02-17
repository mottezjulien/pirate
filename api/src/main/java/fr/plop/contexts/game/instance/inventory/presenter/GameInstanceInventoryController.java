package fr.plop.contexts.game.instance.inventory.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryException;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryItem;
import fr.plop.contexts.game.instance.inventory.domain.GameInstanceInventoryUseCase;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/instances/{instanceId}/inventory")
public class GameInstanceInventoryController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameInstanceInventoryUseCase useCase;

    public GameInstanceInventoryController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameInstanceInventoryUseCase useCase) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.useCase = useCase;
    }

    @GetMapping({"", "/"})
    public Stream<SimpleResponseDTO> list(@RequestHeader("Authorization") String rawInstanceToken,
                                          @RequestHeader("Language") String languageStr,
                                          @PathVariable("instanceId") String instanceIdStr) {
        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            return useCase.list(context)
                    .map(model -> SimpleResponseDTO.fromModel(model, language));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    @GetMapping({"/{inventoryItem}", "/{inventoryItem}/"})
    public DetailResponseDTO details(@RequestHeader("Authorization") String rawInstanceToken,
                                     @RequestHeader("Language") String languageStr,
                                     @PathVariable("instanceId") String instanceIdStr,
                                     @PathVariable("inventoryItem") String inventoryItemStr) {
        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemId = new GameInstanceInventoryItem.Id(inventoryItemStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            Optional<GameInstanceInventoryItem> itemOpt = useCase.details(context, itemId);
            if (itemOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
            }
            return DetailResponseDTO.fromModel(itemOpt.get(), language);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    @DeleteMapping({"/{inventoryItem}", "/{inventoryItem}/"})
    public void drop(@RequestHeader("Authorization") String rawInstanceToken,
                     @PathVariable("instanceId") String instanceIdStr,
                     @PathVariable("inventoryItem") String inventoryItemStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemId = new GameInstanceInventoryItem.Id(inventoryItemStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            useCase.drop(context, itemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/{inventoryItem}/consume", "/{inventoryItem}/consume/"})
    public void consume(@RequestHeader("Authorization") String rawInstanceToken,
                        @PathVariable("instanceId") String instanceIdStr,
                        @PathVariable("inventoryItem") String inventoryItemStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemId = new GameInstanceInventoryItem.Id(inventoryItemStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            useCase.consume(context, itemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    @PostMapping({"/{inventoryItem}/use", "/{inventoryItem}/use/"})
    public void use(@RequestHeader("Authorization") String rawInstanceToken,
                        @PathVariable("instanceId") String instanceIdStr,
                        @PathVariable("inventoryItem") String inventoryItemStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemId = new GameInstanceInventoryItem.Id(inventoryItemStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            useCase.use(context, itemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
    
    @PostMapping({"/{inventoryItem}/equip", "/{inventoryItem}/equip/"})
    public void equip(@RequestHeader("Authorization") String rawInstanceToken,
                      @PathVariable("instanceId") String instanceIdStr,
                      @PathVariable("inventoryItem") String inventoryItemStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemId = new GameInstanceInventoryItem.Id(inventoryItemStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            useCase.equip(context, itemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/{inventoryItem}/unequip", "/{inventoryItem}/unequip/"})
    public void unequip(@RequestHeader("Authorization") String rawInstanceToken,
                        @PathVariable("instanceId") String instanceIdStr,
                        @PathVariable("inventoryItem") String inventoryItemStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemId = new GameInstanceInventoryItem.Id(inventoryItemStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            useCase.unequip(context, itemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/{inventoryItem}/equip/use", "/{inventoryItem}/equip/use/"})
    public void useEquip(@RequestHeader("Authorization") String rawInstanceToken,
                      @PathVariable("instanceId") String instanceIdStr,
                         @PathVariable("inventoryItem") String inventoryItemStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemId = new GameInstanceInventoryItem.Id(inventoryItemStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            useCase.useEquip(context, itemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/merge", "/merge/"})
    public void merge(@RequestHeader("Authorization") String rawInstanceToken,
                        @PathVariable("instanceId") String instanceIdStr,
                        @RequestBody List<String> itemsStr) {
        //TODO Gestion de l'exception si les deux objets ne merge pas
        if(itemsStr.size() != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action action is only supported with 2 items");
        }
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final GameInstanceInventoryItem.Id itemOneId = new GameInstanceInventoryItem.Id(itemsStr.getFirst());
        final GameInstanceInventoryItem.Id itemOtherId = new GameInstanceInventoryItem.Id(itemsStr.get(1));
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawInstanceToken));
            useCase.merge(context, itemOneId, itemOtherId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }




    public record SimpleResponseDTO(String id, String label, ImageResponseDTO image, List<String> actions, int count) {
        public static SimpleResponseDTO fromModel(GameInstanceInventoryItem item, Language language) {
            return new SimpleResponseDTO(
                    item.instanceId().value(), item.label().value(language),
                    ImageResponseDTO.fromModel(item.image()),
                    item.actions().stream().map(Enum::name).toList(), item.count());
        }
    }

    public record DetailResponseDTO(String id, String label, ImageResponseDTO image, List<String> actions, int count,
                                    String description, String availability) {
        public static DetailResponseDTO fromModel(GameInstanceInventoryItem item, Language language) {
            return new DetailResponseDTO(
                    item.instanceId().value(), item.label().value(language),
                    ImageResponseDTO.fromModel(item.image()),
                    item.actions().stream().map(Enum::name).toList(), item.count(),
                    item.optDescription().map(desc -> desc.value(language)).orElse(null),
                    item.availability().name()
            );
        }
    }
}

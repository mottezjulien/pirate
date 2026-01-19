package fr.plop.contexts.game.session.inventory.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryException;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryItem;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/sessions/{sessionId}/inventory")
public class GameSessionInventoryController {

    private final ConnectAuthGameSessionUseCase authGameSessionUseCase;
    private final GameSessionInventoryUseCase useCase;

    public GameSessionInventoryController(ConnectAuthGameSessionUseCase authGameSessionUseCase, GameSessionInventoryUseCase useCase) {
        this.authGameSessionUseCase = authGameSessionUseCase;
        this.useCase = useCase;
    }

    @GetMapping({"", "/"})
    public Stream<SimpleResponseDTO> list(@RequestHeader("Authorization") String rawSessionToken,
                                          @RequestHeader("Language") String languageStr,
                                          @PathVariable("sessionId") String sessionIdStr) {
        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            return useCase.list(context)
                    .map(model -> SimpleResponseDTO.fromModel(model, language));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    @GetMapping({"/{inventoryItem}", "/{inventoryItem}/"})
    public DetailResponseDTO details(@RequestHeader("Authorization") String rawSessionToken,
                                     @RequestHeader("Language") String languageStr,
                                     @PathVariable("sessionId") String sessionIdStr,
                                     @PathVariable("inventoryItem") String gameSessionInventoryItemStr) {
        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final GameSessionInventoryItem.Id sessionItemId = new GameSessionInventoryItem.Id(gameSessionInventoryItemStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            Optional<GameSessionInventoryItem> itemOpt = useCase.details(context, sessionItemId);
            if (itemOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
            }
            return DetailResponseDTO.fromModel(itemOpt.get(), language);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    @DeleteMapping({"/{inventoryItem}", "/{inventoryItem}/"})
    public void drop(@RequestHeader("Authorization") String rawSessionToken,
                     @PathVariable("sessionId") String sessionIdStr,
                     @PathVariable("inventoryItem") String gameSessionInventoryItemStr) {
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final GameSessionInventoryItem.Id sessionItemId = new GameSessionInventoryItem.Id(gameSessionInventoryItemStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            useCase.drop(context, sessionItemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameSessionInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/{inventoryItem}/consume", "/{inventoryItem}/consume/"})
    public void consume(@RequestHeader("Authorization") String rawSessionToken,
                        @PathVariable("sessionId") String sessionIdStr,
                        @PathVariable("inventoryItem") String gameSessionInventoryItemStr) {
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final GameSessionInventoryItem.Id sessionItemId = new GameSessionInventoryItem.Id(gameSessionInventoryItemStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            useCase.consume(context, sessionItemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameSessionInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    @PostMapping({"/{inventoryItem}/use", "/{inventoryItem}/use/"})
    public void use(@RequestHeader("Authorization") String rawSessionToken,
                        @PathVariable("sessionId") String sessionIdStr,
                        @PathVariable("inventoryItem") String gameSessionInventoryItemStr) {
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final GameSessionInventoryItem.Id sessionItemId = new GameSessionInventoryItem.Id(gameSessionInventoryItemStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            useCase.use(context, sessionItemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameSessionInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
    
    @PostMapping({"/{inventoryItem}/equip", "/{inventoryItem}/equip/"})
    public void equip(@RequestHeader("Authorization") String rawSessionToken,
                      @PathVariable("sessionId") String sessionIdStr,
                      @PathVariable("inventoryItem") String gameSessionInventoryItemStr) {
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final GameSessionInventoryItem.Id sessionItemId = new GameSessionInventoryItem.Id(gameSessionInventoryItemStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            useCase.equip(context, sessionItemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameSessionInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/{inventoryItem}/unequip", "/{inventoryItem}/unequip/"})
    public void unequip(@RequestHeader("Authorization") String rawSessionToken,
                        @PathVariable("sessionId") String sessionIdStr,
                        @PathVariable("inventoryItem") String gameSessionInventoryItemStr) {
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final GameSessionInventoryItem.Id sessionItemId = new GameSessionInventoryItem.Id(gameSessionInventoryItemStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            useCase.unequip(context, sessionItemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameSessionInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/{inventoryItem}/equip/use", "/{inventoryItem}/equip/use/"})
    public void useEquip(@RequestHeader("Authorization") String rawSessionToken,
                      @PathVariable("sessionId") String sessionIdStr, 
                         @PathVariable("inventoryItem") String gameSessionInventoryItemStr) {
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final GameSessionInventoryItem.Id sessionItemId = new GameSessionInventoryItem.Id(gameSessionInventoryItemStr);
        try {
            final GameSessionContext context = authGameSessionUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            useCase.useEquip(context, sessionItemId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameSessionInventoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    public record SimpleResponseDTO(String id, String label, ImageResponseDTO image, List<String> actions, int count) {
        public static SimpleResponseDTO fromModel(GameSessionInventoryItem item, Language language) {
            return new SimpleResponseDTO(
                    item.sessionId().value(), item.label().value(language),
                    ImageResponseDTO.fromModel(item.image()),
                    item.actions().stream().map(Enum::name).toList(), item.count());
        }
    }

    public record DetailResponseDTO(String id, String label, ImageResponseDTO image, List<String> actions, int count,
                                    String description, String availability) {
        public static DetailResponseDTO fromModel(GameSessionInventoryItem item, Language language) {
            return new DetailResponseDTO(
                    item.sessionId().value(), item.label().value(language),
                    ImageResponseDTO.fromModel(item.image()),
                    item.actions().stream().map(Enum::name).toList(), item.count(),
                    item.optDescription().map(desc -> desc.value(language)).orElse(null),
                    item.availability().name()
            );
        }
    }
}

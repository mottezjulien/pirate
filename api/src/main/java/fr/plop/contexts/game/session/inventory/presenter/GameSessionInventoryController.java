package fr.plop.contexts.game.session.inventory.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryItem;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryItem;
import fr.plop.contexts.game.session.inventory.domain.GameSessionInventoryUseCase;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.contexts.game.session.talk.TalkController;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ClientInfoStatus;
import java.util.List;
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

    public record SimpleResponseDTO(String id, String label, int count, ImageResponseDTO image) {
        public static SimpleResponseDTO fromModel(GameSessionInventoryItem item, Language language){
            return new SimpleResponseDTO(item.id().value(), item.label().value(language),
                    item.count(),  ImageResponseDTO.fromModel(item.image()));
        }
    }
    
}

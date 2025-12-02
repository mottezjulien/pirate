package fr.plop.contexts.game.session.image.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.ImageDetailsResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/sessions/{sessionId}/images")
public class ImageController {

    private final ConnectUseCase connectUseCase;
    private final GameConfigCache cache;
    private final GameSessionSituationGetPort situationGetPort;

    public ImageController(ConnectUseCase connectUseCase, GameConfigCache cache, GameSessionSituationGetPort situationGetPort) {
        this.connectUseCase = connectUseCase;
        this.cache = cache;
        this.situationGetPort = situationGetPort;
    }

    @GetMapping({"/{imageId}", "/{imageId}/"})
    public ResponseDTO getOne(@RequestHeader("Authorization") String rawToken,
                              @RequestHeader("Language") String languageStr,
                              @PathVariable("sessionId") String sessionIdStr,
                              @PathVariable("imageId") String imageIdStr) {

        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        final ImageItem.Id imageId = new ImageItem.Id(imageIdStr);
        try {
            final ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            final GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found"));
            final ImageConfig imageConfig = cache.image(sessionId);
            final ImageItem item = imageConfig.byItemId(imageId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));
            final GameSessionSituation situation = situationGetPort.get(sessionId, player);
            return ResponseDTO.fromModel(item.select(situation));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record ResponseDTO(String id, ImageDetailsResponseDTO image) {
        public static ResponseDTO fromModel(ImageItem model) {
            return new ResponseDTO(model.id().value(), ImageDetailsResponseDTO.fromModel(model.generic()));
        }
    }



}

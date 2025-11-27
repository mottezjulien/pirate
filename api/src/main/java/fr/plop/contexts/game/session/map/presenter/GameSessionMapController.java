package fr.plop.contexts.game.session.map.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/sessions/{sessionId}/maps")
public class GameSessionMapController {

    private final ConnectUseCase connectUseCase;
    private final GameConfigCache cache;

    public GameSessionMapController(ConnectUseCase connectUseCase, GameConfigCache cache) {
        this.connectUseCase = connectUseCase;
        this.cache = cache;
    }

    @GetMapping({"", "/"})
    public List<GameMapResponseDTO> maps(@RequestHeader("Authorization") String rawToken,
                                         @RequestHeader("Language") String languageStr,
                                         @PathVariable("sessionId") String sessionIdStr) {

        Language language = Language.valueOf(languageStr.toUpperCase());
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);

        try {
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));
            Stream<MapItem> maps = cache.map(sessionId).byStepIds(player.activeStepIds());
            return maps.map(model -> GameMapResponseDTO.fromModel(model, language)).toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }

    }


    public record GameMapResponseDTO(String id, String label, ImageResponseDTO image, List<Item> items) {

        public record Item(String id, String label, String type, Position position, Point point, ImageResponseDTO image) {

            static Item fromModel(MapItem.Position model) {
                return switch (model) {
                    case MapItem.Position.Point point ->
                            new Item(model.id().value(), model.label(),  "POINT", new Position(point.top(), point.left()), new Point(point.color()), null);
                    case MapItem.Position._Image image ->
                            new Item(model.id().value(), model.label(), "IMAGE", new Position(image.top(), image.left()), null, imageResponseDTOFromModel(image.value()));
                };
            }

            public record Position(double top, double left) {

            }

            public record Point(String color) {

            }

        }

        public static GameMapResponseDTO fromModel(MapItem map, Language language) {
            return new GameMapResponseDTO(
                    map.id().value(),
                    map.label().value(language),
                    imageResponseDTOFromModel(map.image()),
                    map.positions().stream().map(Item::fromModel).toList());
        }

        private static ImageResponseDTO imageResponseDTOFromModel(Image image) {
            return new ImageResponseDTO(image.type().name(), image.value());
        }

    }

}



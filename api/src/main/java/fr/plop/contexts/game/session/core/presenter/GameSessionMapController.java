package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.map.persistence.MapConfigRepository;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/sessions/{sessionId}/maps")
public class GameSessionMapController {

    private final ConnectUseCase connectUseCase;
    private final GameSessionRepository gameSessionRepository;

    private final MapConfigRepository mapConfigRepository;

    public GameSessionMapController(ConnectUseCase connectUseCase, GameSessionRepository gameSessionRepository, MapConfigRepository mapConfigRepository) {
        this.connectUseCase = connectUseCase;
        this.gameSessionRepository = gameSessionRepository;
        this.mapConfigRepository = mapConfigRepository;
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

            MapConfig.Id mapConfigId = new MapConfig.Id(gameSessionRepository.mapId(sessionId.value())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No map found", null)));

            //TODO Cache
            MapConfig mapConfig = mapConfigRepository.fullById(mapConfigId.value())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No map found", null))
                    .toModel();
            Stream<MapItem> maps = mapConfig.byStepIds(player.stepActiveIds());
            return maps.map(model -> GameMapResponseDTO.fromModel(model, language))
                    .toList();

        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record GameMapResponseDTO(String id, String label, int priority, ImageResponseDTO image, List<Position> positions) {

        public record Position(String type, String id, String label, Bounds bounds, Point point) {

            static Position fromModel(MapItem.Position model) {
                return switch (model) {
                    case MapItem.Position.Point point ->
                            new Position("POINT", model.id().value(), model.label(), null, Point.fromModel(point));
                    case MapItem.Position.Zone zone ->
                            new Position("ZONE", model.id().value(), model.label(), Bounds.fromModel(zone), null);
                };
            }

            public record Bounds(double left, double top, double right, double bottom) {
                public static Bounds fromModel(MapItem.Position.Zone zone) {
                    return new Bounds(zone.left(), zone.top(), zone.right(), zone.bottom());
                }
            }

            public record Point(double x, double y) {
                public static Point fromModel(MapItem.Position.Point model) {
                    return new Point(model.x(), model.y());
                }
            }

        }

        public static GameMapResponseDTO fromModel(MapItem map, Language language) {
            return new GameMapResponseDTO(
                    map.id().value(),
                    map.label().value(language),
                    map.priority().value(),
                    imageResponseDTOFromModel(map.image()),
                    map.positions().stream().map(Position::fromModel).toList());
        }

        private static ImageResponseDTO imageResponseDTOFromModel(MapItem.Image image) {
            MapItem.Image.Size sizeModel = image.size();
            ImageResponseDTO.Size sizeDTO = new ImageResponseDTO.Size(sizeModel.width(), sizeModel.height());
            return new ImageResponseDTO(image.type().name(), image.value(), sizeDTO);
        }

    }

}



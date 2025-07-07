package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.persistence.MapConfigRepository;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameCreateSessionUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionEntity;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.generic.position.Point;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/sessions")
public class GameSessionController {

    private final ConnectUseCase connectUseCase;
    private final GameCreateSessionUseCase createUseCase;
    private final GameMoveUseCase moveUseCase;

    private final MapConfigRepository mapConfigRepository;
    private final GameSessionRepository gameSessionRepository;

    private final PushPort pushPort;


    public GameSessionController(ConnectUseCase connectUseCase, GameCreateSessionUseCase createUseCase, GameMoveUseCase moveUseCase, MapConfigRepository mapConfigRepository, GameSessionRepository gameSessionRepository, PushPort pushPort) {
        this.connectUseCase = connectUseCase;
        this.createUseCase = createUseCase;
        this.moveUseCase = moveUseCase;
        this.mapConfigRepository = mapConfigRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.pushPort = pushPort;
    }


    @PostMapping({"", "/"})
    public GameSessionCreateResponse create(
            @RequestHeader("Authorization") String rawToken,
            @RequestBody GameSessionCreateRequest request
    ) {
        try {
            ConnectUser user = connectUseCase.findUserIdByRawToken(new ConnectToken(rawToken));
            Template.Code templateCode = new Template.Code(request.templateCode());
            GameSession.Atom session = createUseCase.apply(templateCode, user.id());
            return new GameSessionCreateResponse(session.id().value(), session.label());
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            switch (e.type()) {
                case TEMPLATE_NOT_FOUND:
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The template is not found", e);
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
            }
        }
    }

    public record GameSessionCreateRequest(String templateCode) {

    }

    public record GameSessionCreateResponse(String id, String label) {

    }

    //TODO Player token ??
    @PostMapping({"/{sessionId}/move", "/{sessionId}/move/"})
    public void move(
            @RequestHeader("Authorization") String rawToken,
            @PathVariable("sessionId") String sessionIdStr,
            @RequestBody GameMoveRequestDTO request) {
        try {
            GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));

            moveUseCase.apply(sessionId, player, request.toModel());

        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.type().name(), e);
        }
    }

    public record GameMoveRequestDTO(float lat, float lng) {
        public GameMoveUseCase.Request toModel() {
            return new GameMoveUseCase.Request(new Point(lat, lng));
        }
    }


    @GetMapping({"/{sessionId}/maps", "/{sessionId}/maps/"})
    public List<GameMapResponseDTO> maps(@RequestHeader("Authorization") String rawToken,
                                         @RequestHeader("Language") String languageStr,
                                         @PathVariable("sessionId") String sessionIdStr) {

        //donner la position de l'utilisateur, le back sait tout (update systeme)

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
            Stream<Map> maps = mapConfig.byStepIds(player.stepActiveIds());
            java.util.Map<Map, Optional<Map.Position>> positionByMap = selectPositions(maps, player);

            return positionByMap.entrySet().stream()
                    .map(entry -> GameMapResponseDTO.fromModel(entry.getKey(), entry.getValue(), language))
                    .toList();

        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    private java.util.Map<Map, Optional<Map.Position>> selectPositions(Stream<Map> maps, GamePlayer player) {
        return maps.collect(Collectors.toMap(map -> map, map -> selectPosition(map, player)));
    }

    private Optional<Map.Position> selectPosition(Map map, GamePlayer player) {
        return map.selectPosition(player);
    }

    public record GameMapResponseDTO(String id, String label, int priority,
                                     String definitionType, String definitionValue,
                                     Position position) {

        //Pourcent
        public record Position(double x, double y) {
            public static Position toModel(Map.Position.Point model) {
                return new Position(model.x(), model.y());
            }
        }

        public static GameMapResponseDTO fromModel(Map map, Optional<Map.Position> optPosition, Language language) {
            return new GameMapResponseDTO(map.id().value(), map.label().value(language), map.priority().value(),
                    map.definition().type().name(), map.definition().value(),
                    optPosition.map(position -> Position.toModel(position.point())).orElse(null));
        }

    }


    //TODO UTILE ???
    /*@GetMapping({"/{sessionId}/maps-first", "/{sessionId}/maps-first/"})
    public List<GameMapResponseFirstDTO> mapsFirst(@RequestHeader("Authorization") String rawToken,
                                                 @RequestHeader("Language") String languageStr,
                                          @PathVariable("sessionId") String sessionIdStr) {
        Language language = Language.valueOf(languageStr.toUpperCase());
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);

        List<MapEntity> mapEntities = gameSessionRepository.fullMap(sessionId.value());
        return mapEntities.stream()
                .map(entity -> GameMapResponseFirstDTO.fromModel(entity.toModel(), language))
                .toList();
    }

    public record GameMapResponseFirstDTO(String id, String label, String definition, PointResponseDTO bottomLeft, PointResponseDTO topRight) {

        public static GameMapResponseFirstDTO fromModel(Map model, Language language) {
            return new GameMapResponseFirstDTO(model.id().value(), model.label().value(language),
                    model.definition(),
                    PointResponseDTO.fromModel(model.rect().bottomLeft()),
                    PointResponseDTO.fromModel(model.rect().topRight()));
        }
    }

    public record PointResponseDTO(double lat, double lng) {
        public static PointResponseDTO fromModel(Point model) {
            return new PointResponseDTO(model.lat(), model.lng());
        }
    }*/


    //ADMIN

    @GetMapping({"","/"})
    public List<GameSessionResponseDTO> sessions() {
        return gameSessionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GameSessionEntity::getStartAt))
                .map(GameSessionResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping({"/{sessionId}", "/{sessionId}/"})
    public GameSessionDetailsResponseDTO oneSession(@PathVariable("sessionId") String sessionIdStr) {
        return gameSessionRepository.allById(sessionIdStr)
                .map(entity -> GameSessionDetailsResponseDTO.fromEntity(entity))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record GameSessionResponseDTO(String id, String label, String state, String startAt, String overAt) {
        public static GameSessionResponseDTO fromEntity(GameSessionEntity entity) {
            String startAt = null;
            if(entity.getStartAt() != null) {
                startAt = entity.getStartAt().toString();
            }
            String overAt = null;
            if(entity.getOverAt() != null) {
                overAt = entity.getOverAt().toString();
            }
            return new GameSessionResponseDTO(entity.getId(),
                    entity.getLabel(),
                    entity.getState().name(), startAt, overAt);
        }
    }

    public record GameSessionDetailsResponseDTO(String id, String label, String state, String startAt, String overAt, List<Player> players) {
        public record Player(String id, String state, String userId) {
            public static Player fromEntity(GamePlayerEntity entity) {
                return new Player(entity.getId(), entity.getState().name(), entity.getUser().getId());
            }
        }

        public static GameSessionDetailsResponseDTO fromEntity(GameSessionEntity entity) {
            String startAt = entity.getStartAt().toString();
            String overAt = null;
            if(entity.getOverAt() != null) {
                overAt = entity.getOverAt().toString();
            }
            List<Player> players =  entity.getPlayers().stream()
                    .map(Player::fromEntity).toList();
            return new GameSessionDetailsResponseDTO(entity.getId(),
                    entity.getLabel(),
                    entity.getState().name(), startAt, overAt, players);
        }
    }


    @PostMapping({"/{sessionId}/players/{playerId}/message"})
    public void sendMessage(@PathVariable("sessionId") String sessionIdStr,
                            @PathVariable("playerId") String playerIdStr,
                            @RequestBody SendMessageRequest request) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        GamePlayer.Id playerId = new GamePlayer.Id(playerIdStr);
        pushPort.push(new PushEvent.Message(sessionId, playerId, request.value()));
    }

    public record SendMessageRequest(String value) {

    }

}



package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.persistence.MapConfigRepository;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioStepEntity;
import fr.plop.contexts.game.config.scenario.persistence.core.ScenarioTargetEntity;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameCreateSessionUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalEntity;
import fr.plop.contexts.game.session.scenario.persistence.ScenarioGoalRepository;
import fr.plop.contexts.i18n.domain.I18n;
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
    private final ScenarioGoalRepository goalRepository;

    private final MapConfigRepository mapConfigRepository;
    private final GameSessionRepository gameSessionRepository;


    public GameSessionController(ConnectUseCase connectUseCase, GameCreateSessionUseCase createUseCase, GameMoveUseCase moveUseCase, ScenarioGoalRepository goalRepository, MapConfigRepository mapConfigRepository, GameSessionRepository gameSessionRepository) {
        this.connectUseCase = connectUseCase;
        this.createUseCase = createUseCase;
        this.moveUseCase = moveUseCase;
        this.goalRepository = goalRepository;
        this.mapConfigRepository = mapConfigRepository;
        this.gameSessionRepository = gameSessionRepository;
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


    //TODO TO MOVE TO SCENARIO CONTROLLER ??
    @GetMapping({"/{sessionId}/goals", "/{sessionId}/goals/"})
    public List<GameGoalSimpleResponseDTO> goals(@RequestHeader("Authorization") String rawToken,
                                                 @RequestHeader("Language") String languageStr, @PathVariable("sessionId") String sessionIdStr) {
        try {
            GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));

            Language language = Language.valueOf(languageStr.toUpperCase());
            List<ScenarioGoalEntity> goalEntities = goalRepository.fullByPlayerId(player.id().value());
            return goalEntities.stream()
                    .map(goalEntity -> GameGoalSimpleResponseDTO.fromEntity(goalEntity, language))
                    .toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record GameGoalSimpleResponseDTO(String id, String label, String state,
                                            List<GameTargetSimpleResponseDTO> targets) {
        public static GameGoalSimpleResponseDTO fromEntity(ScenarioGoalEntity goalEntity, Language language) {
            ScenarioStepEntity stepEntity = goalEntity.getStep();
            I18n stepLabel = stepEntity.getLabel().toModel();
            List<GameTargetSimpleResponseDTO> targets = stepEntity.getTargets().stream()
                    .map(targetEntity -> GameTargetSimpleResponseDTO.fromEntity(targetEntity, language))
                    .toList();
            return new GameGoalSimpleResponseDTO(goalEntity.getId(), stepLabel.value(language), goalEntity.getState().name(), targets);
        }
    }

    public record GameTargetSimpleResponseDTO(String id, String label) {
        public static GameTargetSimpleResponseDTO fromEntity(ScenarioTargetEntity targetEntity, Language language) {
            I18n targetLabel = targetEntity.getLabel().toModel();
            return new GameTargetSimpleResponseDTO(targetEntity.getId(), targetLabel.value(language));
        }
    }


    @GetMapping({"/{sessionId}/maps", "/{sessionId}/maps/"})
    public List<GameMapResponseDTO> maps(@RequestHeader("Authorization") String rawToken,
                                         @RequestHeader("Language") String languageStr,
                                         @PathVariable("sessionId") String sessionIdStr) {

        //donner la position de l'utilisateur, le back sais tout (update systeme)

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


}



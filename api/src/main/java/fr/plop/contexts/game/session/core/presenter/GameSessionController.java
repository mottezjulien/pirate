package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.game.config.map.persistence.MapEntity;
import fr.plop.contexts.game.config.map.persistence.MapRepository;
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

@RestController
@RequestMapping("/sessions")
public class GameSessionController {

    private final ConnectUseCase connectUseCase;
    private final GameCreateSessionUseCase createUseCase;
    private final GameMoveUseCase moveUseCase;
    private final ScenarioGoalRepository goalRepository;
    private final GameSessionRepository gameSessionRepository;

    public GameSessionController(ConnectUseCase connectUseCase, GameCreateSessionUseCase createUseCase, GameMoveUseCase moveUseCase, ScenarioGoalRepository goalRepository, GameSessionRepository gameSessionRepository) {
        this.connectUseCase = connectUseCase;
        this.createUseCase = createUseCase;
        this.moveUseCase = moveUseCase;
        this.goalRepository = goalRepository;
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

    public record GameGoalSimpleResponseDTO(String id, String label, String state, List<GameTargetSimpleResponseDTO> targets) {
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
        Language language = Language.valueOf(languageStr.toUpperCase());
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        //ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
        //GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));

        List<MapEntity> mapEntities = gameSessionRepository.fullMap(sessionId.value());
        return mapEntities.stream()
                .map(entity -> GameMapResponseDTO.fromModel(entity.toModel(), language))
                .toList();
    }

    public record GameMapResponseDTO(String id, String label, String definition, PointResponseDTO bottomLeft, PointResponseDTO topRight) {

        public static GameMapResponseDTO fromModel(Map model, Language language) {
            return new GameMapResponseDTO(model.id().value(), model.label().value(language),
                    model.definition(),
                    PointResponseDTO.fromModel(model.rect().bottomLeft()),
                    PointResponseDTO.fromModel(model.rect().topRight()));
        }
    }

    public record PointResponseDTO(double lat, double lng) {
        public static PointResponseDTO fromModel(Point model) {
            return new PointResponseDTO(model.lat(), model.lng());
        }
    }


}



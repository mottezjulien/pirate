package fr.plop.contexts.game.session.map.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.subs.image.Image;
import fr.plop.subs.image.ImageDetailsResponseDTO;
import fr.plop.subs.image.ImagePositionDTO;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/sessions/{sessionId}/maps")
public class GameSessionMapController {

    private final ConnectAuthGameSessionUseCase authGameSessionUseCase;
    private final GameConfigCache cache;
    private final GameSessionSituationGetPort situationGetPort;

    public GameSessionMapController(ConnectAuthGameSessionUseCase authGameSessionUseCase, GameConfigCache cache, GameSessionSituationGetPort situationGetPort) {
        this.authGameSessionUseCase = authGameSessionUseCase;
        this.cache = cache;
        this.situationGetPort = situationGetPort;
    }


    @GetMapping({"", "/"})
    public List<ResponseDTO> maps(@RequestHeader("Authorization") String rawSessionToken,
                                  @PathVariable("sessionId") String sessionIdStr) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            final GameSessionContext context = authGameSessionUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));

            final GameSessionSituation situation = situationGetPort.get(context);
            final MapConfig map = cache.map(sessionId);
            return map.select(situation)
                    .map(mapItem -> ResponseDTO.fromModel(mapItem, situation.board().spaceIds()))
                    .toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record ResponseDTO(String id, ImageDetailsResponseDTO image, Pointer pointer) {
        public record Pointer(ImageResponseDTO image, ImagePositionDTO position) {
            public static Pointer fromModel(Image pointer, MapItem.Position position) {
                return new Pointer(ImageResponseDTO.fromModel(pointer), new ImagePositionDTO(position.top(), position.left()));
            }
        }

        public static ResponseDTO fromModel(MapItem mapItem, List<BoardSpace.Id> spaceIds) {
            Pointer pointerDTO = mapItem.optPointer()
                    .flatMap(pointer -> mapItem.selectPosition(spaceIds)
                            .map(position -> Pointer.fromModel(pointer, position)))
                    .orElse(null);
            return new ResponseDTO(mapItem.id().value(), ImageDetailsResponseDTO.fromModel(mapItem.imageGeneric()), pointerDTO);
        }
    }


}



package fr.plop.contexts.game.instance.board.presenter;

import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.generic.position.Rectangle;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/instances/{instanceId}/boards")
public class GameInstanceBoardController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameConfigCache cache;

    public GameInstanceBoardController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameConfigCache cache) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.cache = cache;
    }

    @GetMapping({"", "/"})
    public List<BoardSpaceDTO> boards(@RequestHeader("Authorization") String rawSessionToken,
                                      @PathVariable("instanceId") String instanceIdStr) {
        GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        try {
            authGameInstanceUseCase.findContext(instanceId, new ConnectToken(rawSessionToken));
            final BoardConfig board = cache.board(instanceId);
            return board.spaces().stream()
                    .map(BoardSpaceDTO::fromModel)
                    .toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record BoardSpaceDTO(String id, String label, List<RectangleDTO> rectangles) {
        public static BoardSpaceDTO fromModel(BoardSpace space) {
            return new BoardSpaceDTO(
                    space.id().value(),
                    space.label(),
                    space.rectangles().stream().map(RectangleDTO::fromModel).toList());
        }
    }

    public record RectangleDTO(PointDTO bottomLeft, PointDTO topRight) {
        public static RectangleDTO fromModel(Rectangle rectangle) {
            return new RectangleDTO(
                    PointDTO.fromModel(rectangle.bottomLeft()),
                    PointDTO.fromModel(rectangle.topRight()));
        }
    }

    public record PointDTO(BigDecimal lat, BigDecimal lng) {
        public static PointDTO fromModel(fr.plop.generic.position.Point point) {
            return new PointDTO(point.lat(), point.lng());
        }
    }

}

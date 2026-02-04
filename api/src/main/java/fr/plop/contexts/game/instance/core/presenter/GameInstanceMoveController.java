package fr.plop.contexts.game.instance.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceMoveUseCase;
import fr.plop.generic.position.Point;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/instances/{instanceId}/move")
public class GameInstanceMoveController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameInstanceMoveUseCase moveUseCase;

    public GameInstanceMoveController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameInstanceMoveUseCase moveUseCase) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.moveUseCase = moveUseCase;
    }


    @PostMapping({"", "/"})
    public void move(@RequestHeader("Authorization") String rawSessionToken,
                     @PathVariable("instanceId") String sessionIdStr,
                     @RequestBody GameMoveRequestDTO request) {
        System.out.println("MOVE: " + request.lat() + " " + request.lng());
        GameInstance.Id sessionId = new GameInstance.Id(sessionIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));

            moveUseCase.apply(context, request.toModel());
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.type().name(), e);
        }
    }

    public record GameMoveRequestDTO(double lat, double lng) {
        public Point toModel() {
            return Point.from(lat, lng);
        }
    }

}



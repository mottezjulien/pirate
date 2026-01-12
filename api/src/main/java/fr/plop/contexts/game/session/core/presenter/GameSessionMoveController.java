package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.generic.position.Point;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/sessions/{sessionId}/move")
public class GameSessionMoveController {

    private final ConnectAuthGameSessionUseCase authGameSessionUseCase;
    private final GameMoveUseCase moveUseCase;

    public GameSessionMoveController(ConnectAuthGameSessionUseCase authGameSessionUseCase, GameMoveUseCase moveUseCase) {
        this.authGameSessionUseCase = authGameSessionUseCase;
        this.moveUseCase = moveUseCase;
    }


    @PostMapping({"", "/"})
    public void move(@RequestHeader("Authorization") String rawSessionToken,
                     @PathVariable("sessionId") String sessionIdStr,
                     @RequestBody GameMoveRequestDTO request) {
        System.out.println("MOVE: " + request.lat() + " " + request.lng());
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            final GameSessionContext context = authGameSessionUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));

            moveUseCase.apply(context, request.toModel());
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.type().name(), e);
        }
    }

    public record GameMoveRequestDTO(double lat, double lng) {
        public Point toModel() {
            return Point.from(lat, lng);
        }
    }

}



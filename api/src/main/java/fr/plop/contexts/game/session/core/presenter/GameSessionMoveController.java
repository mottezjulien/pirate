package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.usecase.GameMoveUseCase;
import fr.plop.generic.position.Point;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/sessions/{sessionId}/move")
public class GameSessionMoveController {

    private final ConnectUseCase connectUseCase;
    private final GameMoveUseCase moveUseCase;

    public GameSessionMoveController(ConnectUseCase connectUseCase, GameMoveUseCase moveUseCase) {
        this.connectUseCase = connectUseCase;
        this.moveUseCase = moveUseCase;
    }

    @PostMapping({"", "/"})
    public void move(
            @RequestHeader("Authorization") String rawToken,
            @PathVariable("sessionId") String sessionIdStr,
            @RequestBody GameMoveRequestDTO request) {
        try {
            System.out.println("MOVE: " + request.lat() + " " + request.lng());
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

}



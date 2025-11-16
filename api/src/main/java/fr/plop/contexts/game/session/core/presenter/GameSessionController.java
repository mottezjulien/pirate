package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionCreateUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionGetPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@RequestMapping("/sessions")
public class GameSessionController {

    private final ConnectUseCase connectUseCase;
    private final GameSessionCreateUseCase createUseCase;
    private final GameSessionStartUseCase startUseCase;
    private final GameOverUseCase gameOverUseCase;
    private final GameSessionGetPort getPort;

    public GameSessionController(ConnectUseCase connectUseCase, GameSessionCreateUseCase createUseCase, GameSessionStartUseCase startUseCase, GameOverUseCase gameOverUseCase, GameSessionGetPort getPort) {
        this.connectUseCase = connectUseCase;
        this.createUseCase = createUseCase;
        this.startUseCase = startUseCase;
        this.gameOverUseCase = gameOverUseCase;
        this.getPort = getPort;
    }


    @PostMapping({"", "/"})
    public GameSessionResponseDTO create(
            @RequestHeader("Authorization") String rawToken,
            @RequestBody GameSessionCreateRequest request
    ) {
        try {
            ConnectUser user = connectUseCase.findUserIdByRawToken(new ConnectToken(rawToken));
            Template.Code templateCode = new Template.Code(request.templateCode());
            GameSession.Atom session = createUseCase.apply(templateCode, user.id());
            return GameSessionResponseDTO.fromModel(session);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            if (Objects.requireNonNull(e.type()) == GameException.Type.TEMPLATE_NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The template is not found", e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping({"/{sessionId}/start", "/{sessionId}/start/"})
    public GameSessionResponseDTO start(
            @RequestHeader("Authorization") String rawToken,
            @PathVariable("sessionId") String sessionIdStr) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));
            GameSession.Atom session = startUseCase.apply(sessionId, player.id());
            return GameSessionResponseDTO.fromModel(session);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            switch (e.type()){
                case SESSION_NOT_FOUND, PLAYER_NOT_FOUND, TEMPLATE_NOT_FOUND -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PostMapping({"/{sessionId}/stop", "/{sessionId}/stop/"})
    public GameSessionResponseDTO stop(@RequestHeader("Authorization") String rawToken,
                                         @PathVariable("sessionId") String sessionIdStr) {

        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found", null));

            SessionGameOver gameOver = new SessionGameOver(SessionGameOver.Type.FAILURE_ONE_CONTINUE);
            gameOverUseCase.apply(sessionId, player.id(), gameOver);
            return GameSessionResponseDTO.fromModel(getPort.findById(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }
    @PostMapping({"/{sessionId}", "/{sessionId}/"})
    private GameSessionResponseDTO get(@RequestHeader("Authorization") String rawToken,
                                       String sessionIdStr) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            return GameSessionResponseDTO.fromModel(getPort.findById(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }

    }

    public record GameSessionCreateRequest(String templateCode) {

    }

    public record GameSessionResponseDTO(String id, String label) {
        public static GameSessionResponseDTO fromModel(GameSession.Atom session) {
            return new GameSessionResponseDTO(session.id().value(), session.label());
        }
    }

}



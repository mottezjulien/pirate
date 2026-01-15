package fr.plop.contexts.game.session.core.presenter;


import fr.plop.contexts.connect.domain.ConnectAuthGameSession;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameSessionUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserGetUseCase;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.core.domain.GameException;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.core.domain.port.GameSessionGetPort;
import fr.plop.contexts.game.session.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionUseCase;
import fr.plop.contexts.game.session.core.domain.usecase.GameSessionStartUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/sessions")
public class GameSessionController {

    private final ConnectAuthUserGetUseCase authUserGetUseCase;
    private final ConnectAuthGameSessionUseCase authGameSessionUseCase;
    private final GameSessionUseCase useCase;
    private final GameSessionStartUseCase startUseCase;
    private final GameOverUseCase gameOverUseCase;
    private final GameSessionGetPort getPort;

    public GameSessionController(ConnectAuthUserGetUseCase authUserGetUseCase, ConnectAuthGameSessionUseCase authGameSessionUseCase, GameSessionUseCase useCase, GameSessionStartUseCase startUseCase, GameOverUseCase gameOverUseCase, GameSessionGetPort getPort) {
        this.authUserGetUseCase = authUserGetUseCase;
        this.authGameSessionUseCase = authGameSessionUseCase;
        this.useCase = useCase;
        this.startUseCase = startUseCase;
        this.gameOverUseCase = gameOverUseCase;
        this.getPort = getPort;
    }


    @GetMapping({"", "/"})
    public GameSessionActivedResponseDTO find(@RequestHeader("Authorization") String rawUserToken) {
        try {
            final ConnectAuthUser connectAuthUser = authUserGetUseCase.findByConnectToken(new ConnectToken(rawUserToken));
            final Optional<GameSessionContext> optContext = useCase.find(connectAuthUser.userId());
            if(optContext.isPresent()){
                final ConnectAuthGameSession authGameSession = authGameSessionUseCase.create(connectAuthUser, optContext.get());
                return GameSessionActivedResponseDTO.fromAuthGameSession(authGameSession);
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }

    }

    @PostMapping({"", "/"})
    public GameSessionActivedResponseDTO create(
            @RequestHeader("Authorization") String rawUserToken,
            @RequestBody GameSessionCreateRequest request
    ) {
        try {
            final ConnectAuthUser connectAuthUser = authUserGetUseCase.findByConnectToken(new ConnectToken(rawUserToken));

            final Template.Id templateId = new Template.Id(request.templateId());
            final GameSessionContext context = useCase.create(templateId, connectAuthUser.userId());

            final ConnectAuthGameSession authGameSession = authGameSessionUseCase.create(connectAuthUser, context);

            return GameSessionActivedResponseDTO.fromAuthGameSession(authGameSession);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            if (Objects.requireNonNull(e.type()) == GameException.Type.TEMPLATE_NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The template is not found", e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    public record GameSessionCreateRequest(String templateId) {

    }

    public record GameSessionActivedResponseDTO(String id, String gameToken) {
        public static GameSessionActivedResponseDTO fromAuthGameSession(ConnectAuthGameSession authGameSession) {
            return new GameSessionActivedResponseDTO(authGameSession.context().sessionId().value(),
                    authGameSession.token().value());
        }
    }


    @PostMapping({"/{sessionId}/start", "/{sessionId}/start/"})
    public GameSessionStoppedResponseDTO start(
            @RequestHeader("Authorization") String rawSessionToken,
            @PathVariable("sessionId") String sessionIdStr) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            ConnectAuthGameSession authGameSession = authGameSessionUseCase
                    .findSessionAuth(sessionId, new ConnectToken(rawSessionToken));
            startUseCase.apply(authGameSession);
            return GameSessionStoppedResponseDTO.fromModel(sessionId);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            switch (e.type()) {
                case SESSION_NOT_FOUND, PLAYER_NOT_FOUND, TEMPLATE_NOT_FOUND ->
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PostMapping({"/{sessionId}/stop", "/{sessionId}/stop/"})
    public GameSessionStoppedResponseDTO stop(@RequestHeader("Authorization") String rawSessionToken,
                                              @PathVariable("sessionId") String sessionIdStr) {

        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        try {
            ConnectAuthGameSession authGameSession = authGameSessionUseCase
                    .findSessionAuth(sessionId, new ConnectToken(rawSessionToken));
            SessionGameOver gameOver = new SessionGameOver(SessionGameOver.Type.FAILURE_ONE_CONTINUE);
            gameOverUseCase.apply(authGameSession.context(), gameOver);
            //TODO Remove game session. In UseCase ??? Je pense que non, faire un scheduler ??? Think ... (après, c'est pour le multi donc fuck)
            authGameSessionUseCase.close(authGameSession.id());
            return GameSessionStoppedResponseDTO.fromModel(getPort.findById(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record GameSessionStoppedResponseDTO(String id) {
        public static GameSessionStoppedResponseDTO fromModel(GameSession.Id sessionId) {
            return new GameSessionStoppedResponseDTO(sessionId.value());
        }
    }

}



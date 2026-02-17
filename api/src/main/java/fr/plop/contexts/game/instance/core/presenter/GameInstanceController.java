package fr.plop.contexts.game.instance.core.presenter;


import fr.plop.contexts.connect.domain.ConnectAuthGameInstance;
import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserGetUseCase;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.InstanceGameOver;
import fr.plop.contexts.game.instance.core.domain.usecase.GameOverUseCase;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceUseCase;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceStartUseCase;
import fr.plop.contexts.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/instances")
//TODO, est ce bien découpé ? on veut avoir des info de la session, du jeu (session aussi ?), et du player ???
//PB de syntaxe, on a la game session (la partie), et la game session, la session de connection lié à un joueur d'une partie
//auth versus player
public class GameInstanceController {

    private final ConnectAuthUserGetUseCase authUserGetUseCase;
    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameInstanceUseCase useCase;
    private final GameInstanceStartUseCase startUseCase;
    private final GameOverUseCase gameOverUseCase;

    public GameInstanceController(ConnectAuthUserGetUseCase authUserGetUseCase, ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameInstanceUseCase useCase, GameInstanceStartUseCase startUseCase, GameOverUseCase gameOverUseCase) {
        this.authUserGetUseCase = authUserGetUseCase;
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.useCase = useCase;
        this.startUseCase = startUseCase;
        this.gameOverUseCase = gameOverUseCase;
    }


    @GetMapping({"", "/"})
    public ResponseDTO find(@RequestHeader("Authorization") String rawUserToken) {
        try {
            final ConnectAuthUser connectAuthUser = authUserGetUseCase.findByConnectToken(new ConnectToken(rawUserToken));
            final Optional<GameInstance.Atom> optInstance = useCase.current(connectAuthUser.userId());
            if(optInstance.isPresent()){
                final GameInstance.Atom atom = optInstance.get();
                GameInstanceContext context = toContext(atom, connectAuthUser.userId());
                final ConnectAuthGameInstance authGame = authGameInstanceUseCase.create(connectAuthUser, context);
                return ResponseDTO.fromModel(authGame, atom.state());
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }

    }

    @PostMapping({"", "/"})
    public ResponseDTO create(@RequestHeader("Authorization") String rawUserToken, @RequestBody CreateRequestDTO request) {
        try {
            final ConnectAuthUser connectAuthUser = authUserGetUseCase.findByConnectToken(new ConnectToken(rawUserToken));

            final Template.Id templateId = new Template.Id(request.templateId());
            final GameInstance.Atom atom = useCase.create(templateId, connectAuthUser.userId());
            final GameInstanceContext context = toContext(atom, connectAuthUser.userId());

            final ConnectAuthGameInstance authGame = authGameInstanceUseCase.create(connectAuthUser, context);

            return ResponseDTO.fromModel(authGame, atom.state());
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceException e) {
            if (Objects.requireNonNull(e.type()) == GameInstanceException.Type.TEMPLATE_NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The template is not found", e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private static GameInstanceContext toContext(GameInstance.Atom atom, User.Id userId) {
        return atom.byUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "player not found"));
    }

    @GetMapping({"/{instanceId}", "/{instanceId}/"})
    public ResponseDTO get(@RequestHeader("Authorization") String rawInstanceToken, @PathVariable("instanceId") String instanceIdStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final ConnectToken connectToken = new ConnectToken(rawInstanceToken);
        try {
            final ConnectAuthGameInstance auth = authGameInstanceUseCase.findAuth(instanceId, connectToken);
            GameInstance.Atom atom = useCase.find(instanceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if(atom.byPlayerId(auth.context().playerId()).isEmpty()){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            return ResponseDTO.fromModel(auth, atom.state());
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }


    @PostMapping({"/{instanceId}/start", "/{instanceId}/start/"})
    public ResponseDTO start(@RequestHeader("Authorization") String rawInstanceToken, @PathVariable("instanceId") String instanceIdStr) {
        final GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        final ConnectToken connectToken = new ConnectToken(rawInstanceToken);
        try {
            final ConnectAuthGameInstance authGameInstance = authGameInstanceUseCase
                    .findAuth(instanceId, connectToken);
            startUseCase.apply(authGameInstance);
            return ResponseDTO.fromModel(authGameInstance, GameInstance.State.ACTIVE);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceException e) {
            switch (e.type()) {
                case INSTANCE_NOT_FOUND, PLAYER_NOT_FOUND, TEMPLATE_NOT_FOUND ->
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PostMapping({"/{instanceId}/stop", "/{instanceId}/stop/"})
    public ResponseDTO stop(@RequestHeader("Authorization") String rawInstanceToken,
                            @PathVariable("instanceId") String instanceIdStr) {

        GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        try {
            ConnectAuthGameInstance auth = authGameInstanceUseCase
                    .findAuth(instanceId, new ConnectToken(rawInstanceToken));

            InstanceGameOver gameOver = new InstanceGameOver(InstanceGameOver.Type.FAILURE_ONE_CONTINUE);
            gameOverUseCase.apply(auth.context(), gameOver);

            Optional<ConnectAuthGameInstance> optUpdatedAuth = authGameInstanceUseCase.close(auth.id());
            if (optUpdatedAuth.isPresent()) {
                auth = optUpdatedAuth.get();
            }
            GameInstance.Atom atom = useCase.find(instanceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            return ResponseDTO.fromModel(auth, atom.state());
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record CreateRequestDTO(String templateId) {

    }
     public record ResponseDTO(String id, String playerId, String state, Auth auth) {

        public record Auth(String token, String state) {

        }

        public static ResponseDTO fromModel(ConnectAuthGameInstance authModel, GameInstance.State gameState) {
            Auth auth = new Auth(authModel.token().value(), authModel.status().name());
            return new ResponseDTO(authModel.context().instanceId().value(), authModel.context().playerId().value(), gameState.name(), auth);
        }
    }

}



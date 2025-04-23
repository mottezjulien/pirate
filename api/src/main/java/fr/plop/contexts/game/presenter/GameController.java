package fr.plop.contexts.game.presenter;


import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.usecase.GameCreateUseCase;
import fr.plop.contexts.game.domain.usecase.GameMoveUseCase;
import fr.plop.contexts.template.domain.model.Template;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/games")
public class GameController {

    private final ConnectUseCase connectUseCase;

    private final GameCreateUseCase createUseCase;
    private final GameMoveUseCase moveUseCase;

    public GameController(ConnectUseCase connectUseCase, GameCreateUseCase createUseCase, GameMoveUseCase moveUseCase) {
        this.connectUseCase = connectUseCase;
        this.createUseCase = createUseCase;
        this.moveUseCase = moveUseCase;
    }

    @PostMapping({"", "/"})
    public GameCreateResponse create(
            @RequestHeader("Authorization") String rawToken,
            @RequestBody GameCreateRequest request
    ) {
        try {
            ConnectUser user = connectUseCase.findUserIdByRawToken(new ConnectToken(rawToken));
            Template.Code templateCode = new Template.Code(request.templateCode());
            Game.Atom game = createUseCase.apply(templateCode, user.id());
            return new GameCreateResponse(game.id().value(), game.label());
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

    public record GameCreateRequest(String templateCode) {

    }

    public record GameCreateResponse(String id, String label) {

    }

    //TODO Player token ??

    @PostMapping({"/{gameId}/move", "/{gameId}/move/"})
    public GameMoveResponseDTO move(
            @RequestHeader("Authorization") String rawToken,
            @PathVariable("gameId") String gameIdStr,
            @RequestBody GameMoveRequestDTO request) {
        try {
            ConnectUser user = connectUseCase.findUserIdByRawToken(new ConnectToken(rawToken));
            Game.Id gameId = new Game.Id(gameIdStr);

            moveUseCase.apply(gameId, user.id(), request.toModel());

            return new GameMoveResponseDTO();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.type().name(), e);
        }
    }

    public record GameMoveResponseDTO() {

    }

    public record GameMoveRequestDTO(float lat, float lng) {

        public GameMoveUseCase.Request toModel() {
            return new GameMoveUseCase.Request(new BoardSpace.Point(lat, lng));
        }
    }


}

package fr.plop.contexts.game.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.domain.GameException;
import fr.plop.contexts.game.domain.model.Game;
import fr.plop.contexts.game.domain.usecase.GameCreateUseCase;
import fr.plop.contexts.template.domain.model.Template;
import org.springframework.http.HttpStatus;
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

    private final GameCreateUseCase useCase;

    public GameController(ConnectUseCase connectUseCase, GameCreateUseCase useCase) {
        this.connectUseCase = connectUseCase;
        this.useCase = useCase;
    }

    @PostMapping({"", "/"})
    public GameCreateResponse create(
            @RequestHeader("Authorization") String rawToken,
            @RequestBody GameCreateRequest request
    ) {
        try {
            ConnectUser user = connectUseCase.findUserIdByRawToken(new ConnectToken(rawToken));
            Template.Code templateCode = new Template.Code(request.templateCode());
            Game.Atom game = useCase.apply(templateCode, user.id());
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


}

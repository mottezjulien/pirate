package fr.plop.contexts.game.instance.core.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.instance.core.domain.GameInstanceException;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.usecase.GameInstanceMoveUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    public void move(@RequestHeader("Authorization") String InstanceToken,
                     @PathVariable("instanceId") String instanceIdStr,
                     @RequestBody GameMoveRequestDTO request) {
        GameInstance.Id instanceId = new GameInstance.Id(instanceIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(instanceId, new ConnectToken(InstanceToken));

            List<BoardSpace.Id> spaceIds = request.spaceIds() == null ? List.of()
                    : request.spaceIds().stream().map(BoardSpace.Id::new).toList();
            moveUseCase.apply(context, spaceIds);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        } catch (GameInstanceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.type().name(), e);
        }
    }

    public record GameMoveRequestDTO(List<String> spaceIds) {
    }

}

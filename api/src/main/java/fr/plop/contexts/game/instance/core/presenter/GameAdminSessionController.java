package fr.plop.contexts.game.instance.core.presenter;


import fr.plop.contexts.game.instance.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceEntity;
import fr.plop.contexts.game.instance.core.persistence.GameInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/instances/admin")
public class GameAdminSessionController {

    private final GameInstanceRepository gameInstanceRepository;
    public GameAdminSessionController(GameInstanceRepository gameInstanceRepository) {
        this.gameInstanceRepository = gameInstanceRepository;
    }

    @GetMapping({"", "/"})
    public List<GameAdminSessionResponseDTO> sessions() {
        return gameInstanceRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GameInstanceEntity::getStartAt))
                .map(GameAdminSessionResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping({"/{instanceId}", "/{instanceId}/"})
    public GameInstanceDetailsResponseDTO oneSession(@PathVariable("instanceId") String sessionIdStr) {
        return gameInstanceRepository.fullById(sessionIdStr)
                .map(GameInstanceDetailsResponseDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record GameAdminSessionResponseDTO(String id, String state, String startAt, String overAt) {
        public static GameAdminSessionResponseDTO fromEntity(GameInstanceEntity entity) {
            String startAt = null;
            if (entity.getStartAt() != null) {
                startAt = entity.getStartAt().toString();
            }
            String overAt = null;
            if (entity.getOverAt() != null) {
                overAt = entity.getOverAt().toString();
            }
            return new GameAdminSessionResponseDTO(entity.getId(),
                    entity.getState().name(), startAt, overAt);
        }
    }

    public record GameInstanceDetailsResponseDTO(String id, String state, String startAt, String overAt,
                                                 List<Player> players) {
        public record Player(String id, String state, String userId) {
            public static Player fromEntity(GamePlayerEntity entity) {
                return new Player(entity.getId(), entity.getState().name(), entity.getUser().getId());
            }
        }

        public static GameInstanceDetailsResponseDTO fromEntity(GameInstanceEntity entity) {
            String startAt = entity.getStartAt().toString();
            String overAt = null;
            if (entity.getOverAt() != null) {
                overAt = entity.getOverAt().toString();
            }
            List<Player> players = entity.getPlayers().stream()
                    .map(Player::fromEntity).toList();
            return new GameInstanceDetailsResponseDTO(entity.getId(),
                    entity.getState().name(), startAt, overAt, players);
        }
    }

}



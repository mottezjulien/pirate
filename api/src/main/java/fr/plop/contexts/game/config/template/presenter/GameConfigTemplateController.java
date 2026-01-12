package fr.plop.contexts.game.config.template.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthUserGetUseCase;
import fr.plop.contexts.game.config.template.persistence.TemplateEntity;
import fr.plop.contexts.game.config.template.persistence.TemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Stream;

@RestController
@RequestMapping("/templates")
public class GameConfigTemplateController {

    private final ConnectAuthUserGetUseCase authUserGetUseCase;
    private final TemplateRepository templateRepository;

    public GameConfigTemplateController(ConnectAuthUserGetUseCase authUserGetUseCase, TemplateRepository templateRepository) {
        this.authUserGetUseCase = authUserGetUseCase;
        this.templateRepository = templateRepository;
    }


    @GetMapping({"/{templateId}"})
    public GameConfigTemplateDetailsResponseDTO findOne(
            @RequestHeader("Authorization") String rawUserToken,
            @PathVariable("templateId") String templateIdStr) {
        try {
            authUserGetUseCase.findByConnectToken(new ConnectToken(rawUserToken));
            return templateRepository.findById(templateIdStr)
                    .map(GameConfigTemplateDetailsResponseDTO::fromEntity)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    @GetMapping({"/search"})
    public Stream<GameConfigTemplateResponseDTO> search(
            @RequestHeader("Authorization") String rawUserToken,
            @RequestParam ("code") String code
    ) {
        try {
            authUserGetUseCase.findByConnectToken(new ConnectToken(rawUserToken));
            return templateRepository.findLikeLowerCode(code.toLowerCase())
                    .stream()
                    .map(GameConfigTemplateResponseDTO::fromEntity);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record GameConfigTemplateResponseDTO(String id, String label, int level, String departureAddress) {
        public static GameConfigTemplateResponseDTO fromEntity(TemplateEntity entity) {
            return new GameConfigTemplateResponseDTO(entity.getId(), entity.getLabel(), entity.getLevel(), entity.getDepartureAddress());
        }

    }

    public record GameConfigTemplateDetailsResponseDTO(String id, String label, int level, String description, Location departure) {
        public record Location(String address, Point bottomLeft, Point topRight) {
            public record Point(double lat, double lng) {

            }
            public static Location fromEntity(TemplateEntity entity) {
                return new Location(entity.getDepartureAddress(),
                        new Point(entity.getDepartureBottomLeftLat().doubleValue(), entity.getDepartureBottomLeftLng().doubleValue()),
                        new Point(entity.getDepartureTopRightLat().doubleValue(), entity.getDepartureTopRightLng().doubleValue()));
            }
        }
        public static GameConfigTemplateDetailsResponseDTO fromEntity(TemplateEntity entity) {
            return new GameConfigTemplateDetailsResponseDTO(entity.getId(), entity.getLabel(), entity.getLevel(), entity.getDescription(), Location.fromEntity(entity));
        }
    }

}



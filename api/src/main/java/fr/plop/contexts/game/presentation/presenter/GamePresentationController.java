package fr.plop.contexts.game.presentation.presenter;


import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthUserGetUseCase;
import fr.plop.contexts.game.presentation.domain.Presentation;
import fr.plop.contexts.game.presentation.persistence.GamePresentationEntity;
import fr.plop.contexts.game.presentation.persistence.GamePresentationRepository;

import fr.plop.subs.i18n.domain.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/presentation")
public class GamePresentationController {

    private final ConnectAuthUserGetUseCase authUserGetUseCase;

    private final GamePresentationRepository repository;

    public GamePresentationController(ConnectAuthUserGetUseCase authUserGetUseCase, GamePresentationRepository repository) {
        this.authUserGetUseCase = authUserGetUseCase;
        this.repository = repository;
    }

    @GetMapping({"/search/code"})
    public Stream<SimpleResponseDTO> searchByCode(@RequestHeader("Language") String languageStr,
                                                                  @RequestParam("code") String code) {
        final Language language = Language.valueOf(languageStr.toUpperCase());
        return repository.fetchLabelsTemplateLikeLowerCode(code)
                .stream()
                .map(entity -> SimpleResponseDTO.fromEntity(entity, language));
    }

    @GetMapping({"/search/location"})
    public Stream<SimpleResponseDTO> searchByLocation(@RequestHeader("Language") String languageStr,
                                                      @RequestParam("bottomLeftLat") BigDecimal bottomLeftLat,
                                                      @RequestParam("bottomLeftLng") BigDecimal bottomLeftLng,
                                                      @RequestParam("topRightLat") BigDecimal topRightLat,
                                                      @RequestParam("topRightLng") BigDecimal topRightLng) {
        final Language language = Language.valueOf(languageStr.toUpperCase());
        return repository.searchByLocation(bottomLeftLat, bottomLeftLng, topRightLat, topRightLng)
                .stream()
                .map(entity -> SimpleResponseDTO.fromEntity(entity, language));
    }

    public record SimpleResponseDTO(String presentationId, String gameId, String label, int level,
                                    String departureAddress, Point departurePoint, float rating) {

        record Point(double lat, double lng) {

        }
        public static SimpleResponseDTO fromEntity(GamePresentationEntity entity, Language language) {
            // Ajout du RoundingMode pour éviter les erreurs de division
            BigDecimal departureLat = entity.getDepartureTopRightLat().add(entity.getDepartureBottomLeftLat())
                    .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
            BigDecimal departureLng = entity.getDepartureTopRightLng().add(entity.getDepartureBottomLeftLng())
                    .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
            Point departurePoint = new Point(departureLat.doubleValue(), departureLng.doubleValue());
            return new SimpleResponseDTO(entity.getId(), entity.getGame().getId(),
                    entity.getLabel().toModel().value(language), entity.getLevel(), entity.getDepartureAddress(),
                    departurePoint,
                    entity.getReviewRating());
        }
    }


    @GetMapping({"/{id}"})
    public DetailsResponseDTO findOne(
            @RequestHeader("Authorization") String rawUserToken,
            @RequestHeader("Language") String languageStr,
            @PathVariable("id") String presentationIdStr) {
        final Language language = Language.valueOf(languageStr.toUpperCase());

        try {
            ConnectAuthUser authUser = authUserGetUseCase.findByConnectToken(new ConnectToken(rawUserToken));
            return repository.fullById(presentationIdStr)
                    .map(entity -> entity.toModel(authUser.userId()))
                    .map(model -> DetailsResponseDTO.fromModel(model, language))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record DetailsResponseDTO(String id, String label, String description, int level, Location departure, Float rating,
                                     List<History> histories, List<Achievement> achievements) {
        public static DetailsResponseDTO fromModel(Presentation model, Language language) {
            return new DetailsResponseDTO(model.id().value(), model.label().value(language),
                    model.description().value(language), model.level().value(),
                    Location.fromModel(model.departure()), model.rating(),
                    List.of(), //TODO History
                    model.achievements().stream()
                            .map(Achievement::fromModel).toList());
        }

        public record Location(String address, Point bottomLeft, Point topRight) {
            public static Location fromModel(fr.plop.generic.position.Location model) {
                return new Location(model.address().toString(),
                        Point.fromModel(model.rectangle().bottomLeft()),
                        Point.fromModel(model.rectangle().topRight()));
            }

            public record Point(double lat, double lng) {
                public static Point fromModel(fr.plop.generic.position.Point model) {
                    return new Point(model.lat().doubleValue(), model.lng().doubleValue());
                }
            }
        }

        public record History(String id) {

        }

        public record Achievement(String id) {

            public static Achievement fromModel(Presentation.Achievement model) {
                return new Achievement(model.id().value());
            }
        }

    }

}

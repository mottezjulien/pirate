package fr.plop.contexts.game.instance.map.presenter;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.map.domain.MapObject;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.contexts.game.instance.situation.domain.port.GameInstanceSituationGetPort;
import fr.plop.subs.image.Image;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/instances/{instanceId}/maps")
public class GameInstanceMapController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameConfigCache cache;
    private final GameInstanceSituationGetPort situationGetPort;

    public GameInstanceMapController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameConfigCache cache, GameInstanceSituationGetPort situationGetPort) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.cache = cache;
        this.situationGetPort = situationGetPort;
    }


    @GetMapping({"", "/"})
    public List<ResponseDTO> maps(@RequestHeader("Authorization") String rawSessionToken,
                                  @PathVariable("instanceId") String sessionIdStr) {
        GameInstance.Id sessionId = new GameInstance.Id(sessionIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));

            final GameInstanceSituation situation = situationGetPort.get(context);
            final MapConfig map = cache.map(sessionId);
            return map.select(situation)
                    .map(ResponseDTO::fromModel)
                    .toList();
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record ResponseDTO(String id, String label, ImageDTO image, BoundsDTO bounds,
                               ImageDTO pointer, List<MapObjectDTO> objects) {
        public static ResponseDTO fromModel(MapItem mapItem) {
            return new ResponseDTO(
                    mapItem.id().value(),
                    mapItem.label(),
                    ImageDTO.fromModel(mapItem.image()),
                    BoundsDTO.fromModel(mapItem.bounds()),
                    mapItem.optPointer().map(ImageDTO::fromModel).orElse(null),
                    mapItem.objects().stream().map(MapObjectDTO::fromModel).toList());
        }
    }

    public record ImageDTO(String type, String value) {
        public static ImageDTO fromModel(Image image) {
            return new ImageDTO(image.type().name(), image.value());
        }
    }

    public record PointDTO(BigDecimal lat, BigDecimal lng) {
        public static PointDTO fromModel(fr.plop.generic.position.Point point) {
            return new PointDTO(point.lat(), point.lng());
        }
    }

    public record BoundsDTO(PointDTO bottomLeft, PointDTO topRight) {
        public static BoundsDTO fromModel(fr.plop.generic.position.Rectangle rectangle) {
            return new BoundsDTO(
                    PointDTO.fromModel(rectangle.bottomLeft()),
                    PointDTO.fromModel(rectangle.topRight()));
        }
    }

    public record MapObjectDTO(String id, String label, String type, PointDTO position,
                                String color, ImageDTO image) {
        public static MapObjectDTO fromModel(MapObject object) {
            return switch (object) {
                case MapObject.PointMarker point -> new MapObjectDTO(
                        point.id().value(), point.label(), "POINT",
                        PointDTO.fromModel(point.position()),
                        point.color(), null);
                case MapObject.ImageMarker img -> new MapObjectDTO(
                        img.id().value(), img.label(), "IMAGE",
                        PointDTO.fromModel(img.position()),
                        null, ImageDTO.fromModel(img.image()));
            };
        }
    }

}

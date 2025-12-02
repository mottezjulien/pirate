package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.Image.domain.ImageGeneric;
import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.generic.ImagePoint;
import fr.plop.generic.enumerate.Priority;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.image.Image;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record MapItem(Id id, ImageGeneric imageGeneric,
                      Priority priority, Optional<Condition> optCondition, Optional<Image> optPointer,
                      List<Position> positions) {



    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Position(Id id, BoardSpace.Id spaceId, ImagePoint point, Priority priority) {

        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

        public Position(BoardSpace.Id spaceId, ImagePoint point, Priority priority) {
            this(new Id(), spaceId, point, priority);
        }

        public double top() {
            return point.top();
        }
        public double left() {
            return point.left();
        }
    }

    public MapItem(ImageGeneric imageGeneric) {
        this(imageGeneric, Optional.empty(), List.of());
    }

    public MapItem(ImageGeneric imageGeneric, Condition condition) {
        this(imageGeneric, Priority.byDefault(), Optional.of(condition), Optional.empty(), List.of());
    }


    public MapItem(ImageGeneric imageGeneric, Optional<Image> optPointer, List<Position> positions) {
        this(imageGeneric, Priority.byDefault(), Optional.empty(), optPointer, positions);
    }

    public MapItem(ImageGeneric imageGeneric, Priority priority, Optional<Condition> optCondition, Optional<Image> optPointer, List<Position> positions) {
        this(new Id(), imageGeneric,  priority, optCondition, optPointer, positions);
    }

    public String imageValue() {
        return imageGeneric.imageValue();
    }

    public Image.Type imageType() {
        return imageGeneric.imageType();
    }

    public Stream<ImageObject> imageObjects() {
        return imageGeneric.objects().stream();
    }

    public Optional<Position> selectPosition(List<BoardSpace.Id> spaceIds) {
        return positions.stream().filter(position -> spaceIds.contains(position.spaceId))
                .min(Comparator.comparing(o -> o.priority));
    }

}

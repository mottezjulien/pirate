package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public record Map(Id id, I18n label, Definition definition,
                  Priority priority, List<Position> positions) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Definition(Type type, String value) {
        public enum Type {
            ASSET, WEB
        }
    }

    public record Position(Point point, Priority priority, List<BoardSpace.Id> spaceIds) {
        public record Point(double x, double y) {

        }

        public boolean contains(GamePlayer player) {
            return spaceIds.stream().anyMatch(spaceId -> player.inSpace(spaceId));
        }

    }

    public enum Priority {
        HIGHEST, HIGH, MEDIUM, LOW, LOWEST;

        public int value() {
            return switch (this) {
                case LOWEST -> 1;
                case LOW -> 2;
                case MEDIUM -> 3;
                case HIGH -> 4;
                case HIGHEST -> 5;
            };
        }
    }

    public Optional<Position> selectPosition(GamePlayer player) {
        return positions.stream()
                .filter(position -> position.contains(player))
                .max(Comparator.comparing(o -> o.priority.value()));
    }

}

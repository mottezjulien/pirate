package fr.plop.contexts.game.config.map.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

import java.util.List;

public record MapItem(Id id, I18n label, Image image,
                      Priority priority, List<Position> positions, List<ScenarioConfig.Step.Id> stepIds) {


    public MapItem(I18n label, Image image, Priority priority, List<Position> positions) {
        this(new Id(), label, image, priority, positions, List.of());
    }

    public boolean isSteps(List<ScenarioConfig.Step.Id> stepIds) {
        return stepIds.stream().anyMatch(this::isStep);
    }

    public boolean isStep(ScenarioConfig.Step.Id stepId) {
        return stepIds.contains(stepId);
    }

    public boolean isImageAsset() {
        return image.isAsset();
    }

    public String imagePath() {
        return image.value;
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Image(Type type, String value, Size size) {
        public boolean isAsset() {
            return type == Type.ASSET;
        }

        public enum Type {
            ASSET, WEB
        }

        public record Size(int width, int height) {

        }

    }

    public sealed interface Position permits Position.Zone, Position.Point {

        record Atom(Id id, String label, Priority priority, List<BoardSpace.Id> spaceIds) {
            public Atom(String label, Priority priority, List<BoardSpace.Id> spaceIds) {
                this(new Id(), label, priority, spaceIds);
            }
        }

        record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

        Atom atom();

        default Id id() {
            return atom().id();
        }

        default Priority priority() {
            return atom().priority();
        }

        default String label() {
            return atom().label();
        }

        default List<BoardSpace.Id> spaceIds() {
            return atom().spaceIds();
        }

        record Zone(Atom atom, double top, double left, double bottom, double right) implements Position {

        }

        record Point(Atom atom, double x, double y) implements Position {

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
}

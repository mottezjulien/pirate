package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record ScenarioConfig(Id id, String label, List<Step> steps, List<Possibility> genericPossibilities) {


    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Step(Id id, I18n label, Optional<I18n> optDescription, Integer order, List<Target> targets, List<Possibility> possibilities) {


        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

        public Step() {
            this(List.of());
        }

        public Step(Id id, List<Possibility> possibilities) {
            this(id, new I18n(),  Optional.empty(), 0, List.of(), possibilities);
        }

        public Step(List<Possibility> possibilities) {
            this(List.of(), possibilities);
        }

        public Step(List<Target> targets, List<Possibility> possibilities) {
            this(new Id(), new I18n(), Optional.empty(),  0, targets, possibilities);
        }

        public Optional<Target> targetById(Target.Id targetId) {
            return targets.stream().filter(target -> target.id().equals(targetId)).findFirst();
        }

    }

    public record Target(Id id, I18n label, Optional<I18n> optDescription, boolean optional, List<I18n> hints, Optional<I18n> optAnswer) {
        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }
    }

    public ScenarioConfig() {
        this(List.of());
    }

    public ScenarioConfig(List<Step> steps) {
        this(new Id(), "", steps, List.of());
    }

    public Step firstStep() {
        return orderedSteps().findFirst().orElseThrow();
    }

    public Stream<Step> orderedSteps() {
        return steps.stream().sorted(Comparator.comparing(o -> o.order));
    }

    public Optional<Target> targetById(Target.Id targetId) {
        return steps.stream().flatMap(step -> step.targetById(targetId).stream())
                .findFirst();
    }

}
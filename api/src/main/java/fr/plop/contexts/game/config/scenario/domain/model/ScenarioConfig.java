package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record ScenarioConfig(Id id, String label, List<Step> steps) {

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Step(Id id, I18n label, Integer order, List<Target> targets, List<Possibility> possibilities) {


        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

        public Step() {
            this(List.of());
        }

        public Step(List<Possibility> possibilities) {
            this(List.of(), possibilities);
        }

        public Step(List<Target> targets, List<Possibility> possibilities) {
            this(new Id(), new I18n(), 0, targets, possibilities);
        }

        public Step(Id id, List<Possibility> possibilities) {
            this(id, new I18n(), 0, List.of(), possibilities);
        }

    }

    public record Target(Id id, I18n label, Optional<I18n> desc, boolean optional) {
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
        this(new Id(), "", steps);
    }

    public Step firstStep() {
        return orderedSteps().findFirst().orElseThrow();
    }

    public Stream<Step> orderedSteps() {
        return steps.stream().sorted(Comparator.comparing(o -> o.order));
    }

}
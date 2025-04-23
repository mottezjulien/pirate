package fr.plop.contexts.scenario.domain.model;

import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record Scenario(Id id, String label, List<Step> steps) {

    public record Id(String value) {

        public Id() {
            this(StringTools.generate());
        }

    }
    public record Step(Id id, Optional<I18n> label, List<Target> targets, List<Possibility> possibilities) {

        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

        public boolean isFirst() {
            return possibilities.stream().allMatch(Possibility::isFirst);
        }

    }

    public record Target(Optional<I18n> label, Optional<I18n> desc, boolean optional) {

    }

    public Scenario() {
        this("any scenario label", List.of());
    }

    public Scenario(String label, List<Step> steps) {
        this(new Id(), label, steps);
    }

    public Stream<Step> firstSteps() {
        return steps.stream().filter(Step::isFirst);
    }

}
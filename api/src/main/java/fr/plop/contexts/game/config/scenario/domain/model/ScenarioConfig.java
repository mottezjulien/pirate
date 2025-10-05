package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.subs.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;

public record ScenarioConfig(Id id, String label, List<Step> steps) {

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

        public Step() {
            this(List.of());
        }

        public Step(List<Possibility> possibility) {
            this(List.of(), possibility);
        }

        public Step(List<Target> targets, List<Possibility> possibilities) {
            this(new Id(), Optional.empty(), targets, possibilities);
        }

    }

    public record Target(Id id, Optional<I18n> label, Optional<I18n> desc, boolean optional) {

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
        return steps.getFirst();
    }


}
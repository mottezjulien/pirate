package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.generic.tools.StringTools;

public sealed interface PossibilityRecurrence permits PossibilityRecurrence.Always, PossibilityRecurrence.Times {

    boolean accept(int currentTimes);

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    record Always(Id id) implements PossibilityRecurrence {
        public Always() {
            this(new Id());
        }

        @Override
        public boolean accept(int currentTimes) {
            return true;
        }
    }

    record Times(Id id, int value) implements PossibilityRecurrence {
        public Times(int value) {
            this(new Id(), value);
        }

        @Override
        public boolean accept(int currentTimes) {
            return currentTimes <= value;
        }
    }

}

package fr.plop.contexts.game.config.scenario.domain.model;

public sealed interface PossibilityRecurrence permits PossibilityRecurrence.Always, PossibilityRecurrence.Times {

    record Always() implements PossibilityRecurrence {}

    record Times(int value) implements PossibilityRecurrence {}

    static PossibilityRecurrence once() {
        return new Times(1);
    }

}

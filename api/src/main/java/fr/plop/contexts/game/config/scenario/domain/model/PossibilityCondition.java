package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;

public sealed interface PossibilityCondition permits
        PossibilityCondition.InsideSpace,
        PossibilityCondition.OutsideSpace,
        PossibilityCondition.AbsoluteTime,
        PossibilityCondition.RelativeTimeAfterOtherTrigger,
        PossibilityCondition.InStep {

    record Id(String value) {

        public Id() {
            this(StringTools.generate());
        }
    }

    record InsideSpace(Id id, BoardSpace.Id spaceId) implements PossibilityCondition {

    }

    record OutsideSpace(Id id, BoardSpace.Id spaceId) implements PossibilityCondition {

    }

    record RelativeTimeAfterOtherTrigger(Id id, Id otherTriggerId, Duration duration) implements PossibilityCondition {

    }

    record AbsoluteTime(Id id, Duration duration, BeforeOrAfter beforeOrAfter) implements PossibilityCondition {

    }

    record InStep(Id id, ScenarioConfig.Step.Id stepId) implements PossibilityCondition {

    }

}
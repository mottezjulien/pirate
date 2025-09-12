package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;

public sealed interface PossibilityCondition permits
        PossibilityCondition.InsideSpace,
        PossibilityCondition.OutsideSpace,
        PossibilityCondition.AbsoluteTime,
        PossibilityCondition.RelativeTimeAfterOtherTrigger,
        PossibilityCondition.StepIn,
        PossibilityCondition.StepTarget {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    Id id();

    record InsideSpace(Id id, BoardSpace.Id spaceId) implements PossibilityCondition {

    }

    record OutsideSpace(Id id, BoardSpace.Id spaceId) implements PossibilityCondition {

    }

    record RelativeTimeAfterOtherTrigger(Id id, Id otherTriggerId, Duration duration) implements PossibilityCondition {

    }

    record AbsoluteTime(Id id, Duration duration, BeforeOrAfter beforeOrAfter) implements PossibilityCondition {

    }

    record StepIn(Id id, ScenarioConfig.Step.Id stepId) implements PossibilityCondition {

    }

    record StepTarget(Id id, ScenarioConfig.Target.Id targetId) implements PossibilityCondition {

    }

}
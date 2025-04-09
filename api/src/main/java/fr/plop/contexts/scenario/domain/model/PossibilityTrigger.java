package fr.plop.contexts.scenario.domain.model;

import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;

public sealed interface PossibilityTrigger permits
        PossibilityTrigger.GoInSpace,
        PossibilityTrigger.GoOutSpace,
        PossibilityTrigger.AbsoluteTime,
        PossibilityTrigger.RelativeTimeAfterOtherTrigger {

    record Id(String value) {

        public Id() {
            this(StringTools.generate());
        }

    }

    record GoInSpace(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {

    }

    record GoOutSpace(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {

    }

    record AbsoluteTime(Id id, Duration duration) implements PossibilityTrigger {

    }

    record RelativeTimeAfterOtherTrigger(Id id, Id otherTriggerId, Duration duration) implements PossibilityTrigger {

    }

}
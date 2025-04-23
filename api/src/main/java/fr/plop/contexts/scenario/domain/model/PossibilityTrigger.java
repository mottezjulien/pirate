package fr.plop.contexts.scenario.domain.model;

import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.contexts.event.domain.GameEvent;
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

    default boolean accept(GameEvent event) {
        return false;
    }

    record GoInSpace(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event) {
            return event instanceof GameEvent.GoIn;
        }
    }

    record GoOutSpace(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event) {
            return event instanceof GameEvent.GoOut;
        }
    }

    record AbsoluteTime(Id id, Duration duration) implements PossibilityTrigger {

    }

    record RelativeTimeAfterOtherTrigger(Id id, Id otherTriggerId, Duration duration) implements PossibilityTrigger {

    }

}
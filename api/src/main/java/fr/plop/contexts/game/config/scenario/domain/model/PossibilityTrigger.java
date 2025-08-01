package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.event.domain.GameEvent;
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
            return event instanceof GameEvent.GoIn goInEvent && goInEvent.spaceId().equals(spaceId);
        }
    }

    record GoOutSpace(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event) {
            return event instanceof GameEvent.GoOut goOutEvent && goOutEvent.spaceId().equals(spaceId);
        }
    }

    record AbsoluteTime(Id id, Duration duration) implements PossibilityTrigger {
        //TODO Accept
    }

    record RelativeTimeAfterOtherTrigger(Id id, Id otherTriggerId, Duration duration) implements PossibilityTrigger {
        //TODO Accept
    }

}
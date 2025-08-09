package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.time.TimeUnit;
import fr.plop.generic.tools.StringTools;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public sealed interface PossibilityTrigger permits
        PossibilityTrigger.GoInSpace,
        PossibilityTrigger.GoOutSpace,
        PossibilityTrigger.AbsoluteTime,
        PossibilityTrigger.RelativeTimeAfterOtherPossibility {


    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    Id id();

    default boolean accept(GameEvent event, List<GameAction> actions) {
        return false;
    }

    record GoInSpace(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            return event instanceof GameEvent.GoIn goInEvent && goInEvent.spaceId().equals(spaceId);
        }
    }

    record GoOutSpace(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            return event instanceof GameEvent.GoOut goOutEvent && goOutEvent.spaceId().equals(spaceId);
        }
    }

    record AbsoluteTime(Id id, TimeUnit value) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            if (event instanceof GameEvent.TimeClick timeClickEvent) {
                return timeClickEvent.is(this.value);
            }
            return false;
        }
    }

    record RelativeTimeAfterOtherPossibility(Id id, Possibility.Id otherPossibilityId,
                                             TimeUnit value) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            if (event instanceof GameEvent.TimeClick timeClickEvent) {
                Optional<GameAction> optFirst = optFirst(actions, otherPossibilityId);
                return optFirst.map(first -> first.timeClick().add(value).equals(timeClickEvent.timeUnit()))
                        .orElse(false);
            }
            return false;
        }

        private Optional<GameAction> optFirst(List<GameAction> actions, Possibility.Id possibilityId) {
            return actions.stream()
                    .filter(action -> action.possibilityId().equals(possibilityId))
                    .min(Comparator.comparing(GameAction::timeClick));
        }
    }

}
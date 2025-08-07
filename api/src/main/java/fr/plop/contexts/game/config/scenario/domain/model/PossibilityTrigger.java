package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.time.TimeClick;
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

    record AbsoluteTime(Id id, TimeClick timeClick) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            if (event instanceof GameEvent.EachTimeClick timeClick) {
                return timeClick.is(this.timeClick);
            }
            return false;
        }
    }

    record RelativeTimeAfterOtherPossibility(Id id, Possibility.Id otherPossibilityId,
                                             TimeClick timeClick) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            if (event instanceof GameEvent.EachTimeClick eachTimeClick) {
                Optional<GameAction> optFirst = optFirst(actions, otherPossibilityId);
                return optFirst.map(first -> first.timeClick().add(timeClick).equals(eachTimeClick.timeClick()))
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
package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.tools.StringTools;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public sealed interface PossibilityTrigger permits
        PossibilityTrigger.SpaceGoIn,
        PossibilityTrigger.SpaceGoOut,
        PossibilityTrigger.StepActive,
        PossibilityTrigger.AbsoluteTime,
        PossibilityTrigger.RelativeTimeAfterOtherPossibility,
        PossibilityTrigger.TalkOptionSelect,
        PossibilityTrigger.TalkEnd,
        PossibilityTrigger.ClickMapObject {


    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    Id id();

    default boolean accept(GameEvent event, List<GameAction> previousUserActions) {
        return false;
    }

    record SpaceGoIn(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            return event instanceof GameEvent.GoIn(BoardSpace.Id spaceId1) && spaceId1.equals(spaceId);
        }
    }

    record SpaceGoOut(Id id, BoardSpace.Id spaceId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            return event instanceof GameEvent.GoOut(BoardSpace.Id spaceId1) && spaceId1.equals(spaceId);
        }
    }

    record StepActive(Id id) implements PossibilityTrigger {

    }

    record AbsoluteTime(Id id, GameSessionTimeUnit value) implements PossibilityTrigger {
        public AbsoluteTime(GameSessionTimeUnit value) {
            this(new Id(), value);
        }

        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            if (event instanceof GameEvent.TimeClick timeClickEvent) {
                return timeClickEvent.is(this.value);
            }
            return false;
        }
    }

    record RelativeTimeAfterOtherPossibility(Id id, Possibility.Id otherPossibilityId, GameSessionTimeUnit value) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            if (event instanceof GameEvent.TimeClick(GameSessionTimeUnit timeUnit)) {
                Optional<GameAction> optFirst = optFirst(previousUserActions, otherPossibilityId);
                return optFirst.map(first -> first.timeClick().add(value).equals(timeUnit))
                        .orElse(false);
            }
            return false;
        }

        private Optional<GameAction> optFirst(List<GameAction> previousUserActions, Possibility.Id possibilityId) {
            return previousUserActions.stream()
                    .filter(action -> action.possibilityId().equals(possibilityId))
                    .min(Comparator.comparing(GameAction::timeClick));
        }
    }


    record TalkEnd(Id id, TalkItem.Id talkId) implements PossibilityTrigger {

    }

    record TalkOptionSelect(Id id, TalkItem.Id talkId, TalkItem.Options.Option.Id optionId) implements PossibilityTrigger {

    }

    record ClickMapObject(Id id, String objectReference) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            // TODO: Implement when MapClick GameEvent is added
            return false;
        }
    }

}
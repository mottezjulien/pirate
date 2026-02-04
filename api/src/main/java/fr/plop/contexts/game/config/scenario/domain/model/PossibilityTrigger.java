package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.message.MessageToken;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.instance.core.domain.model.GameAction;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;

import fr.plop.generic.tools.StringTools;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public sealed interface PossibilityTrigger permits
        PossibilityTrigger.And,
        PossibilityTrigger.Or,
        PossibilityTrigger.Not,
        PossibilityTrigger.SpaceGoIn,
        PossibilityTrigger.SpaceGoOut,
        PossibilityTrigger.StepActive,
        PossibilityTrigger.AbsoluteTime,
        PossibilityTrigger.RelativeTimeAfterOtherPossibility,
        PossibilityTrigger.TalkOptionSelect,
        PossibilityTrigger.Talk,
        PossibilityTrigger.TalkInputText,
        PossibilityTrigger.ImageObjectClick,
        PossibilityTrigger.InventoryItemAction,
        PossibilityTrigger.MessageConfirmAnswer {


    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    Id id();

    default boolean accept(GameEvent event, List<GameAction> previousUserActions) {
        return false;
    }

    record And(Id id, List<PossibilityTrigger> triggers) implements PossibilityTrigger {
        public And(List<PossibilityTrigger> triggers) {
            this(new Id(), triggers);
        }

        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            return triggers.stream()
                    .allMatch(t -> t.accept(event, previousUserActions));
        }
    }

    record Or(Id id, List<PossibilityTrigger> triggers) implements PossibilityTrigger {
        public Or(List<PossibilityTrigger> triggers) {
            this(new Id(), triggers);
        }

        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            return triggers.stream()
                    .anyMatch(t -> t.accept(event, previousUserActions));
        }
    }

    record Not(Id id, PossibilityTrigger trigger) implements PossibilityTrigger {
        public Not(PossibilityTrigger trigger) {
            this(new Id(), trigger);
        }

        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            boolean innerResult = trigger.accept(event, previousUserActions);
            boolean result = !innerResult;
            System.out.println("DEBUG Not.accept: inner=" + innerResult + " result=" + result + " event=" + event);
            return result;
        }
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

    record StepActive(Id id, ScenarioConfig.Step.Id stepId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            return event instanceof GameEvent.GoalActive(ScenarioConfig.Step.Id actualStepId) && stepId.equals(actualStepId);
        }
    }

    record AbsoluteTime(Id id, GameInstanceTimeUnit value) implements PossibilityTrigger {
        public AbsoluteTime(GameInstanceTimeUnit value) {
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

    record RelativeTimeAfterOtherPossibility(Id id, Possibility.Id otherPossibilityId, GameInstanceTimeUnit value) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            if (event instanceof GameEvent.TimeClick(GameInstanceTimeUnit timeUnit)) {
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


    record Talk(Id id, TalkItem.Id talkId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            if (event instanceof GameEvent.Talk talkEvent) {
                return talkEvent.talkId().equals(talkId);
            }
            return false;
        }
    }

    record TalkOptionSelect(Id id, TalkItem.Id talkId, TalkItemNext.Options.Option.Id optionId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            if (event instanceof GameEvent.Talk(TalkItem.Id eventTalkId, Optional<TalkItemNext.Options.Option.Id> eventOptOptionId)) {
                return eventTalkId.equals(talkId) && eventOptOptionId.map(optionIdEvent -> optionIdEvent.equals(optionId)).orElse(false);
            }
            return false;
        }
    }

    record TalkInputText(Id id, TalkItem.Id talkId, String value, MatchType matchType) implements PossibilityTrigger {

        public enum MatchType {
            EQUALS, DIFFERENT, ALMOST_EQUALS, COMPLETELY_DIFFERENT,
        }
        
        @Override
        public boolean accept(GameEvent event, List<GameAction> previousUserActions) {
            if (event instanceof GameEvent.TalkInputText(TalkItem.Id eventTalkId, String eventValue)) {
                System.out.println("DEBUG TalkInputText.accept: eventTalkId=" + eventTalkId.value() + " talkId=" + talkId.value() + " eventValue=" + eventValue + " value=" + value + " matchType=" + matchType);
                if (!eventTalkId.equals(talkId)) {
                    System.out.println("DEBUG TalkInputText.accept: talkId mismatch -> false");
                    return false;
                }
                boolean result = switch (matchType) {
                    case EQUALS -> eventValue.equals(value);
                    case DIFFERENT -> !eventValue.equals(value);
                    case ALMOST_EQUALS -> StringTools.almostEquals(eventValue, value);
                    case COMPLETELY_DIFFERENT -> !StringTools.almostEquals(eventValue, value);
                };
                System.out.println("DEBUG TalkInputText.accept: result=" + result);
                return result;
            }
            return false;
        }
    }

    record ImageObjectClick(Id id, ImageObject.Id objectId) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            return event instanceof GameEvent.ImageObjectClick(ImageObject.Id objectIdEvent) && objectId.equals(objectIdEvent);
        }
    }

    record InventoryItemAction(Id id, GameConfigInventoryItem.Id itemId)implements PossibilityTrigger  {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            return event instanceof GameEvent.InventoryItemAction(GameConfigInventoryItem.Id eventTtemId)
                    && itemId.equals(eventTtemId);
        }
    }

    record MessageConfirmAnswer(Id id, MessageToken token, boolean expectedAnswer) implements PossibilityTrigger {
        @Override
        public boolean accept(GameEvent event, List<GameAction> actions) {
            if (event instanceof GameEvent.MessageConfirmAnswer(MessageToken eventMessageToken, boolean eventAnswer)) {
                return token.equals(eventMessageToken) && expectedAnswer == eventAnswer;
            }
            return false;
        }
    }

}
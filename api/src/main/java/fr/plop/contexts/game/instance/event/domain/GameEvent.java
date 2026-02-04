package fr.plop.contexts.game.instance.event.domain;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.message.MessageToken;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;

import java.util.Optional;

public interface GameEvent {

    record GoIn(BoardSpace.Id spaceId) implements GameEvent {

    }

    record GoOut(BoardSpace.Id spaceId) implements GameEvent {

    }

    record TimeClick(GameInstanceTimeUnit timeUnit) implements GameEvent {
        public boolean is(GameInstanceTimeUnit timeUnit) {
            return this.timeUnit.equals(timeUnit);
        }
    }


    record GoalActive(ScenarioConfig.Step.Id stepId) implements GameEvent {

    }

    record Talk(TalkItem.Id talkId, Optional<TalkItemNext.Options.Option.Id> optOptionId) implements GameEvent {

    }

    record TalkInputText(TalkItem.Id talkId, String value) implements GameEvent {

    }

    record ImageObjectClick(ImageObject.Id objectId) implements GameEvent {

    }
    record InventoryItemAction(GameConfigInventoryItem.Id itemId) implements GameEvent  {

    }

    record MessageConfirmAnswer(MessageToken token, boolean answer) implements GameEvent {

    }



}

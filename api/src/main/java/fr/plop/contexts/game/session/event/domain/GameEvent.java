package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import java.util.Optional;

public interface GameEvent {

    record GoIn(BoardSpace.Id spaceId) implements GameEvent {

    }

    record GoOut(BoardSpace.Id spaceId) implements GameEvent {

    }

    record TimeClick(GameSessionTimeUnit timeUnit) implements GameEvent {
        public boolean is(GameSessionTimeUnit timeUnit) {
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

    record ConfirmAnswer(Consequence.Id confirmId, boolean answer) implements GameEvent {

    }

}

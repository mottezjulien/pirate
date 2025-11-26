package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;

public record Possibility(Id id, PossibilityRecurrence recurrence,
                          PossibilityTrigger trigger, Optional<Condition> optCondition,
                          List<Consequence> consequences) {

    public Possibility(PossibilityTrigger trigger, List<Consequence> consequences) {
        this(new PossibilityRecurrence.Always(), trigger, consequences);
    }

    public Possibility(PossibilityRecurrence recurrence, PossibilityTrigger trigger, List<Consequence> consequences) {
        this(new Id(), recurrence, trigger, Optional.empty(), consequences);
    }

    public Possibility(PossibilityRecurrence recurrence, PossibilityTrigger trigger, Condition condition, List<Consequence> consequences) {
        this(new Id(), recurrence, trigger, Optional.of(condition), consequences);
    }


    public boolean accept(GameEvent event, List<GameAction> previousActions, GameSessionSituation situation) {
        int count = (int) previousActions.stream().filter(action -> action.is(id))
                .count();
        return trigger.accept(event, previousActions)
                && optCondition.map(condition -> condition.accept(situation).toBoolean()).orElse(true)
                && recurrence.accept(count);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }


}
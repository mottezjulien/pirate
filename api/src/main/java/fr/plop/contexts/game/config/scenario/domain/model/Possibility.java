package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.tools.StringTools;

import java.util.List;

public record Possibility(
        Id id,
        PossibilityRecurrence recurrence,
        PossibilityTrigger trigger,
        List<PossibilityCondition> conditions,
        AndOrOr conditionType,
        List<PossibilityConsequence> consequences) {

    public boolean isFirst() {
        return conditions.isEmpty();
    }

    public boolean accept(GameEvent event, List<GameAction> actions) {
        int count = (int) actions.stream()
                .filter(action -> action.is(id))
                .count();
        return trigger.accept(event, actions) && recurrence.accept(count);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public Possibility(PossibilityRecurrence recurrence, PossibilityTrigger trigger, List<PossibilityCondition> conditions, AndOrOr conditionType, List<PossibilityConsequence> consequences) {
        this(new Id(), recurrence, trigger, conditions, conditionType, consequences);
    }

}
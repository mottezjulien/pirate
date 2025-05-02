package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.tools.StringTools;

import java.util.List;

public record Possibility(
        Id id,
        PossibilityTrigger trigger,
        List<PossibilityCondition> conditions,
        AndOrOr conditionType,
        List<PossibilityConsequence> consequences) {

    public boolean isFirst() {
        return conditions.isEmpty();
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public Possibility(PossibilityTrigger trigger, List<PossibilityCondition> conditions, AndOrOr conditionType, List<PossibilityConsequence> consequences) {
        this(new Id(), trigger, conditions, conditionType, consequences);
    }

}
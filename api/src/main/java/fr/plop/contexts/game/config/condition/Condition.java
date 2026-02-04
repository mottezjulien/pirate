package fr.plop.contexts.game.config.condition;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.inventory.domain.model.GameConfigInventoryItem;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.generic.enumerate.TrueOrFalse;
import fr.plop.generic.tools.StringTools;

import java.util.List;

public sealed interface Condition permits
        Condition.And,
        Condition.Or,
        Condition.Not,
        Condition.InsideSpace,
        Condition.OutsideSpace,
        Condition.AbsoluteTime,
        Condition.Step,
        Condition.Target,
        Condition.TalkWithCharacter,
        Condition.InventoryHasItem {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }
    Id id();

    TrueOrFalse accept(GameInstanceSituation situation);

    record And(Id id, List<Condition> conditions) implements Condition {
        public And(List<Condition> conditions) {
            this(new Id(), conditions);
        }

        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return conditions.stream()
                    .map(condition -> condition.accept(situation))
                    .reduce(TrueOrFalse::and)
                    .orElse(TrueOrFalse.FALSE);
        }
    }

    record Or(Id id, List<Condition> conditions) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return conditions.stream()
                    .map(condition -> condition.accept(situation))
                    .reduce(TrueOrFalse::or)
                    .orElse(TrueOrFalse.FALSE);
        }
    }

    record Not(Id id, Condition condition) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return condition.accept(situation).inverse();
        }
    }

    record InsideSpace(Id id, BoardSpace.Id spaceId) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return TrueOrFalse.fromBoolean(situation.board().spaceIds().contains(spaceId));
        }
    }

    record OutsideSpace(Id id, BoardSpace.Id spaceId) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return TrueOrFalse.fromBoolean(situation.board().spaceIds().contains(spaceId)).inverse();
        }
    }

    record AbsoluteTime(Id id, GameInstanceTimeUnit value, BeforeOrAfter beforeOrAfter) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            final GameInstanceTimeUnit timeUnit = situation.time().current();
            return switch (beforeOrAfter) {
                case BEFORE -> TrueOrFalse.fromBoolean(timeUnit.isBefore(value));
                case AFTER -> TrueOrFalse.fromBoolean(timeUnit.isAfter(value));
            };
        }
    }

    record Step(Id id, ScenarioConfig.Step.Id stepId) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return TrueOrFalse.fromBoolean(situation.scenario().stepIds().contains(stepId));
        }
    }

    record Target(Id id, ScenarioConfig.Target.Id targetId) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return TrueOrFalse.fromBoolean(situation.scenario().targetIds().contains(targetId));
        }
    }

    record TalkWithCharacter(Id id, TalkCharacter.Id characterId) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return TrueOrFalse.fromBoolean(situation.talk().hasTalk(characterId));
        }
    }

    record InventoryHasItem(Id id, GameConfigInventoryItem.Id itemId) implements Condition {
        @Override
        public TrueOrFalse accept(GameInstanceSituation situation) {
            return TrueOrFalse.fromBoolean(situation.inventory().has(itemId));
        }
    }

}
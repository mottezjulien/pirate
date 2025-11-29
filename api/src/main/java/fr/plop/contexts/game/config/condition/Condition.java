package fr.plop.contexts.game.config.condition;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;
import fr.plop.generic.enumerate.TrueOrFalse;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;

public sealed interface Condition permits
        Condition.And,
        Condition.Or,
        Condition.Not,
        Condition.InsideSpace,
        Condition.OutsideSpace,
        Condition.AbsoluteTime,
        Condition.Step,
        Condition.Target {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    static Optional<Condition> buildAndFromList(List<Condition> conditions) {
        if(conditions.isEmpty()) {
            return Optional.empty();
        }
        if(conditions.size() == 1) {
            return Optional.of(conditions.getFirst());
        }
        return Optional.of(new And(conditions));
    }
    Id id();

    TrueOrFalse accept(GameSessionSituation situation);

    record And(Id id, List<Condition> conditions) implements Condition {
        public And(List<Condition> conditions) {
            this(new Id(), conditions);
        }

        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            return conditions.stream()
                    .map(condition -> condition.accept(situation))
                    .reduce(TrueOrFalse::and)
                    .orElse(TrueOrFalse.FALSE);
        }
    }

    record Or(Id id, List<Condition> conditions) implements Condition {
        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            return conditions.stream()
                    .map(condition -> condition.accept(situation))
                    .reduce(TrueOrFalse::or)
                    .orElse(TrueOrFalse.FALSE);
        }
    }

    record Not(Id id, Condition condition) implements Condition {
        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            return condition.accept(situation).inverse();
        }
    }

    record InsideSpace(Id id, BoardSpace.Id spaceId) implements Condition {
        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            return TrueOrFalse.fromBoolean(situation.board().spaceIds().contains(spaceId));
        }
    }

    record OutsideSpace(Id id, BoardSpace.Id spaceId) implements Condition {
        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            return TrueOrFalse.fromBoolean(situation.board().spaceIds().contains(spaceId)).inverse();
        }
    }

    record AbsoluteTime(Id id, GameSessionTimeUnit value, BeforeOrAfter beforeOrAfter) implements Condition {
        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            final GameSessionTimeUnit timeUnit = situation.time().current();
            return switch (beforeOrAfter) {
                case BEFORE -> TrueOrFalse.fromBoolean(timeUnit.isBefore(value));
                case AFTER -> TrueOrFalse.fromBoolean(timeUnit.isAfter(value));
            };
        }
    }

    record Step(Id id, ScenarioConfig.Step.Id stepId) implements Condition {
        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            return TrueOrFalse.fromBoolean(situation.scenario().stepIds().contains(stepId));
        }
    }

    record Target(Id id, ScenarioConfig.Target.Id targetId) implements Condition {
        @Override
        public TrueOrFalse accept(GameSessionSituation situation) {
            return TrueOrFalse.fromBoolean(situation.scenario().targetIds().contains(targetId));
        }
    }

}
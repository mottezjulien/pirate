package fr.plop.contexts.game.config.condition;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;
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
        Condition.Target {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    enum Result {
        TRUE, FALSE;

        public Result opposite() {
            return switch (this) {
                case TRUE -> Result.FALSE;
                case FALSE -> Result.TRUE;
            };
        }

        public Result and(Result result) {
            return switch (this) {
                case TRUE -> result;
                case FALSE -> Result.FALSE;
            };
        }

        public Result or(Result result) {
            return switch (this) {
                case TRUE -> Result.TRUE;
                case FALSE -> result;
            };
        }
    }

    Id id();

    Result accept(GameSessionSituation situation);


    record And(Id id, List<Condition> conditions) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            return conditions.stream()
                    .map(condition -> condition.accept(situation))
                    .reduce(Result.TRUE, Result::and);
        }
    }

    record Or(Id id, List<Condition> conditions) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            return conditions.stream()
                    .map(condition -> condition.accept(situation))
                    .reduce(Result.FALSE, Result::or);
        }
    }

    record Not(Id id, Condition condition) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            return condition.accept(situation).opposite();
        }
    }

    record InsideSpace(Id id, BoardSpace.Id spaceId) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            if (situation.board().spaceIds().contains(spaceId)) {
                return Result.TRUE;
            }
            return Result.FALSE;
        }
    }

    record OutsideSpace(Id id, BoardSpace.Id spaceId) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            Not not = new Not(new Id(), new InsideSpace(new Id(), spaceId));
            return not.accept(situation);
        }
    }

    record AbsoluteTime(Id id, GameSessionTimeUnit value, BeforeOrAfter beforeOrAfter) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            return switch (beforeOrAfter) {
                case BEFORE -> situation.time().current().isBefore(value) ? Result.TRUE : Result.FALSE;
                case AFTER -> situation.time().current().isAfter(value) ? Result.TRUE : Result.FALSE;
            };
        }

    }

    record Step(Id id, ScenarioConfig.Step.Id stepId) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            if (situation.scenario().stepIds().contains(stepId)) {
                return Result.TRUE;
            }
            return Result.FALSE;
        }
    }

    record Target(Id id, ScenarioConfig.Target.Id targetId) implements Condition {
        @Override
        public Result accept(GameSessionSituation situation) {
            if (situation.scenario().targetIds().contains(targetId)) {
                return Result.TRUE;
            }
            return Result.FALSE;
        }
    }

}
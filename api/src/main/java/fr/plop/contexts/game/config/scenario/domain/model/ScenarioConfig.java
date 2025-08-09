package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record ScenarioConfig(Id id, String label, List<Step> steps) {


    public record Id(String value) {

        public Id() {
            this(StringTools.generate());
        }

    }

    public record Step(Id id, Optional<I18n> label, List<Target> targets, List<Possibility> possibilities) {

        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

        public Step(List<Target> targets, List<Possibility> possibilities) {
            this(new Id(), Optional.empty(), targets, possibilities);
        }

        public boolean isFirst() {
            return possibilities.stream().allMatch(Possibility::isFirst);
        }

    }

    public record Target(Id id, Optional<I18n> label, Optional<I18n> desc, boolean optional) {

        public record Id(String value) {
            public Id() {
                this(StringTools.generate());
            }
        }

    }

    public ScenarioConfig() {
        this("", List.of());
    }

    public ScenarioConfig(String label, List<Step> steps) {
        this(new Id(), label, steps);
    }

    public Stream<Step> firstSteps() {
        return steps.stream().filter(Step::isFirst);
    }

    /*
    public Stream<Possibility> possibilities(GamePlayer.Id playerId) {
        List<ScenarioGoal> actives = activeGoals(playerId).toList();
        return steps.stream()
                .filter(step -> actives.stream().anyMatch(goal -> goal.stepId().equals(step.id())))
                .flatMap(step -> step.possibilities().stream());
    }

    private Stream<ScenarioGoal> activeGoals(GamePlayer.Id playerId) {
        return goals(playerId)
                .filter(goal -> goal.state() == ScenarioGoal.State.ACTIVE);
    }

    public Stream<ScenarioGoal> goals(GamePlayer.Id playerId) {
        return goals.stream().filter(goal -> goal.playerId().equals(playerId));
    }*/

}
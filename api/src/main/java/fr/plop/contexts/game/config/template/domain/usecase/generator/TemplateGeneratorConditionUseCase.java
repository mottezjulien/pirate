package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;

import java.util.List;
import java.util.Optional;


public class TemplateGeneratorConditionUseCase {

    private static final String SEPARATOR = ":";

    private final TemplateGeneratorGlobalCache context;

    public TemplateGeneratorConditionUseCase(TemplateGeneratorGlobalCache context) {
        this.context = context;
    }


    public Condition apply(Tree tree) {
        Tree subTree = tree.sub();
        switch (subTree.header()) {
            case "AND" -> {
                List<Condition> conditions = subTree.children().stream()
                        .map(this::apply).toList();
                return new Condition.And(new Condition.Id(), conditions);
            }
            case "OR" -> {
                List<Condition> conditions = subTree.children().stream()
                        .map(this::apply).toList();
                return new Condition.Or(new Condition.Id(), conditions);
            }
            case "INSIDESPACE" -> {
                String spaceRef = subTree.findByKeyWithUnique("spaceId");
                BoardSpace.Id spaceId = context.reference(spaceRef, BoardSpace.Id.class, new BoardSpace.Id());
                return new Condition.InsideSpace(new Condition.Id(), spaceId);
            }
            case "OUTSIDESPACE" -> {
                String spaceRef = subTree.findByKeyWithUnique("spaceId");
                BoardSpace.Id spaceId = context.reference(spaceRef, BoardSpace.Id.class, new BoardSpace.Id());
                return new Condition.OutsideSpace(new Condition.Id(), spaceId);
            }
            case "ABSOLUTETIME" -> {
                int duration;
                BeforeOrAfter beforeOrAfter = BeforeOrAfter.BEFORE;
                Optional<String> optDuration = subTree.findByKey("Duration");
                if(optDuration.isPresent()){
                    duration = Integer.parseInt(optDuration.get());
                    Optional<String> optBeforeAfter = subTree.findByKey("BeforeOrAfter");
                    if(optBeforeAfter.isPresent()){
                        beforeOrAfter = BeforeOrAfter.valueOf(optBeforeAfter.get().toUpperCase());
                    }
                } else {
                    duration = Integer.parseInt(subTree.params().getFirst());
                    if(subTree.params().size() > 1) {
                        beforeOrAfter = BeforeOrAfter.valueOf(subTree.params().get(1).toUpperCase());
                    }
                }
                return new Condition.AbsoluteTime(new Condition.Id(), GameSessionTimeUnit.ofMinutes(duration), beforeOrAfter);
            }
            case "INSTEP" -> {
                String stepRef = subTree.findByKeyWithUnique("stepId");
                ScenarioConfig.Step.Id stepId = context.reference(stepRef, ScenarioConfig.Step.Id.class, new ScenarioConfig.Step.Id());
                return new Condition.Step(new Condition.Id(),stepId);
            }
            case "STEPTARGET" -> {
                String targetRef = subTree.findByKeyWithUnique("targetId");
                ScenarioConfig.Target.Id targetId = context.reference(targetRef, ScenarioConfig.Target.Id.class, new ScenarioConfig.Target.Id());
                return new Condition.Target(new Condition.Id(), targetId);
            }
        }
        throw new TemplateException("Invalid condition format: " + String.join(SEPARATOR, tree.params()));
    }

}

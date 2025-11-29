package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.TemplateException;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.BeforeOrAfter;

public class TemplateGeneratorConditionUseCase {

    private static final String SEPARATOR = ":";

    private final TemplateGeneratorGlobalCache context;

    public TemplateGeneratorConditionUseCase(TemplateGeneratorGlobalCache context) {
        this.context = context;
    }


    public Condition apply(Tree tree) {

        String type = tree.params().getFirst().toLowerCase();
        switch (type) {
            case "insidespace" -> {
                // Format: "CONDITION:InsideSpace:spaceId:LABEL" ou "CONDITION:InsideSpace:LABEL" (legacy)
                Tree subTree = tree.sub();
                String spaceRef;
                if (subTree.hasUniqueParam()) {
                    spaceRef = subTree.uniqueParam();
                } else if (subTree.hasParamKey("spaceId")) {
                    spaceRef = subTree.paramValue("spaceId");
                } else {
                    throw new TemplateException("InsideSpace missing required parameter: spaceId");
                }

                BoardSpace.Id spaceId = context.getReference(spaceRef, BoardSpace.Id.class).orElseThrow();
                return new Condition.InsideSpace(
                        new Condition.Id(),
                        spaceId
                );
            }
            case "outsidespace" -> {
                if (tree.params().size() == 2) {
                    BoardSpace.Id spaceId = context.getReference(tree.params().get(1), BoardSpace.Id.class).orElseThrow();
                    return new Condition.OutsideSpace(
                            new Condition.Id(),
                            spaceId
                    );
                }
                BoardSpace.Id spaceId = context.getReference(tree.params().get(2), BoardSpace.Id.class).orElseThrow();
                return new Condition.OutsideSpace(new Condition.Id(), spaceId);
            }
            case "absolutetime" -> {
                // Format: "CONDITION:ABSOLUTETIME:Duration:27" -> params=[ABSOLUTETIME, Duration, 27]
                // ou "condition:ABSOLUTETIME:27" -> params=[ABSOLUTETIME, 27]
                Tree subTree = tree.sub();
                String durationStr;
                if (subTree.hasUniqueParam()) {
                    durationStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("Duration")) {
                    durationStr = subTree.paramValue("Duration");
                } else {
                    throw new TemplateException("AbsoluteTime condition missing required parameter: Duration");
                }
                return new Condition.AbsoluteTime(new Condition.Id(), GameSessionTimeUnit.ofMinutes(Integer.parseInt(durationStr)), BeforeOrAfter.BEFORE);
            }
            case "instep" -> {
                // Format: "CONDITION:InStep:stepId:0987" ou "CONDITION:InStep:0987" (legacy)
                Tree subTree = tree.sub();
                String stepIdStr;
                if (subTree.hasUniqueParam()) {
                    stepIdStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("stepId")) {
                    stepIdStr = subTree.paramValue("stepId");
                } else {
                    throw new TemplateException("InStep missing required parameter: stepId");
                }

                return new Condition.Step(
                        new Condition.Id(),
                        new ScenarioConfig.Step.Id(stepIdStr)
                );
            }

            case "steptarget" -> {
                // Format: "CONDITION:StepTarget:targetId:TARGET_REF" ou "CONDITION:StepTarget:TARGET_REF" (legacy)
                Tree subTree = tree.sub();
                String targetIdStr;
                if (subTree.hasUniqueParam()) {
                    targetIdStr = subTree.uniqueParam();
                } else if (subTree.hasParamKey("targetId")) {
                    targetIdStr = subTree.paramValue("targetId");
                } else {
                    throw new TemplateException("StepTarget missing required parameter: targetId");
                }

                return new Condition.Target(
                        new Condition.Id(),
                        new ScenarioConfig.Target.Id(targetIdStr)
                );
            }
        }
        throw new TemplateException("Invalid condition format: " + String.join(SEPARATOR, tree.params()));
    }

}

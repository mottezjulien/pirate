package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;

import java.util.List;
import java.util.Optional;

public class TemplateInitUseCase {

    public interface OutPort {

        boolean isEmpty();

        void createAll(Template template);

    }

    private final OutPort outPort;

    public TemplateInitUseCase(OutPort outPort) {
        this.outPort = outPort;
    }

    public void apply() {
        if (outPort.isEmpty()) {
            outPort.createAll(firstTemplate());
        }
    }

    private Template firstTemplate() {
        BoardConfig board = firstBoard();
        ScenarioConfig scenario = firstScenario(board);
        MapConfig mapConfig = new MapConfig(List.of(new MapConfig.Item(firstMap())));
        return new Template(new Template.Atom(new Template.Id(), new Template.Code("first")), "Chez Wam", "0.0.1",
                scenario, board, mapConfig);
    }

    private BoardConfig firstBoard() {
        Rect bureau = new Rect(
                new Point(45.77808f,  4.80353f),
                new Point(45.77818f,  4.80363f));
        BoardSpace spaceBureau = new BoardSpace("Bureau", BoardSpace.Priority.HIGHEST, List.of(bureau));

        Rect cusine = new Rect(
                new Point(45.7780f, 4.80372f),
                new Point(45.7781f, 4.80382f));
        BoardSpace spaceCuisine = new BoardSpace("Cuisine", BoardSpace.Priority.HIGHEST, List.of(cusine));

        return new BoardConfig(List.of(spaceBureau, spaceCuisine));
    }

    private ScenarioConfig firstScenario(BoardConfig board) {
        ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();
        ScenarioConfig.Target target1 = new ScenarioConfig.Target(Optional.of(i18n("Va au salon")), Optional.empty(), false);
        ScenarioConfig.Target target2 = new ScenarioConfig.Target(Optional.of(i18n("Va à la cuisine")), Optional.of(i18n("là où on fait la cuisine")), true);

        PossibilityTrigger trigger1 = new PossibilityTrigger.GoOutSpace(new PossibilityTrigger.Id(), board.spaces().getFirst().id());
        PossibilityConsequence consequence1 = new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), i18n("Vous êtes sorti du bureau"));
        List<PossibilityConsequence> consequences1 = List.of(consequence1);
        Possibility possibility1 = new Possibility(trigger1, List.of(), AndOrOr.AND, consequences1);

        PossibilityTrigger trigger2 = new PossibilityTrigger.GoInSpace(new PossibilityTrigger.Id(), board.spaces().get(1).id());
        PossibilityConsequence consequence2 = new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), i18n("Vous êtes dans la cuisine"));
        PossibilityConsequence consequence2bis = new PossibilityConsequence.Goal(new PossibilityConsequence.Id(), stepId, ScenarioGoal.State.SUCCESS);
        List<PossibilityConsequence> consequences2 = List.of(consequence2, consequence2bis);
        Possibility possibility2 = new Possibility(trigger2, List.of(), AndOrOr.AND, consequences2);

        ScenarioConfig.Step step1 = new ScenarioConfig.Step(stepId, Optional.of(i18n("Chapitre 1")), List.of(target1, target2), List.of(possibility1, possibility2));

        return new ScenarioConfig("Mon premier scénario", List.of(step1));
    }

    private Map firstMap() {
        Rect rect = new Rect(new Point(45.7780f,  4.80353f), new Point(45.77818f,  4.80382f));
        return new Map(new Map.Id(), i18n("Chez moi"), "asset:assets/game/first/map/map0.png", rect);
    }

    private static I18n i18n(String fr) {
        return new I18n(java.util.Map.of(Language.FR, fr, Language.EN, fr + " en anglais"));
    }

}

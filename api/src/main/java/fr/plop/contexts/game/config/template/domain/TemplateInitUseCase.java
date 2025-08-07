package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.map.domain.Map;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.generic.enumerate.AndOrOr;
import fr.plop.generic.position.Point;
import fr.plop.generic.position.Rect;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class TemplateInitUseCase {

    public interface OutPort {

        boolean isEmpty();

        void create(Template template);

        void deleteAll();
    }

    private final OutPort outPort;

    public TemplateInitUseCase(OutPort outPort) {
        this.outPort = outPort;
    }

    public void apply() {
        /*if (outPort.isEmpty()) {
            outPort.create(firstTemplate());
        }*/
        outPort.deleteAll();
        outPort.create(testFirstTemplate());
        outPort.create(chezWam1Template());
    }

    private Template testFirstTemplate() {
        ScenarioConfig scenario = new ScenarioConfig();
        BoardConfig board = new BoardConfig(List.of());
        MapConfig mapConfig = new MapConfig(List.of());
        return new Template(new Template.Atom(new Template.Id(), new Template.Code("TEST_FIRST")), "Test 1", "0.0.1",
                Duration.ofMinutes(3), scenario, board, mapConfig);
    }

    private Template chezWam1Template() {
        BoardConfig board = chezWamBoard();
        ScenarioConfig scenario = chezWamScenario(board);
        BoardSpace.Id bureau = board.spaces().getFirst().id();
        BoardSpace.Id cuisine = board.spaces().get(1).id();
        MapConfig mapConfig = new MapConfig(List.of(new MapConfig.Item(chezWamFirstMap(bureau, cuisine)), new MapConfig.Item(chezWamSecondMap(bureau))));
        return new Template(new Template.Atom(new Template.Id(), new Template.Code("CHEZWAM1")), "Chez Wam", "0.0.1",
                Duration.ofMinutes(30), scenario, board, mapConfig);
    }

    private BoardConfig chezWamBoard() {

        Rect bureau = new Rect(
                new Point(45.77806f, 4.80351f),
                new Point(45.77820f, 4.80367f));
        BoardSpace spaceBureau = new BoardSpace("Bureau", BoardSpace.Priority.HIGHEST, List.of(bureau));

        Rect cusine = new Rect(
                new Point(45.77798f, 4.8037f),
                new Point(45.77812f, 4.80384f));
        BoardSpace spaceCuisine = new BoardSpace("Cuisine", BoardSpace.Priority.HIGHEST, List.of(cusine));

        return new BoardConfig(List.of(spaceBureau, spaceCuisine));
    }

    private ScenarioConfig chezWamScenario(BoardConfig board) {
        ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();
        ScenarioConfig.Target target1 = new ScenarioConfig.Target(new ScenarioConfig.Target.Id(), Optional.of(i18n("Va au salon")), Optional.empty(), false);
        ScenarioConfig.Target target2 = new ScenarioConfig.Target(new ScenarioConfig.Target.Id(), Optional.of(i18n("Va à la cuisine")), Optional.of(i18n("là où on fait la cuisine")), true);

        PossibilityTrigger triggerGoOut = new PossibilityTrigger.GoOutSpace(new PossibilityTrigger.Id(), board.spaces().getFirst().id());

        PossibilityConsequence consequenceGoalTarget = new PossibilityConsequence.GoalTarget(new PossibilityConsequence.Id(), stepId, target1.id(), ScenarioGoal.State.SUCCESS);
        Possibility possibilityThree = new Possibility(new PossibilityRecurrence.Times(3), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceGoalTarget));

        PossibilityConsequence consequenceAlert = new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), i18n("Vous êtes sorti du bureau"));
        Possibility possibilityAlways = new Possibility(new PossibilityRecurrence.Always(), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceAlert));

        PossibilityTrigger trigger2 = new PossibilityTrigger.GoInSpace(new PossibilityTrigger.Id(), board.spaces().get(1).id());
        PossibilityConsequence consequence2 = new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), i18n("Vous êtes dans la cuisine"));
        PossibilityConsequence consequence2bis = new PossibilityConsequence.Goal(new PossibilityConsequence.Id(), stepId, ScenarioGoal.State.SUCCESS);
        List<PossibilityConsequence> consequences2 = List.of(consequence2, consequence2bis);
        Possibility possibility2 = new Possibility(new PossibilityRecurrence.Times(1), trigger2, List.of(), AndOrOr.AND, consequences2);

        ScenarioConfig.Step step1 = new ScenarioConfig.Step(stepId, Optional.of(i18n("Chapitre 1")), List.of(target1, target2), List.of(possibilityThree, possibilityAlways, possibility2));

        return new ScenarioConfig("Mon premier scénario", List.of(step1));
    }

    private Map chezWamFirstMap(BoardSpace.Id bureau, BoardSpace.Id cusine) {
        Map.Position.Point bureauPoint = new Map.Position.Point(.75, .25);
        Map.Position bureauPosition = new Map.Position(bureauPoint, Map.Priority.HIGH, List.of(bureau));
        Map.Position.Point cusinePoint = new Map.Position.Point(.25, .75);
        Map.Position cusinePosition = new Map.Position(cusinePoint, Map.Priority.HIGH, List.of(cusine));
        List<Map.Position> points = List.of(bureauPosition, cusinePosition);
        return new Map(new Map.Id(), i18n("Chez moi"),
                new Map.Definition(Map.Definition.Type.ASSET, "assets/game/first/map/map0.png"),
                Map.Priority.HIGH, points);
    }

    private Map chezWamSecondMap(BoardSpace.Id bureau) {
        Map.Position.Point bureauPoint = new Map.Position.Point(.5, .5);
        Map.Position bureauPosition = new Map.Position(bureauPoint, Map.Priority.HIGH, List.of(bureau));
        List<Map.Position> points = List.of(bureauPosition);
        return new Map(new Map.Id(), i18n("Bureau"),
                new Map.Definition(Map.Definition.Type.ASSET, "assets/game/first/map/map_bureau.png"),
                Map.Priority.HIGH, points);
    }


    private static I18n i18n(String fr) {
        return new I18n(java.util.Map.of(Language.FR, fr, Language.EN, fr + " en anglais"));
    }

}

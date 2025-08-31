package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.map.domain.MapItem;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.TalkOptions;
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
        outPort.create(chezWam1Template());
        outPort.create(chezWam1TemplateBis());
    }

    private Template chezWam1Template() {
        BoardConfig board = chezWamBoard();
        ScenarioConfig scenario = chezWamScenario(board);
        BoardSpace.Id bureau = board.spaces().getFirst().id();
        BoardSpace.Id cuisine = board.spaces().get(1).id();
        MapConfig mapConfig = new MapConfig(List.of(chezWamFirstMap(bureau, cuisine)));//, new MapConfig.Item(chezWamSecondMap(bureau))));
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

        Consequence consequenceGoalTarget = new Consequence.ScenarioTarget(new Consequence.Id(), stepId, target1.id(), fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.SUCCESS);
        Possibility possibilityThree = new Possibility(new PossibilityRecurrence.Times(3), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceGoalTarget));

        Consequence consequenceAlert = new Consequence.DisplayTalkAlert(new Consequence.Id(), i18n("Vous êtes sorti du bureau"));
        Possibility possibilityAlways = new Possibility(new PossibilityRecurrence.Always(), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceAlert));

        PossibilityTrigger trigger2 = new PossibilityTrigger.GoInSpace(new PossibilityTrigger.Id(), board.spaces().get(1).id());
        Consequence consequence2 = new Consequence.DisplayTalkAlert(new Consequence.Id(), i18n("Vous êtes dans la cuisine"));
        Consequence consequence2bis = new Consequence.ScenarioStep(new Consequence.Id(), stepId, fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.SUCCESS);
        List<Consequence> consequences2 = List.of(consequence2, consequence2bis);
        Possibility possibility2 = new Possibility(new PossibilityRecurrence.Times(1), trigger2, List.of(), AndOrOr.AND, consequences2);

        ScenarioConfig.Step step1 = new ScenarioConfig.Step(stepId, Optional.of(i18n("Chapitre 1")), List.of(target1, target2), List.of(possibilityThree, possibilityAlways, possibility2));

        return new ScenarioConfig("Mon premier scénario", List.of(step1));
    }

    private MapItem chezWamFirstMap(BoardSpace.Id bureau, BoardSpace.Id cusine) {
        MapItem.Position.Atom bureauZoneAtom = new MapItem.Position.Atom("bureau", MapItem.Priority.HIGH, List.of(bureau));
        MapItem.Position.Zone bureauZone = new MapItem.Position.Zone(bureauZoneAtom, 11.666656494140625, 144.5893837668679, 250.2560555197976, 209.0);
        //Map.Position.Point cusinePoint = new Map.Position.Point(.25, .75);
        //Map.Position cusinePosition = new Map.Position(cusinePoint, Map.Priority.HIGH, List.of(cusine));
        List<MapItem.Position> positions = List.of(bureauZone);//, cusinePosition);
        MapItem.Image.Size size = new MapItem.Image.Size(800, 600);
        MapItem.Image image = new MapItem.Image(MapItem.Image.Type.ASSET, "assets/game/first/map/map0.png", size);
        return new MapItem(i18n("Chez moi"), image, MapItem.Priority.HIGH, positions);
    }


    private Template chezWam1TemplateBis() {
        BoardConfig board = chezWamBoardBis();
        ScenarioConfig scenario = chezWamScenarioBis(board);
        BoardSpace.Id bureau = board.spaces().getFirst().id();
        //BoardSpace.Id cuisine = board.spaces().get(1).id();
        MapConfig mapConfig = new MapConfig(List.of(chezWamFirstMapBis(bureau)));
        return new Template(new Template.Atom(new Template.Id(), new Template.Code("CHEZWAM2")), "Chez Wam bis", "0.0.2",
                Duration.ofMinutes(10), scenario, board, mapConfig);
    }

    private BoardConfig chezWamBoardBis() {
        Rect bureau = new Rect(
                new Point(45.77806f, 4.80351f),
                new Point(45.77820f, 4.80367f));
        BoardSpace spaceBureau = new BoardSpace("Bureau", BoardSpace.Priority.HIGHEST, List.of(bureau));

        Rect cusine = new Rect(
                new Point(45.77798f, 4.8037f),
                new Point(45.77812f, 4.80384f));
        BoardSpace spaceCuisine = new BoardSpace("Cuisine", BoardSpace.Priority.HIGHEST, List.of(cusine));
        //TODO more ....
        return new BoardConfig(List.of(spaceBureau, spaceCuisine));
    }


    private ScenarioConfig chezWamScenarioBis(BoardConfig board) {
        /*ScenarioConfig.Step.Id stepId = new ScenarioConfig.Step.Id();
        ScenarioConfig.Target target1 = new ScenarioConfig.Target(new ScenarioConfig.Target.Id(), Optional.of(i18n("Va au salon")), Optional.empty(), false);
        ScenarioConfig.Target target2 = new ScenarioConfig.Target(new ScenarioConfig.Target.Id(), Optional.of(i18n("Va à la cuisine")), Optional.of(i18n("là où on fait la cuisine")), true);

        PossibilityTrigger triggerGoOut = new PossibilityTrigger.GoOutSpace(new PossibilityTrigger.Id(), board.spaces().getFirst().id());

        Consequence consequenceGoalTarget = new Consequence.ScenarioGoalTarget(new Consequence.Id(), stepId, target1.id(), fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.SUCCESS);
        Possibility possibilityThree = new Possibility(new PossibilityRecurrence.Times(3), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceGoalTarget));

        Consequence consequenceAlert = new Consequence.Alert(new Consequence.Id(), i18n("Vous êtes sorti du bureau"));
        Possibility possibilityAlways = new Possibility(new PossibilityRecurrence.Always(), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceAlert));

        PossibilityTrigger trigger2 = new PossibilityTrigger.GoInSpace(new PossibilityTrigger.Id(), board.spaces().get(1).id());
        Consequence consequence2 = new Consequence.Alert(new Consequence.Id(), i18n("Vous êtes dans la cuisine"));
        Consequence consequence2bis = new Consequence.ScenarioGoal(new Consequence.Id(), stepId, fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.SUCCESS);
        List<Consequence> consequences2 = List.of(consequence2, consequence2bis);
        Possibility possibility2 = new Possibility(new PossibilityRecurrence.Times(1), trigger2, List.of(), AndOrOr.AND, consequences2);

        ScenarioConfig.Step step1 = new ScenarioConfig.Step(stepId, Optional.of(i18n("Chapitre 1")), List.of(target1, target2), List.of(possibilityThree, possibilityAlways, possibility2));

        return new ScenarioConfig("Mon premier scénario", List.of(step1));*/

        ScenarioConfig.Step firstStepBureau = firstStepBureau(board.spaces().getFirst().id());
        //TODO more STEPS ....
        return new ScenarioConfig("Utile", List.of(firstStepBureau));
    }

    private static ScenarioConfig.Step firstStepBureau(BoardSpace.Id bureauBoardId) {

        /// TODO TO FILE !!!

        ScenarioConfig.Step.Id stepOneBureau = new ScenarioConfig.Step.Id();
        ScenarioConfig.Target target1 = new ScenarioConfig.Target(new ScenarioConfig.Target.Id(), Optional.of(i18n("Récupérer une clef")), Optional.of(i18n("Il va falloir cliquer sur le picto main sur la carte du bureau et saisir le clef à l'intérieur")), true);
        ScenarioConfig.Target target2 = new ScenarioConfig.Target(new ScenarioConfig.Target.Id(), Optional.of(i18n("Sortir du bureau")), Optional.empty(), false);

        PossibilityTrigger triggerGoIn = new PossibilityTrigger.GoInSpace(bureauBoardId);
        PossibilityTrigger triggerGoOut = new PossibilityTrigger.GoOutSpace(bureauBoardId);

        I18n oui = i18n("Oui");
        I18n no = i18n("No");

        Consequence outConsequence = new Consequence.ScenarioStep(new Consequence.Id(), stepOneBureau, ScenarioGoal.State.SUCCESS);


        TalkOptions.Option welcomeYes = new TalkOptions.Option(oui);
        TalkOptions.Option welcomeNo = new TalkOptions.Option(no);
        TalkOptions welcomeTalk = new TalkOptions(i18n("Bienvenue, tu veux faire le tutorial ?"), List.of(welcomeYes, welcomeNo));

        //Message de bienvenue, intro
        Consequence welcomeConsequence = new Consequence.DisplayTalkOptions(new Consequence.Id(), welcomeTalk);
        Possibility welcomePossibility = new Possibility(triggerGoIn, welcomeConsequence);

        //After value bienvenue
        PossibilityTrigger welcomeNoTrigger = new PossibilityTrigger.SelectTalkOption(welcomeNo.id());
        Possibility welcomeNoPossibility = new Possibility(welcomeNoTrigger, outConsequence);
        //TODO Yes


        // Message de sorti
        TalkOptions.Option outYes = new TalkOptions.Option(oui);
        TalkOptions.Option outNo = new TalkOptions.Option(no);
        TalkOptions outTalk = new TalkOptions(i18n("Es tu vraiment sur de sortir du bureau ?"), List.of(outYes, outNo));

        Consequence goOutConsequence = new Consequence.DisplayTalkOptions(new Consequence.Id(), outTalk);
        Possibility goOutPossibility = new Possibility(triggerGoOut, goOutConsequence);


        //TODO more Possibilities

        return new ScenarioConfig.Step(stepOneBureau,
                Optional.of(i18n("Introduction - Au bureau")),
                List.of(target1, target2), List.of(welcomePossibility, welcomeNoPossibility, goOutPossibility));
    }

    private MapItem chezWamFirstMapBis(BoardSpace.Id bureau) {
        /*Map.Position.Atom bureauZoneAtom = new Map.Position.Atom("bureau", Map.Priority.HIGH, List.of(bureau));
        Map.Position.Zone bureauZone = new Map.Position.Zone(bureauZoneAtom, 11.666656494140625, 144.5893837668679, 250.2560555197976, 209.0);
        //Map.Position.Point cusinePoint = new Map.Position.Point(.25, .75);
        //Map.Position cusinePosition = new Map.Position(cusinePoint, Map.Priority.HIGH, List.of(cusine));
        List<Map.Position> positions = List.of(bureauZone);//, cusinePosition);
        Map.Image.Size size = new Map.Image.Size(800, 600);
        Map.Image image = new Map.Image(Map.Image.Type.ASSET, "assets/game/first/map/map0.png", size);
        return new Map(i18n("Chez moi"), image, Map.Priority.HIGH, positions);*/

        MapItem.Position.Atom positionAtom = new MapItem.Position.Atom("centre bureau", MapItem.Priority.HIGH, List.of(bureau));
        MapItem.Position.Point pointCenterBureau = new MapItem.Position.Point(positionAtom, 0.459134578704834, 0.5112178509051983);
        MapItem.Image.Size size = new MapItem.Image.Size(800, 600);
        MapItem.Image image = new MapItem.Image(MapItem.Image.Type.ASSET, "assets/game/first/map/map0.png", size);
        return new MapItem(i18n("Details bureau"), image, MapItem.Priority.HIGH, List.of(pointCenterBureau));
    }


    private static I18n i18n(String fr) {
        return new I18n(java.util.Map.of(Language.FR, fr, Language.EN, fr + " en anglais"));
    }

}

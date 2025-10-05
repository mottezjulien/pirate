package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.template.domain.model.Template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        // outPort.create(chezWam1Template());
        Template chezWam = chezWamTemplate();
        if (!chezWam.isValid()) {
            throw new IllegalStateException("Template chez_wam invalide: Talks référencés manquants");
        }
        outPort.create(chezWam);

        Template testDiscussionTemplate = testDiscussionTemplate();
        if (!testDiscussionTemplate.isValid()) {
            throw new IllegalStateException("Template test_discution invalide: Talks référencés manquants");
        }
        outPort.create(testDiscussionTemplate);
    }

    /*private Template chezWam1Template() {
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

        PossibilityTrigger triggerGoOut = new PossibilityTrigger.SpaceGoOut(new PossibilityTrigger.Id(), board.spaces().getFirst().id());

        Consequence consequenceGoalTarget = new Consequence.ScenarioTarget(new Consequence.Id(), stepId, target1.id(), fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal.State.SUCCESS);
        Possibility possibilityThree = new Possibility(new PossibilityRecurrence.Times(3), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceGoalTarget));

        Consequence consequenceAlert = new Consequence.DisplayTalkAlert(new Consequence.Id(), i18n("Vous êtes sorti du bureau"));
        Possibility possibilityAlways = new Possibility(new PossibilityRecurrence.Always(), triggerGoOut, List.of(), AndOrOr.AND, List.of(consequenceAlert));

        PossibilityTrigger trigger2 = new PossibilityTrigger.SpaceGoIn(new PossibilityTrigger.Id(), board.spaces().get(1).id());
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

    private static I18n i18n(String fr) {
        return new I18n(java.util.Map.of(Language.FR, fr, Language.EN, fr + " en anglais"));
    }*/


    private Template chezWamTemplate() {
        try {
            String scriptContent = loadScriptFromResources("template/chez_wam.txt");
            TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();
            TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
            return generator.apply(script);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du script chez_wam.txt", e);
        }
    }

    private Template testDiscussionTemplate() {
        try {
            String scriptContent = loadScriptFromResources("template/test_discution.txt");
            TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();
            TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
            return generator.apply(script);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du script test_discution.txt", e);
        }
    }

    private String loadScriptFromResources(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Fichier non trouvé: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}

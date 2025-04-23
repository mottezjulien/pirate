package fr.plop.contexts.template.domain;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.board.domain.model.BoardSpace;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.contexts.i18n.domain.Language;
import fr.plop.contexts.scenario.domain.model.Possibility;
import fr.plop.contexts.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.scenario.domain.model.Scenario;
import fr.plop.contexts.template.domain.model.Template;
import fr.plop.generic.enumerate.AndOrOr;

import java.util.List;
import java.util.Map;
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
        Board board = firstBoard();
        Scenario scenario = firstScenario(board);
        return new Template(new Template.Atom(new Template.Id(), new Template.Code("first")), "Chez Wam", "0.0.1", scenario, board);
    }



    private Board firstBoard() {
        BoardSpace.Rect bureau = new BoardSpace.Rect(
                new BoardSpace.Point(45.7781f, 4.8036f),
                new BoardSpace.Point(45.77815f, 4.803545f));
        BoardSpace spaceBureau = new BoardSpace("Bureau", BoardSpace.Priority.HIGHEST, List.of(bureau));

        BoardSpace.Rect cusine = new BoardSpace.Rect(
                new BoardSpace.Point(45.7781f, 4.803747f),
                new BoardSpace.Point(45.7780f, 4.803803f));
        BoardSpace spaceCuisine = new BoardSpace("Cuisine", BoardSpace.Priority.HIGHEST, List.of(cusine));

        return new Board(List.of(spaceBureau, spaceCuisine));
    }

    private Scenario firstScenario(Board board) {
        Scenario.Step.Id stepId = new Scenario.Step.Id();
        Scenario.Target target1 = new Scenario.Target(Optional.of(i18n("Va au salon")), Optional.empty(), false);
        Scenario.Target target2 = new Scenario.Target(Optional.of(i18n("Va à la cuisine")), Optional.of(i18n("là où on fait la cuisine")), true);

        PossibilityTrigger trigger1 = new PossibilityTrigger.GoOutSpace(new PossibilityTrigger.Id(), board.space(0).id());
        PossibilityConsequence consequence1 = new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), i18n("Vous êtes sorti du bureau"));
        List<PossibilityConsequence> consequences1 = List.of(consequence1);
        Possibility possibility1 = new Possibility(trigger1, List.of(), AndOrOr.AND, consequences1);

        PossibilityTrigger trigger2 = new PossibilityTrigger.GoInSpace(new PossibilityTrigger.Id(), board.space(1).id());
        PossibilityConsequence consequence2 = new PossibilityConsequence.Alert(new PossibilityConsequence.Id(), i18n("Vous êtes dans la cuisine"));
        PossibilityConsequence consequence2bis = new PossibilityConsequence.SuccessGoal(new PossibilityConsequence.Id(), stepId);
        List<PossibilityConsequence> consequences2 = List.of(consequence2, consequence2bis);
        Possibility possibility2 = new Possibility(trigger2, List.of(), AndOrOr.AND, consequences2);

        Scenario.Step step1 = new Scenario.Step(stepId, Optional.of(i18n("Chapitre 1")), List.of(target1, target2), List.of(possibility1, possibility2));

        return new Scenario("Mon premier scénario", List.of(step1));
    }

    private static I18n i18n(String fr) {
        return new I18n(Map.of(Language.FR, fr, Language.EN, fr + " en anglais"));
    }

    /*

    private Scenario firstScenario(Board board) {

        Scenario.Step.Id stepId = new Scenario.Step.Id();

        I18n rdvGare = new I18n("RDV à la gare desc",
                Map.of(Language.FR, "RDV à la gare en français", Language.EN, "RDV à la gare en anglais"));
        Scenario.Target target1 = new Scenario.Target(Optional.of(rdvGare), Optional.empty(), false);

        I18n goPiscine = new I18n("Go piscine desc",
                Map.of(Language.FR, "Aller à la piscine en français", Language.EN, "Aller à la piscine en anglais"));
        I18n goPiscineDesc = new I18n("Ton bonnet desc",
                Map.of(Language.FR, "Et n'oublie pas ton bonnet en français", Language.EN, "Et n'oublie pas ton bonnet en anglais"));
        Scenario.Target target2 = new Scenario.Target(Optional.of(goPiscine), Optional.of(goPiscineDesc), true);

        PossibilityTrigger trigger1 = new PossibilityTrigger.GoOutSpace(new PossibilityTrigger.Id(), board.spaces().toList().getFirst().id());
        List<PossibilityCondition> conditions1 = List.of();

        PossibilityConsequence consequence1 = new PossibilityConsequence.EndedStep(new PossibilityConsequence.Id(), stepId);
        List<PossibilityConsequence> consequences1 = List.of(consequence1);
        Possibility possibility1 = new Possibility(trigger1, conditions1, AndOrOr.AND, consequences1);

        List<Scenario.Target> targets1 = List.of(target1, target2);
        List<Possibility> possibilities1 = List.of(possibility1);

        I18n firstChapter = new I18n("Premier chapitre desc",
                Map.of(Language.FR, "Premier Chapitre FR", Language.EN, "Premier Chapitre EN"));
        Scenario.Step step1 = new Scenario.Step(stepId, Optional.of(firstChapter), targets1, possibilities1);
        List<Scenario.Step> steps = List.of(step1);
        return new Scenario("Mon premier scénario", steps);
    }

    private Board firstBoard() {
        BoardSpace.Rect rect = new BoardSpace.Rect(
                new BoardSpace.Point(5.098765f, 60.980f),
                new BoardSpace.Point(1.0f, 6.098760f));
        List<BoardSpace> spaces = List.of(new BoardSpace("La gare", 1, List.of(rect)));
        return new Board(spaces);
    }*/

}

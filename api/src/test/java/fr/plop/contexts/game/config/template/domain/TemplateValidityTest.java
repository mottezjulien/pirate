package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TemplateValidityTest {

    @Test
    void isValid_should_be_false_when_displayTalk_references_missing_talk() {
        // Scenario: DisplayTalk -> UNKNOWN
        Consequence.DisplayTalk displayTalk = new Consequence.DisplayTalk(new Consequence.Id(), new TalkItem.Id("UNKNOWN"));
        Possibility possibility = new Possibility(
                new PossibilityRecurrence.Always(),
                new PossibilityTrigger.StepActive(new PossibilityTrigger.Id()),
                List.of(displayTalk)
        );
        ScenarioConfig.Step step = new ScenarioConfig.Step(List.of(possibility));
        ScenarioConfig scenario = new ScenarioConfig(List.of(step));

        Template template = new Template(new Template.Code("T_CODE"), "label", scenario);

        assertFalse(template.isValid());
    }

   /*@Test
    void isValid_should_be_true_when_all_referenced_talks_exist() {
        // Talk MultipleOptions présent
        TalkItem.MultipleOptions.Option optA = new TalkItem.MultipleOptions.Option(i18n("A"));
        TalkItem.MultipleOptions talkOptions = new TalkItem.MultipleOptions(new TalkItem.Id("TALK_OK"), i18n("Question"), TalkCharacter.nobody(), List.of(optA));

        // Scenario: DisplayTalk -> TALK_OK et Trigger TalkNext -> TALK_OK
        Consequence.DisplayTalk displayTalk = new Consequence.DisplayTalk(new Consequence.Id(), new TalkItem.Id("TALK_OK"));
        Possibility possibility = new Possibility(
                new PossibilityRecurrence.Always(),
                new PossibilityTrigger.TalkNext(new PossibilityTrigger.Id(), new TalkItem.Id("TALK_OK"), Optional.empty()),
                List.of(),
                List.of(displayTalk)
        );
        ScenarioConfig.Step step = new ScenarioConfig.Step(new ScenarioConfig.Step.Id(), Optional.empty(), List.of(), List.of(possibility));
        ScenarioConfig scenario = new ScenarioConfig(List.of(step));

        TalkConfig talk = new TalkConfig(List.of(talkOptions));

        Template template = new Template(new Template.Code("T_CODE"), "label", scenario, new BoardConfig(), new MapConfig(), talk);

        assertTrue(template.isValid());
    }*/
}
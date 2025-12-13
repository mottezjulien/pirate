package fr.plop.contexts.game.config.consequence;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionState;
import fr.plop.generic.tools.StringTools;
import fr.plop.subs.i18n.domain.I18n;

public sealed interface Consequence permits
        Consequence.ScenarioStep,
        Consequence.ScenarioTarget,
        Consequence.SessionEnd,
        Consequence.DisplayMessage,
        Consequence.DisplayTalk,
        Consequence.UpdatedMetadata,
        Consequence.ObjetAdd,
        Consequence.ObjetRemove {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    record ScenarioStep(Id id, ScenarioConfig.Step.Id stepId, ScenarioSessionState state) implements Consequence {
        public ScenarioStep(ScenarioConfig.Step.Id stepId, ScenarioSessionState state) {
            this(new Id(), stepId, state);
        }
    }

    record ScenarioTarget(Id id, ScenarioConfig.Target.Id targetId, ScenarioSessionState state) implements Consequence {

    }

    record SessionEnd(Id id, SessionGameOver gameOver) implements Consequence {

    }

    record DisplayMessage(Id id, I18n value) implements Consequence {

    }

    record DisplayTalk(Id id, TalkItem.Id talkId) implements Consequence {

    }

    record UpdatedMetadata(Id id, String metadataId, float value) implements Consequence {

    }

    record ObjetAdd(Id id, String objetId) implements Consequence {

    }

    record ObjetRemove(Id id, String objetId) implements Consequence {

    }

}
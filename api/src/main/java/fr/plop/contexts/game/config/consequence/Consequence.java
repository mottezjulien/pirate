package fr.plop.contexts.game.config.consequence;

import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.TalkOptions;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

public sealed interface Consequence permits
        Consequence.ScenarioStep,
        Consequence.ScenarioTarget,
        Consequence.SessionEnd,
        Consequence.DisplayTalkAlert,
        Consequence.DisplayTalkOptions,
        Consequence.UpdatedMetadata,
        Consequence.ObjetAdd,
        Consequence.ObjetRemove {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    record ScenarioStep(Id id, ScenarioConfig.Step.Id stepId, ScenarioGoal.State state) implements Consequence {

    }

    record ScenarioTarget(Id id, ScenarioConfig.Step.Id stepId, ScenarioConfig.Target.Id targetId,
                          ScenarioGoal.State state) implements Consequence {

    }

    record SessionEnd(Id id, SessionGameOver gameOver) implements Consequence {

    }

    record DisplayTalkAlert(Id id, I18n value) implements Consequence {

    }

    record DisplayTalkOptions(Id id, TalkOptions value) implements Consequence {

    }

    record UpdatedMetadata(Id id, String metadataId, float value) implements Consequence {

    }

    record ObjetAdd(Id id, String objetId) implements Consequence {

    }

    record ObjetRemove(Id id, String objetId) implements Consequence {

    }

}
package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

public sealed interface PossibilityConsequence permits
        PossibilityConsequence.Goal,
        PossibilityConsequence.GoalTarget,
        PossibilityConsequence.GameOver,
        PossibilityConsequence.Alert,
        PossibilityConsequence.UpdatedMetadata,
        PossibilityConsequence.AddObjet,
        PossibilityConsequence.RemoveObjet {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    record Goal(Id id, ScenarioConfig.Step.Id stepId, ScenarioGoal.State state) implements PossibilityConsequence {

    }

    record GoalTarget(Id id, ScenarioConfig.Step.Id stepId, ScenarioConfig.Target.Id targetId,
                      ScenarioGoal.State state) implements PossibilityConsequence {

    }

    record GameOver(Id id,
                    fr.plop.contexts.game.session.core.domain.model.GameOver gameOver) implements PossibilityConsequence {

    }

    record Alert(Id id, I18n message) implements PossibilityConsequence {

    }

    record UpdatedMetadata(Id id, String metadataId, float value) implements PossibilityConsequence {

    }

    record AddObjet(Id id, String objetId) implements PossibilityConsequence {

    }

    record RemoveObjet(Id id, String objetId) implements PossibilityConsequence {

    }

}
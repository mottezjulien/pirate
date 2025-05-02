package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.i18n.domain.I18n;
import fr.plop.generic.tools.StringTools;

public sealed interface PossibilityConsequence permits
        PossibilityConsequence.ActiveGoal,
        PossibilityConsequence.SuccessGoal,
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

    record ActiveGoal(Id id, ScenarioConfig.Step.Id stepId) implements PossibilityConsequence {

    }

    record SuccessGoal(Id id, ScenarioConfig.Step.Id stepId) implements PossibilityConsequence {

    }

    record GameOver(Id id) implements PossibilityConsequence {

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
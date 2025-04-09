package fr.plop.contexts.scenario.domain.model;

import fr.plop.generic.tools.StringTools;

public sealed interface PossibilityConsequence permits
        PossibilityConsequence.StartedStep,
        PossibilityConsequence.EndedStep,
        PossibilityConsequence.GameOver,
        //PossibilityConsequence.FireEvent, // TODO ???
        PossibilityConsequence.UpdatedMetadata,
        PossibilityConsequence.AddObjet,
        PossibilityConsequence.RemoveObjet {

    record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    record StartedStep(Id id, Scenario.Step.Id stepId) implements PossibilityConsequence {

    }

    record EndedStep(Id id, Scenario.Step.Id stepId) implements PossibilityConsequence {

    }

    record GameOver(Id id) implements PossibilityConsequence {

    }

        /*record FireEvent(Id id) implements PossibilityConsequence {

        }*/

    record UpdatedMetadata(Id id, String metadataId, float value) implements PossibilityConsequence {

    }

    record AddObjet(Id id, String objetId) implements PossibilityConsequence {

    }

    record RemoveObjet(Id id, String objetId) implements PossibilityConsequence {

    }

}
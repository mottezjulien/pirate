package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.i18n.persistence.I18nEntity;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_SCENARIO_POSSIBILITY_CONSEQUENCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ScenarioPossibilityConsequenceAbstractEntity {

    @Id
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PossibilityConsequence toModel() {
        return switch (this) {
            case ScenarioPossibilityConsequenceAddObjectEntity addObject -> addObject.toModel();
            case ScenarioPossibilityConsequenceRemoveObjectEntity removeObject -> removeObject.toModel();
            case ScenarioPossibilityConsequenceUpdatedMetadataEntity metadataUpdate -> metadataUpdate.toModel();
            case ScenarioPossibilityConsequenceGameOverEntity gameOver -> gameOver.toModel();
            case ScenarioPossibilityConsequenceGoalEntity goal -> goal.toModel();
            case ScenarioPossibilityConsequenceGoalTargetEntity goalTarget -> goalTarget.toModel();
            case ScenarioPossibilityConsequenceAlertEntity alert -> alert.toModel();
            default -> throw new IllegalStateException("Unknown type");
        };
    }


    public static ScenarioPossibilityConsequenceAbstractEntity fromModel(PossibilityConsequence consequence) {
        return switch (consequence) {
            case PossibilityConsequence.AddObjet addObjet -> {
                ScenarioPossibilityConsequenceAddObjectEntity entity = new ScenarioPossibilityConsequenceAddObjectEntity();
                entity.setId(addObjet.id().value());
                entity.setObjetId(addObjet.objetId());
                yield entity;
            }
            case PossibilityConsequence.RemoveObjet removeObjet -> {
                ScenarioPossibilityConsequenceRemoveObjectEntity entity = new ScenarioPossibilityConsequenceRemoveObjectEntity();
                entity.setId(removeObjet.id().value());
                entity.setObjetId(removeObjet.objetId());
                yield entity;
            }
            case PossibilityConsequence.Goal goal -> {
                ScenarioPossibilityConsequenceGoalEntity entity = new ScenarioPossibilityConsequenceGoalEntity();
                entity.setId(goal.id().value());
                entity.setStepId(goal.stepId().value());
                entity.setState(goal.state());
                yield entity;
            }
            case PossibilityConsequence.GoalTarget goalTarget -> {
                ScenarioPossibilityConsequenceGoalTargetEntity entity = new ScenarioPossibilityConsequenceGoalTargetEntity();
                entity.setId(goalTarget.id().value());
                entity.setStepId(goalTarget.stepId().value());
                entity.setTargetId(goalTarget.targetId().value());
                entity.setState(goalTarget.state());
                yield entity;
            }

            case PossibilityConsequence.GameOver ignored -> new ScenarioPossibilityConsequenceGameOverEntity();
            case PossibilityConsequence.UpdatedMetadata updatedMetadata -> {
                ScenarioPossibilityConsequenceUpdatedMetadataEntity entity = new ScenarioPossibilityConsequenceUpdatedMetadataEntity();
                entity.setId(updatedMetadata.id().value());
                entity.setMetadataId(updatedMetadata.metadataId());
                entity.setValue(updatedMetadata.value());
                yield entity;
            }
            case PossibilityConsequence.Alert alert -> {
                ScenarioPossibilityConsequenceAlertEntity entity = new ScenarioPossibilityConsequenceAlertEntity();
                entity.setId(alert.id().value());
                I18nEntity message = new I18nEntity();
                message.setId(alert.message().id().value());
                entity.setMessage(message);
                yield entity;
            }
        };
    }

}

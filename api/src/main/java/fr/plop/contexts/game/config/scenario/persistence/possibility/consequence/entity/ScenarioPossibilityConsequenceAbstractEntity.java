package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.Image.persistence.ImageItemEntity;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.talk.persistence.TalkItemEntity;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

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

    public Consequence toModel() {
        return switch (this) {
            case ScenarioPossibilityConsequenceAddObjectEntity addObject -> addObject.toModel();
            case ScenarioPossibilityConsequenceRemoveObjectEntity removeObject -> removeObject.toModel();
            case ScenarioPossibilityConsequenceUpdatedMetadataEntity metadataUpdate -> metadataUpdate.toModel();
            case ScenarioPossibilityConsequenceGameOverEntity gameOver -> gameOver.toModel();
            case ScenarioPossibilityConsequenceGoalEntity goal -> goal.toModel();
            case ScenarioPossibilityConsequenceGoalTargetEntity goalTarget -> goalTarget.toModel();
            case ScenarioPossibilityConsequenceMessageEntity alert -> alert.toModel();
            case ScenarioPossibilityConsequenceConfirmEntity confirm -> confirm.toModel();
            case ScenarioPossibilityConsequenceTalkEntity options -> options.toModel();
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }


    public static ScenarioPossibilityConsequenceAbstractEntity fromModel(Consequence consequence) {
        return switch (consequence) {
            case Consequence.ObjetAdd addObjet -> {
                ScenarioPossibilityConsequenceAddObjectEntity entity = new ScenarioPossibilityConsequenceAddObjectEntity();
                entity.setId(addObjet.id().value());
                entity.setObjetId(addObjet.objetId());
                yield entity;
            }
            case Consequence.ObjetRemove removeObjet -> {
                ScenarioPossibilityConsequenceRemoveObjectEntity entity = new ScenarioPossibilityConsequenceRemoveObjectEntity();
                entity.setId(removeObjet.id().value());
                entity.setObjetId(removeObjet.objetId());
                yield entity;
            }
            case Consequence.ScenarioStep goal -> {
                ScenarioPossibilityConsequenceGoalEntity entity = new ScenarioPossibilityConsequenceGoalEntity();
                entity.setId(goal.id().value());
                entity.setStepId(goal.stepId().value());
                entity.setState(goal.state());
                yield entity;
            }
            case Consequence.ScenarioTarget goalTarget -> {
                ScenarioPossibilityConsequenceGoalTargetEntity entity = new ScenarioPossibilityConsequenceGoalTargetEntity();
                entity.setId(goalTarget.id().value());
                //entity.setStepId(goalTarget.stepId().value());
                entity.setTargetId(goalTarget.targetId().value());
                entity.setState(goalTarget.state());
                yield entity;
            }
            case Consequence.SessionEnd sessionEnd -> {
                ScenarioPossibilityConsequenceGameOverEntity entity = new ScenarioPossibilityConsequenceGameOverEntity();
                entity.setId(sessionEnd.id().value());
                entity.setGameOverType(sessionEnd.gameOver().type());
                sessionEnd.gameOver().optReasonId().ifPresent(reasonId -> entity.setLabelId(reasonId.value()));
                yield entity;
            }
            case Consequence.UpdatedMetadata updatedMetadata -> {
                ScenarioPossibilityConsequenceUpdatedMetadataEntity entity = new ScenarioPossibilityConsequenceUpdatedMetadataEntity();
                entity.setId(updatedMetadata.id().value());
                entity.setMetadataId(updatedMetadata.metadataId());
                entity.setValue(updatedMetadata.value());
                yield entity;
            }
            case Consequence.DisplayAlert displayAlert -> {
                ScenarioPossibilityConsequenceMessageEntity entity = new ScenarioPossibilityConsequenceMessageEntity();
                entity.setId(displayAlert.id().value());
                I18nEntity value = new I18nEntity();
                value.setId(displayAlert.value().id().value());
                entity.setValue(value);
                yield entity;
            }
            case Consequence.DisplayConfirm displayConfirm -> {
                ScenarioPossibilityConsequenceConfirmEntity entity = new ScenarioPossibilityConsequenceConfirmEntity();
                entity.setId(displayConfirm.id().value());
                I18nEntity message = new I18nEntity();
                message.setId(displayConfirm.message().id().value());
                entity.setMessage(message);
                yield entity;
            }
            case Consequence.DisplayTalk displayTalk -> {
                ScenarioPossibilityConsequenceTalkEntity entity = new ScenarioPossibilityConsequenceTalkEntity();
                entity.setId(displayTalk.id().value());
                TalkItemEntity talkItemEntity = new TalkItemEntity();
                talkItemEntity.setId(displayTalk.talkId().value());
                entity.setTalk(talkItemEntity);
                yield entity;
            }
            case Consequence.DisplayImage  displayImage -> {
                ScenarioPossibilityConsequenceImageEntity entity = new ScenarioPossibilityConsequenceImageEntity();
                entity.setId(displayImage.id().value());
                ImageItemEntity itemEntity = new ImageItemEntity();
                itemEntity.setId(displayImage.imageId().value());
                entity.setImage(itemEntity);
                yield entity;
            }
        };
    }

}

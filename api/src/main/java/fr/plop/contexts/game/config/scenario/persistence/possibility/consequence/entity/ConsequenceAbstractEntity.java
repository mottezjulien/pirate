package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.Image.persistence.ImageItemEntity;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.talk.persistence.TalkItemEntity;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_CONSEQUENCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ConsequenceAbstractEntity {

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
            case ConsequenceAddItemEntity addObject -> addObject.toModel();
            case ConsequenceRemoveItemEntity removeObject -> removeObject.toModel();
            case ConsequenceUpdatedMetadataEntity metadataUpdate -> metadataUpdate.toModel();
            case ConsequenceGameOverEntity gameOver -> gameOver.toModel();
            case ConsequenceGoalEntity goal -> goal.toModel();
            case ConsequenceGoalTargetEntity goalTarget -> goalTarget.toModel();
            case ConsequenceMessageEntity alert -> alert.toModel();
            case ConsequenceConfirmEntity confirm -> confirm.toModel();
            case ConsequenceTalkEntity options -> options.toModel();
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }


    public static ConsequenceAbstractEntity fromModel(Consequence consequence) {
        return switch (consequence) {
            case Consequence.InventoryAddItem addItem -> {
                ConsequenceAddItemEntity entity = new ConsequenceAddItemEntity();
                entity.setId(addItem.id().value());
                entity.setItemId(addItem.itemId().value());
                yield entity;
            }
            case Consequence.InventoryRemoveItem removeItem -> {
                ConsequenceRemoveItemEntity entity = new ConsequenceRemoveItemEntity();
                entity.setId(removeItem.id().value());
                entity.setItemId(removeItem.itemId().value());
                yield entity;
            }
            case Consequence.ScenarioStep goal -> {
                ConsequenceGoalEntity entity = new ConsequenceGoalEntity();
                entity.setId(goal.id().value());
                entity.setStepId(goal.stepId().value());
                entity.setState(goal.state());
                yield entity;
            }
            case Consequence.ScenarioTarget goalTarget -> {
                ConsequenceGoalTargetEntity entity = new ConsequenceGoalTargetEntity();
                entity.setId(goalTarget.id().value());
                //entity.setStepId(goalTarget.stepId().value());
                entity.setTargetId(goalTarget.targetId().value());
                entity.setState(goalTarget.state());
                yield entity;
            }
            case Consequence.StopPlayer stopPlayer -> {
                ConsequenceGameOverEntity entity = new ConsequenceGameOverEntity();
                entity.setId(stopPlayer.id().value());
                entity.setGameOverType(stopPlayer.gameOver().type());
                stopPlayer.gameOver().optReasonId().ifPresent(reasonId -> entity.setLabelId(reasonId.value()));
                yield entity;
            }
            case Consequence.UpdatedMetadata updatedMetadata -> {
                ConsequenceUpdatedMetadataEntity entity = new ConsequenceUpdatedMetadataEntity();
                entity.setId(updatedMetadata.id().value());
                entity.setMetadataId(updatedMetadata.metadataId());
                entity.setValue(updatedMetadata.value());
                yield entity;
            }
            case Consequence.DisplayAlert displayAlert -> {
                ConsequenceMessageEntity entity = new ConsequenceMessageEntity();
                entity.setId(displayAlert.id().value());
                I18nEntity value = new I18nEntity();
                value.setId(displayAlert.value().id().value());
                entity.setValue(value);
                yield entity;
            }
            case Consequence.DisplayConfirm displayConfirm -> {
                ConsequenceConfirmEntity entity = new ConsequenceConfirmEntity();
                entity.setId(displayConfirm.id().value());
                I18nEntity message = new I18nEntity();
                message.setId(displayConfirm.message().id().value());
                entity.setMessage(message);
                entity.setToken(displayConfirm.token().value());
                yield entity;
            }
            case Consequence.DisplayTalk displayTalk -> {
                ConsequenceTalkEntity entity = new ConsequenceTalkEntity();
                entity.setId(displayTalk.id().value());
                TalkItemEntity talkItemEntity = new TalkItemEntity();
                talkItemEntity.setId(displayTalk.talkId().value());
                entity.setTalk(talkItemEntity);
                yield entity;
            }
            case Consequence.DisplayImage  displayImage -> {
                ConsequenceImageEntity entity = new ConsequenceImageEntity();
                entity.setId(displayImage.id().value());
                ImageItemEntity itemEntity = new ImageItemEntity();
                itemEntity.setId(displayImage.imageId().value());
                entity.setImage(itemEntity);
                yield entity;
            }
        };
    }

}

package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity;


import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST2_SCENARIO_POSSIBILITY_ACTION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ScenarioPossibilityTriggerAbstractEntity {

    @Id
    //@UuidGenerator
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PossibilityTrigger toModel() {
        return switch (this) {
            case ScenarioPossibilityTriggerAbsoluteTimeEntity absoluteTime -> absoluteTime.toModel();
            case ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity relativeTime -> relativeTime.toModel();
            case ScenarioPossibilityTriggerGoInSpaceEntity goInSpace -> goInSpace.toModel();
            case ScenarioPossibilityTriggerGoOutSpaceEntity goOutSpace -> goOutSpace.toModel();
            default -> throw new IllegalStateException("Unknown type");
        };
    }

    public static ScenarioPossibilityTriggerAbstractEntity fromModel(PossibilityTrigger model) {
        return switch (model) {
            case PossibilityTrigger.AbsoluteTime absoluteTime -> {
                ScenarioPossibilityTriggerAbsoluteTimeEntity entity = new ScenarioPossibilityTriggerAbsoluteTimeEntity();
                entity.setId(absoluteTime.id().value());
                entity.setMinutes(absoluteTime.timeClick().minutes());
                yield entity;
            }
            case PossibilityTrigger.RelativeTimeAfterOtherPossibility relativeTimeAfterOther -> {
                ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity entity = new ScenarioPossibilityTriggerRelativeTimeAfterOtherTriggerEntity();
                entity.setId(relativeTimeAfterOther.id().value());
                entity.setMinutes((int) relativeTimeAfterOther.timeClick().minutes());
                entity.setOtherPossibilityId(relativeTimeAfterOther.otherPossibilityId().value());
                yield entity;
            }
            case PossibilityTrigger.GoInSpace goInSpace -> {
                ScenarioPossibilityTriggerGoInSpaceEntity entity = new ScenarioPossibilityTriggerGoInSpaceEntity();
                entity.setId(goInSpace.id().value());
                entity.setSpaceId(goInSpace.spaceId().value());
                yield entity;
            }
            case PossibilityTrigger.GoOutSpace goOutSpace -> {
                ScenarioPossibilityTriggerGoInSpaceEntity entity = new ScenarioPossibilityTriggerGoInSpaceEntity();
                entity.setId(goOutSpace.id().value());
                entity.setSpaceId(goOutSpace.spaceId().value());
                yield entity;
            }
        };
    }

}

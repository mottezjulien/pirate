package fr.plop.contexts.game.config.scenario.persistence.possibility.trigger.entity;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "GO_IN_SPACE")
public final class ScenarioPossibilityTriggerGoInSpaceEntity extends ScenarioPossibilityTriggerAbstractEntity {

    @Column(name = "go_in_space_id")
    private String spaceId;

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public PossibilityTrigger toModel() {
        return new PossibilityTrigger.GoInSpace(new PossibilityTrigger.Id(id), new BoardSpace.Id(spaceId));
    }
}

package fr.plop.contexts.game.config.scenario.persistence.possibility.condition.entity;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityCondition;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INSIDE_SPACE")
public final class ScenarioPossibilityConditionInsideSpaceEntity
        extends ScenarioPossibilityConditionAbstractEntity {

    @Column(name = "inside_space_id")
    private String spaceId;

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public PossibilityCondition toModel() {
        return new PossibilityCondition.InsideSpace(new PossibilityCondition.Id(id), new BoardSpace.Id(spaceId));
    }
}

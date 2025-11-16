package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.SessionGameOver;
import fr.plop.subs.i18n.domain.I18n;
import jakarta.persistence.*;

import java.util.Optional;

@Entity
@DiscriminatorValue("GAME_OVER")
public final class ScenarioPossibilityConsequenceGameOverEntity extends
        ScenarioPossibilityConsequenceAbstractEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "gameover_type")
    private SessionGameOver.Type gameOverType;
    @Column(name = "label_id")
    private String labelId;

    public SessionGameOver.Type getGameOverType() {
        return gameOverType;
    }

    public void setGameOverType(SessionGameOver.Type gameOverType) {
        this.gameOverType = gameOverType;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public Consequence toModel() {
        SessionGameOver gameOver = new SessionGameOver(gameOverType, Optional.ofNullable(labelId).map(I18n.Id::new));
        return new Consequence.SessionEnd(new Consequence.Id(id), gameOver);
    }

}

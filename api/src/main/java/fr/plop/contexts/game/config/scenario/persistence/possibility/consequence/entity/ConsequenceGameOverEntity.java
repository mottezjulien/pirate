package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.instance.core.domain.model.InstanceGameOver;
import fr.plop.subs.i18n.domain.I18n;
import jakarta.persistence.*;

import java.util.Optional;

@Entity
@DiscriminatorValue("GAME_OVER")
public final class ConsequenceGameOverEntity extends
        ConsequenceAbstractEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "gameover_type")
    private InstanceGameOver.Type gameOverType;
    @Column(name = "label_id")
    private String labelId;

    public InstanceGameOver.Type getGameOverType() {
        return gameOverType;
    }

    public void setGameOverType(InstanceGameOver.Type gameOverType) {
        this.gameOverType = gameOverType;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public Consequence toModel() {
        InstanceGameOver gameOver = new InstanceGameOver(gameOverType, Optional.ofNullable(labelId).map(I18n.Id::new));
        return new Consequence.StopPlayer(new Consequence.Id(id), gameOver);
    }

}

package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import fr.plop.contexts.game.session.core.domain.model.GameOver;
import fr.plop.contexts.i18n.domain.I18n;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("GAME_OVER")
public final class ScenarioPossibilityConsequenceGameOverEntity extends
        ScenarioPossibilityConsequenceAbstractEntity {
    
    
    @Enumerated(EnumType.STRING)
    private GameOver.Type type;
    @Column(name = "label_id")
    private String labelId;

    public PossibilityConsequence toModel() {
        GameOver gameOver = new GameOver(type, new I18n.Id(labelId));
        return new PossibilityConsequence.GameOver(new PossibilityConsequence.Id(id), gameOver);
    }

}

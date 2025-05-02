package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.i18n.persistence.I18nEntity;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityConsequence;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("ALERT")
public final class ScenarioPossibilityConsequenceAlertEntity
        extends ScenarioPossibilityConsequenceAbstractEntity {

    @ManyToOne
    @JoinColumn(name = "alert_message_i18n_id")
    private I18nEntity message;

    public I18nEntity getMessage() {
        return message;
    }

    public void setMessage(I18nEntity message) {
        this.message = message;
    }

    public PossibilityConsequence toModel() {
        return new PossibilityConsequence.Alert(new PossibilityConsequence.Id(id), message.toModel());
    }
}

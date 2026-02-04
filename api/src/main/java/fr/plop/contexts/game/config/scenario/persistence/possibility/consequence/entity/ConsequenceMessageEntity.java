package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("MESSAGE")
public final class ConsequenceMessageEntity
        extends ConsequenceAbstractEntity {

    @ManyToOne
    @JoinColumn(name = "message_i18n_id")
    private I18nEntity value;

    public I18nEntity getValue() {
        return value;
    }

    public void setValue(I18nEntity message) {
        this.value = message;
    }

    public Consequence toModel() {
        return new Consequence.DisplayAlert(new Consequence.Id(id), value.toModel());
    }
}

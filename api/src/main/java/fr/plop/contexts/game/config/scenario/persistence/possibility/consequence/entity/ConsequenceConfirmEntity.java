package fr.plop.contexts.game.config.scenario.persistence.possibility.consequence.entity;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.message.MessageToken;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("CONFIRM")
public final class ConsequenceConfirmEntity extends ConsequenceAbstractEntity {

    @ManyToOne
    @JoinColumn(name = "confirm_message_i18n_id")
    private I18nEntity message;
    private String token;

    public I18nEntity getMessage() {
        return message;
    }

    public void setMessage(I18nEntity message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Consequence toModel() {
        return new Consequence.DisplayConfirm(new Consequence.Id(id), message.toModel(), new MessageToken(token));
    }


}

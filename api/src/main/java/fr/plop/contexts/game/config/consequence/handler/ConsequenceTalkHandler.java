package fr.plop.contexts.game.config.consequence.handler;


import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.subs.i18n.domain.I18n;
import org.springframework.stereotype.Component;

@Component
public class ConsequenceTalkHandler implements ConsequenceHandler {

    private final PushPort pushPort;

    private final GamePlayerRepository playerRepository;

    public ConsequenceTalkHandler(PushPort pushPort, GamePlayerRepository playerRepository) {
        this.pushPort = pushPort;
        this.playerRepository = playerRepository;
    }

    @Override
    public boolean supports(Consequence consequence) {
        return consequence instanceof Consequence.DisplayAlert
                || consequence instanceof Consequence.DisplayTalk
                || consequence instanceof Consequence.DisplayConfirm;
    }

    @Override
    public void handle(GameSessionContext context, Consequence consequence) {
        switch (consequence) {
            case Consequence.DisplayAlert message -> message(context, message.value());
            case Consequence.DisplayTalk talk -> talk(context, talk.talkId());
            case Consequence.DisplayConfirm confirm -> confirm(context, confirm);
            default -> throw new IllegalStateException("Unexpected value: " + consequence);
        }
    }

    public void message(GameSessionContext context, I18n message) {
        GamePlayerEntity player = playerRepository.findByIdFetchUser(context.playerId().value()).orElseThrow();
        PushEvent event = new PushEvent.Message(context, message.value(player.getUser().getLanguage()));
        pushPort.push(event);
    }

    public void talk(GameSessionContext context, TalkItem.Id talkId) {
        PushEvent event = new PushEvent.Talk(context, talkId);
        pushPort.push(event);
    }

    public void confirm(GameSessionContext context, Consequence.DisplayConfirm confirm) {
        GamePlayerEntity player = playerRepository.findByIdFetchUser(context.playerId().value()).orElseThrow();
        String message = confirm.message().value(player.getUser().getLanguage());
        PushEvent event = new PushEvent.Confirm(context, confirm.id(), message);
        pushPort.push(event);
    }

}

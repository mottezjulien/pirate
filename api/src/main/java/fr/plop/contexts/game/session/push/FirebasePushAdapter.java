package fr.plop.contexts.game.session.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import fr.plop.contexts.connect.persistence.entity.ConnectionDeviceEntity;
import fr.plop.contexts.connect.persistence.repository.ConnectionDeviceRepository;
import fr.plop.generic.tools.StringTools;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FirebasePushAdapter implements PushPort {

    private final ConnectionDeviceRepository deviceRepository;

    public FirebasePushAdapter(ConnectionDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void push(PushEvent event) throws PushException {

        Optional<ConnectionDeviceEntity> optUser = deviceRepository.findByPlayerId(event.playerId().value()).stream().findFirst();
        ConnectionDeviceEntity deviceEntity = optUser.orElseThrow(() -> new PushException(PushException.Type.USER_NOT_FOUND));

        String token = deviceEntity.getFirebaseToken();
        if (StringTools.isEmpty(token)) {
            throw new PushException(PushException.Type.INVALID_TOKEN);
        }

        switch (event) {
            case PushEvent.GameStatus gameStatus -> {
                Notification notification = Notification.builder()
                        .setTitle("SYSTEM:UPDATE_STATUS")
                        .setBody("SYSTEM:UPDATE_STATUS")
                        .build();
                doFirebasePush(token, notification);
            }
            case PushEvent.GameMove gameMove -> {
                Notification notification = Notification.builder()
                        .setTitle("SYSTEM:UPDATE_MOVE")
                        .setBody("SYSTEM:UPDATE_MOVE")
                        .build();
                doFirebasePush(token, notification);
            }
        }


    }

    private static void doFirebasePush(String token, Notification notification) throws PushException {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new PushException(PushException.Type.PROVIDER_EXCEPTION, e);
        }
    }

}

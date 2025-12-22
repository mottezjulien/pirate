package fr.plop.contexts.user;

import fr.plop.subs.i18n.domain.Language;

import java.util.Optional;

public class UserOnboardUseCase {

    public record Request(Language language, String nickName, String email) {

    }

    public interface Port {
        Optional<User> findById(User.Id userId);

        void save(User user);
    }

    private final Port port;

    public UserOnboardUseCase(Port port) {
        this.port = port;
    }

    public Optional<User> apply(User.Id userId, Request request) {
        return port.findById(userId).map(oldUser -> {
            final User newUser = oldUser.type(User.Type.ONBOARDED)
                    .language(request.language())
                    .nickName(request.nickName())
                    .email(request.email());
            port.save(newUser);
            return newUser;
        });
    }

}

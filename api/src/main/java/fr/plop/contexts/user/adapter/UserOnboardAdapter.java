package fr.plop.contexts.user.adapter;

import fr.plop.contexts.user.User;
import fr.plop.contexts.user.UserOnboardUseCase;
import fr.plop.contexts.user.persistence.UserEntity;
import fr.plop.contexts.user.persistence.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserOnboardAdapter implements UserOnboardUseCase.Port {

    private final UserRepository userRepository;

    public UserOnboardAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(User.Id userId) {
        return userRepository.findById(userId.value()).map(UserEntity::toModel);
    }

    @Override
    public void save(User user) {
        userRepository.findById(user.id().value())
                .ifPresent(entity -> {
                    entity.setType(user.type());
                    entity.setLanguage(user.language());
                    user.optNickName().ifPresent(entity::setNickName);
                    user.optEmail().ifPresent(entity::setEmail);
                    userRepository.save(entity);
                });
    }
}

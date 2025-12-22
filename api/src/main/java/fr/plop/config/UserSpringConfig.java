package fr.plop.config;


import fr.plop.contexts.user.UserOnboardUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserSpringConfig {
    @Bean
    public UserOnboardUseCase UserOnboardUseCase(UserOnboardUseCase.Port port) {
        return new UserOnboardUseCase(port);
    }
}

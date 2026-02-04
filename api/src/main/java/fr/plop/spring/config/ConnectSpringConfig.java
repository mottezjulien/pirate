package fr.plop.spring.config;


import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import fr.plop.contexts.connect.usecase.ConnectAuthUserGetUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectSpringConfig {

    @Bean
    public ConnectAuthUserCreateUseCase connectionCreateAuthUseCase(ConnectAuthUserCreateUseCase.Port port) {
        return new ConnectAuthUserCreateUseCase(port);
    }

    @Bean
    public ConnectAuthUserGetUseCase connectAuthUserGetUseCase(ConnectAuthUserGetUseCase.Port port) {
        return new ConnectAuthUserGetUseCase(port);
    }

    @Bean
    public ConnectAuthGameInstanceUseCase connectAuthGameInstanceUseCase(ConnectAuthGameInstanceUseCase.Port port) {
        return new ConnectAuthGameInstanceUseCase(port);
    }

}

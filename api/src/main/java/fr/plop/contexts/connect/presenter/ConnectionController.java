package fr.plop.contexts.connect.presenter;


import fr.plop.contexts.connect.domain.ConnectAuth;
import fr.plop.contexts.connect.domain.ConnectionCreateAuthUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connect")
public class ConnectionController {
    private final ConnectionCreateAuthUseCase createAuthUseCase;

    public ConnectionController(ConnectionCreateAuthUseCase createAuthUseCase) {
        this.createAuthUseCase = createAuthUseCase;
    }


    @PostMapping({"", "/"})
    public AuthResponseDTO auth(@RequestBody ConnectionRequestDTO request) {
        ConnectAuth auth = createAuthUseCase.byDeviceId(request.deviceId(), request.firebaseToken());
        return new AuthResponseDTO(auth.token().value());
    }

}

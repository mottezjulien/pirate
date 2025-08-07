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
    public ResponseDTO auth(@RequestBody RequestDTO request) {
        ConnectAuth auth = createAuthUseCase.byDeviceId(request.deviceId());
        return new ResponseDTO(auth.token().value());
    }

    public record RequestDTO(String deviceId) {

    }

    public record ResponseDTO(String token) {

    }

}

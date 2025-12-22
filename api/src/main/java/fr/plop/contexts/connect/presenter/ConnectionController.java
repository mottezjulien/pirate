package fr.plop.contexts.connect.presenter;


import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.usecase.ConnectAuthUserCreateUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connect")
public class ConnectionController {
    private final ConnectAuthUserCreateUseCase createAuthUseCase;

    public ConnectionController(ConnectAuthUserCreateUseCase createAuthUseCase) {
        this.createAuthUseCase = createAuthUseCase;
    }

    @PostMapping({"", "/"})
    public ResponseDTO createAuth(@RequestBody RequestDTO request) {
        ConnectAuthUser auth = createAuthUseCase.byDeviceId(request.deviceId());
        return new ResponseDTO(auth.token().value());
    }

    public record RequestDTO(String deviceId) {

    }

    public record ResponseDTO(String token) {

    }

}

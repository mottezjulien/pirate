package fr.plop.contexts.user.presenter;


import fr.plop.contexts.connect.domain.ConnectAuthUser;
import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthUserGetUseCase;
import fr.plop.contexts.user.User;
import fr.plop.contexts.user.UserOnboardUseCase;
import fr.plop.contexts.user.persistence.UserRepository;
import fr.plop.subs.i18n.domain.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final ConnectAuthUserGetUseCase connectAuthUserGetUseCase;

    private final UserRepository userRepository;

    private final UserOnboardUseCase onBoardUseCase;

    public UserController(ConnectAuthUserGetUseCase connectAuthUserGetUseCase, UserRepository userRepository, UserOnboardUseCase onBoardUseCase) {
        this.connectAuthUserGetUseCase = connectAuthUserGetUseCase;
        this.userRepository = userRepository;
        this.onBoardUseCase = onBoardUseCase;
    }


    @GetMapping({"", "/"})
    public SimpleResponseDTO find(@RequestHeader("Authorization") String rawConnectToken) {
        try {
            final ConnectAuthUser connectAuthUser = connectAuthUserGetUseCase.findByConnectToken(new ConnectToken(rawConnectToken));
            final User user = userRepository.findById(connectAuthUser.userId().value())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                    .toModel();
            return SimpleResponseDTO.fromModel(user);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    @PostMapping({"/onboard", "/onboard/"})
    public SimpleResponseDTO onboard(@RequestHeader("Authorization") String rawConnectToken,
                                     @RequestBody OnBoardRequestDTO requestDTO) {
        try {
            final ConnectAuthUser connectAuthUser = connectAuthUserGetUseCase.findByConnectToken(new ConnectToken(rawConnectToken));
            final UserOnboardUseCase.Request request = new UserOnboardUseCase
                    .Request(Language.valueOf(requestDTO.language()), requestDTO.nickName(), requestDTO.email());
            User user = onBoardUseCase.apply(connectAuthUser.userId(), request)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            return SimpleResponseDTO.fromModel(user);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record OnBoardRequestDTO(String language, String nickName, String email) {

    }

    public record SimpleResponseDTO(String id, String type, String language, String nickname, String email) {
        public static SimpleResponseDTO fromModel(User user) {
            return new SimpleResponseDTO(user.id().value(), user.type().name(), user.language().name(),
                    user.optNickName().orElse(null), user.optEmail().orElse(null));
        }
    }

}

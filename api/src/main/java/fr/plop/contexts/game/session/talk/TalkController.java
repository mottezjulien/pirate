package fr.plop.contexts.game.session.talk;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.domain.ConnectUseCase;
import fr.plop.contexts.connect.domain.ConnectUser;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.persistence.TalkConfigRepository;
import fr.plop.contexts.game.session.core.domain.model.GameContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.persistence.GameSessionRepository;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.contexts.game.session.situation.domain.port.GameSessionSituationGetPort;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sessions/{sessionId}/talks")
public class TalkController {

    private final ConnectUseCase connectUseCase;
    private final GameSessionRepository gameSessionRepository;
    private final TalkConfigRepository talkConfigRepository;
    private final GameEventBroadCast broadCast;
    private final GameSessionSituationGetPort situationGetPort;

    public TalkController(ConnectUseCase connectUseCase, GameSessionRepository gameSessionRepository, TalkConfigRepository talkConfigRepository, GameEventBroadCast broadCast, GameSessionSituationGetPort situationGetPort) {
        this.connectUseCase = connectUseCase;
        this.gameSessionRepository = gameSessionRepository;
        this.talkConfigRepository = talkConfigRepository;
        this.broadCast = broadCast;
        this.situationGetPort = situationGetPort;
    }

    @GetMapping({"/{talkId}", "/{talkId}/"})
    public ResponseDTO getOne(@RequestHeader("Authorization") String rawToken,
                              @RequestHeader("Language") String languageStr,
                              @PathVariable("sessionId") String sessionIdStr,
                              @PathVariable("talkId") String talkIdStr) {
        Language language = Language.valueOf(languageStr.toUpperCase());
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        TalkItem.Id talkId = new TalkItem.Id(talkIdStr);

        try {
            final ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            final GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found"));

            final TalkConfig.Id talkConfigId = new TalkConfig.Id(gameSessionRepository.talkId(sessionId.value())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found")));

            final TalkConfig talkConfig = talkConfigRepository.fullById(talkConfigId.value())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"))
                    .toModel();
            TalkItem item = talkConfig.byId(talkId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));

            GameSessionSituation situation = situationGetPort.get(sessionId, player);

            item = item.select(situation);

            return ResponseDTO.fromModel(item, language);
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }


    @PostMapping({"/{talkId}/options/{optionId}", "/{talkId}/options/{optionId}/"})
    public ResponseDTO selectOption(@RequestHeader("Authorization") String rawToken,
                                    @RequestHeader("Language") String languageStr,
                             @PathVariable("sessionId") String sessionIdStr,
                             @PathVariable("talkId") String talkIdStr,
                             @PathVariable("optionId") String optionIdStr) {
        GameSession.Id sessionId = new GameSession.Id(sessionIdStr);
        TalkItem.Id taklId = new TalkItem.Id(talkIdStr);
        TalkItem.Options.Option.Id optionId = new TalkItem.Options.Option.Id(optionIdStr);

        try {
            ConnectUser user = connectUseCase.findUserIdBySessionIdAndRawToken(sessionId, new ConnectToken(rawToken));
            GamePlayer player = user.player().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No player found"));//TODO

            TalkConfig.Id talkConfigId = new TalkConfig.Id(gameSessionRepository.talkId(sessionId.value())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found")));

            TalkConfig talkConfig = talkConfigRepository.fullById(talkConfigId.value())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"))
                    .toModel();

            TalkItem item = talkConfig.byId(taklId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));

            GameContext context = new GameContext(sessionId, player.id());

            if(item instanceof TalkItem.Options multipleOptions) {
                Optional<TalkItem.Options.Option> optOption = multipleOptions.option(optionId);
                if(optOption.isPresent()) {
                    broadCast.fire(new GameEvent.Talk(item.id(), Optional.of(optionId)), context);
                    TalkItem.Options.Option option = optOption.get();
                    if(option.hasNext()) {
                        return getOne(rawToken, languageStr, sessionIdStr, option.nextId().value());
                    }
                    throw new ResponseStatusException(HttpStatus.NO_CONTENT);
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found");

        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }


    public record ResponseDTO(String id, String value, Character character, Result result) {

        public record Character(String name, ImageResponseDTO image) { }

        public record Result(String type, List<Option> options, String nextId) {
            public record Option(String id, String value) {
                public static Result.Option fromModel(TalkItem.Options.Option model, Language language) {
                    return new Result.Option(model.id().value(), model.value().value(language));
                }

            }
        }
        public static ResponseDTO fromModel(TalkItem item, Language language) {
            ImageResponseDTO image = new ImageResponseDTO(item.characterReference().image().type().name(), item.characterReference().image().value(), null);
            ResponseDTO.Character character = new Character(item.character().name(), image);
            List<Result.Option> options = List.of();
            String nextId = null;
            String resultType = switch (item) {
                case TalkItem.Options multipleOptions -> {
                    options = multipleOptions.options()
                            .map(option -> Result.Option.fromModel(option, language))
                            .toList();
                    yield "MULTIPLE";
                }
                case TalkItem.Continue _continue -> {
                    nextId = _continue.nextId().value();
                    yield "CONTINUE";
                }
                case TalkItem.Simple ignored -> "SIMPLE";
            };
            Result result = new Result(resultType, options, nextId);
            return new ResponseDTO(item.id().value(), item.value(language), character, result);
        }
    }




}

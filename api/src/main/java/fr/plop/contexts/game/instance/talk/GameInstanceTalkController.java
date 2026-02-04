package fr.plop.contexts.game.instance.talk;


import fr.plop.contexts.connect.domain.ConnectException;
import fr.plop.contexts.connect.domain.ConnectToken;
import fr.plop.contexts.connect.usecase.ConnectAuthGameInstanceUseCase;
import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.event.domain.GameEvent;
import fr.plop.contexts.game.instance.event.domain.GameEventOrchestrator;
import fr.plop.contexts.game.instance.situation.domain.GameInstanceSituation;
import fr.plop.contexts.game.instance.situation.domain.port.GameInstanceSituationGetPort;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import fr.plop.subs.image.ImageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/instances/{instanceId}/talks")
public class GameInstanceTalkController {

    private final ConnectAuthGameInstanceUseCase authGameInstanceUseCase;
    private final GameConfigCache cache;
    private final GameInstanceSituationGetPort situationGetPort;
    private final GameEventOrchestrator eventOrchestrator;
    private final GameInstanceTalkUseCase useCase;

    public GameInstanceTalkController(ConnectAuthGameInstanceUseCase authGameInstanceUseCase, GameConfigCache cache, GameInstanceSituationGetPort situationGetPort, GameEventOrchestrator eventOrchestrator, GameInstanceTalkUseCase useCase) {
        this.authGameInstanceUseCase = authGameInstanceUseCase;
        this.cache = cache;
        this.situationGetPort = situationGetPort;
        this.eventOrchestrator = eventOrchestrator;
        this.useCase = useCase;
    }


    @GetMapping({"/{talkId}", "/{talkId}/"})
    public ResponseDTO getOne(@RequestHeader("Authorization") String rawSessionToken,
                              @RequestHeader("Language") String languageStr,
                              @PathVariable("instanceId") String sessionIdStr,
                              @PathVariable("talkId") String talkIdStr) {
        final Language language = Language.valueOf(languageStr.toUpperCase());
        final GameInstance.Id sessionId = new GameInstance.Id(sessionIdStr);
        final TalkItem.Id talkId = new TalkItem.Id(talkIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));
            final TalkConfig talkConfig = cache.talk(sessionId);
            final TalkItem item = talkConfig.byId(talkId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));

            final GameInstanceSituation situation = situationGetPort.get(context);
            ResponseDTO response = ResponseDTO.fromModel(item, situation, language);
            useCase.read(context, item.characterId(), talkId);
            return response;
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }




    @PostMapping({"/{talkId}/options/{optionId}", "/{talkId}/options/{optionId}/"})
    public ResponseDTO selectOption(@RequestHeader("Authorization") String rawSessionToken,
                                    @RequestHeader("Language") String languageStr,
                                    @PathVariable("instanceId") String sessionIdStr,
                                    @PathVariable("talkId") String talkIdStr,
                                    @PathVariable("optionId") String optionIdStr) {
        final GameInstance.Id sessionId = new GameInstance.Id(sessionIdStr);
        final TalkItem.Id talkId = new TalkItem.Id(talkIdStr);
        final TalkItemNext.Options.Option.Id optionId = new TalkItemNext.Options.Option.Id(optionIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase
                    .findContext(sessionId, new ConnectToken(rawSessionToken));
            final TalkConfig talkConfig = cache.talk(sessionId);
            final TalkItem item = talkConfig.byId(talkId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));

            if (item.next() instanceof TalkItemNext.Options multipleOptions) {
                Optional<TalkItemNext.Options.Option> optOption = multipleOptions.option(optionId);
                if (optOption.isPresent()) {
                    eventOrchestrator.fire(context, new GameEvent.Talk(item.id(), Optional.of(new TalkItemNext.Options.Option.Id(optionIdStr))));
                    TalkItemNext.Options.Option option = optOption.get();
                    if (option.hasNext()) {
                        return getOne(rawSessionToken, languageStr, sessionIdStr, option.nextId().value());
                    }
                    throw new ResponseStatusException(HttpStatus.NO_CONTENT);
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found");

        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }




    @PostMapping({"/{talkId}/inputtext", "/{talkId}/inputtext/"})
    public void submitInputText(@RequestHeader("Authorization") String rawSessionToken,
                                @PathVariable("instanceId") String sessionIdStr,
                                @PathVariable("talkId") String talkIdStr,
                                @RequestBody InputTextRequestDTO request) {
        final GameInstance.Id sessionId = new GameInstance.Id(sessionIdStr);
        final TalkItem.Id talkId = new TalkItem.Id(talkIdStr);
        try {
            final GameInstanceContext context = authGameInstanceUseCase.findContext(sessionId, new ConnectToken(rawSessionToken));
            final TalkConfig talkConfig = cache.talk(sessionId);
            final TalkItem item = talkConfig.byId(talkId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No talk found"));

            if (item.next() instanceof TalkItemNext.InputText) {
                eventOrchestrator.fire(context, new GameEvent.TalkInputText(item.id(), request.value()));
                throw new ResponseStatusException(HttpStatus.NO_CONTENT);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Talk item is not an InputText status");

        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.type().name(), e);
        }
    }

    public record InputTextRequestDTO(String value) {
    }


    public record ResponseDTO(String id, String text, Character character, Next next, Map<String, String> parameters) {

        public record Character(String name, ImageResponseDTO image) {
        }

        public record Next(String type, List<Option> options, String nextId) {
            public record Option(String id, String value) {
                public static Option fromModel(TalkItemNext.Options.Option model, Language language) {
                    return new Option(model.id().value(), model.value().value(language));
                }

            }
        }

        public static ResponseDTO fromModel(TalkItem item, GameInstanceSituation situation, Language language) {
            final I18n resolvedI18n = item.resolve(situation);
            String text = resolvedI18n.value(language);

            final Image imageCharacter = item.characterReference().image();
            final ImageResponseDTO image = ImageResponseDTO.fromModel(imageCharacter);
            final Character character = new Character(item.character().name(), image);
            List<Next.Option> options = List.of();
            java.util.Map<String, String> parameters = new HashMap<>();
            String nextId = null;
            final String resultType = switch (item.next()) {
                case TalkItemNext.Empty ignored -> "EMPTY";
                case TalkItemNext.Continue _continue -> {
                    nextId = _continue.nextId().value();
                    yield "CONTINUE";
                }
                case TalkItemNext.Options multipleOptions -> {
                    options = multipleOptions.options()
                            .filter(option -> option.accept(situation))
                            .map(option -> Next.Option.fromModel(option, language))
                            .toList();
                    yield "MULTIPLE";
                }
                case TalkItemNext.InputText inputText -> {
                    parameters.put("type", inputText.type().name());
                    inputText.optSize().ifPresent(size -> parameters.put("size", size.toString()));
                    yield "INPUTTEXT";
                }
            };
            Next result = new Next(resultType, options, nextId);
            return new ResponseDTO(item.id().value(), text, character, result, parameters);
        }
    }


}
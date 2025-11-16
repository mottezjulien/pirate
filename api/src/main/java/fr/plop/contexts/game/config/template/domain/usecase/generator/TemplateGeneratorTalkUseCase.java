package fr.plop.contexts.game.config.template.domain.usecase.generator;

import fr.plop.contexts.game.config.talk.domain.TalkCharacter;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Tree;
import fr.plop.contexts.game.config.template.domain.usecase.ParsingContext;
import fr.plop.subs.i18n.domain.I18n;
import fr.plop.subs.image.Image;

import java.util.*;
import java.util.stream.Stream;

public class TemplateGeneratorTalkUseCase {

    private static final String SEPARATOR = ":";

    private static final String PARAM_KEY_TALK_OPTION = "OPTION";
    public static final String POUET_CHARACTER = "CHARACTER";

    private final ParsingContext context;

    private final Map<String, TalkItem.Id> localReferences = new HashMap<>();
    private final Map<String, String> nextLocalReferences = new HashMap<>();

    private final TemplateGeneratorI18nUseCase i18nGenerator = new TemplateGeneratorI18nUseCase();

    public TemplateGeneratorTalkUseCase(ParsingContext context) {
        this.context = context;
    }

    public List<TalkItem> apply(Tree tree) {
        List<TalkCharacter> characters = characters(tree);
        List<TalkItem> items = items(tree, characters);
        return items.stream().map(item -> {
            if (item instanceof TalkItem.Continue _continue) {
                String referenceNextId = nextLocalReferences.get(item.id().value());
                return _continue.withNextId(localReferences.get(referenceNextId));
            }
            if (item instanceof TalkItem.Options itamsOptions) {
                List<TalkItem.Options.Option> options = itamsOptions.options().map(opt -> {
                    if (nextLocalReferences.containsKey(opt.id().value())) {
                        return opt.withNextId(localReferences.get(nextLocalReferences.get(opt.id().value())));
                    }
                    return opt;
                }).toList();
                return itamsOptions.withOptions(options);
            }
            return item;
        }).toList();
    }


    private List<TalkCharacter> characters(Tree tree) {
        return tree.children().stream().flatMap(child -> {
            if (child.header().contains(POUET_CHARACTER)) {
                return parseCharactersFromTree(child).stream();
            }
            return Stream.empty();
        }).toList();
    }

    private List<TalkItem> items(Tree tree, List<TalkCharacter> characters) {
        return tree.children().stream()
            .flatMap(child -> {
                Optional<TalkItem> optItem = parseTalkItemFromTree(child, characters);
                if (child.reference() != null) {
                    optItem.ifPresent(talkItem -> {
                        context.registerReference(child.reference(), talkItem);
                        localReferences.put(child.reference(), talkItem.id());
                    });
                }
                return optItem.stream();
            }).toList();
    }

    private Optional<TalkItem> parseTalkItemFromTree(Tree child, List<TalkCharacter> characters) {
        return switch (child.header()) {
            case "SIMPLE" -> Optional.of(parseSimpleFromTree(child, characters));
            case "CONTINUE" -> Optional.of(parseContinueFromTree(child, characters));
            case "OPTIONS" -> Optional.of(parseMultipleOptionsFromTree(child, characters));
            default -> Optional.empty();
        };
    }

    private List<TalkCharacter> parseCharactersFromTree(Tree characterTree) {
        List<TalkCharacter> result = new ArrayList<>();
        for (Tree characterChild : characterTree.children()) {
            for (Tree avatarTree : characterChild.children()) {
                String reference = avatarTree.header();
                List<String> params = avatarTree.params();
                if (params.size() >= 2) {
                    // Format 1: header + params
                    result.add(new TalkCharacter(characterChild.headerKeepCase(), reference, buildImage(params.getFirst(), params.get(1))));
                } else if (params.size() == 1) {
                    // Format hybride: header contient avatarName, params[0] = imagePath
                    result.add(new TalkCharacter(characterChild.headerKeepCase(), reference, buildImage("ASSET", params.getFirst())));
                } else if (reference.contains(SEPARATOR)) {
                    // Format 2: tout dans le header "AvatarName:Type:image_path.jpg"
                    String[] parts = reference.split(SEPARATOR, 3);
                    if (parts.length >= 3) {
                        result.add(new TalkCharacter(characterChild.headerKeepCase(), parts[0], buildImage(parts[1], parts[2])));
                    }
                }
            }
        }
        return result;
    }


    private TalkItem.Simple parseSimpleFromTree(Tree simpleTree, List<TalkCharacter> characters) {
        String characterName = "";
        String avatarName = "";
        List<String> params = simpleTree.params();
        if (!params.isEmpty()) {
            characterName = params.getFirst();
            if (params.size() >= 2) {
                avatarName = params.get(1);
            }
        }

        Optional<I18n> messageOpt = i18nGenerator.apply(simpleTree.children().stream()
                .filter(child -> !child.header().equals(POUET_CHARACTER)).toList());
        I18n message = messageOpt.orElse(new I18n(Map.of()));

        TalkCharacter talkCharacter = parseCharacterFromTreeChildren(simpleTree.children(), characters);

        if (talkCharacter == null) {
            talkCharacter = parseCharacterFromParams(characterName, avatarName, characters);
        }

        return new TalkItem.Simple(new TalkItem.Id(), message, talkCharacter);
    }

    private TalkItem.Continue parseContinueFromTree(Tree continueTree, List<TalkCharacter> characters) {
        Optional<I18n> messageOpt = i18nGenerator.apply(continueTree.children().stream()
                .filter(child -> !child.header().equals(POUET_CHARACTER)).toList());
        I18n message = messageOpt.orElse(new I18n(Map.of()));

        // Parse character from children (new format: Character:Alice:alice-sad)
        TalkCharacter talkCharacter = parseCharacterFromTreeChildren(continueTree.children(), characters);
        if (talkCharacter == null) {
            talkCharacter = TalkCharacter.nobody();
        }

        TalkItem.Id id = new TalkItem.Id();
        nextLocalReferences.put(id.value(), continueTree.uniqueParam());
        return new TalkItem.Continue(id, message, talkCharacter, null);
    }


    private TalkCharacter parseCharacterFromTreeChildren(List<Tree> children, List<TalkCharacter> characters) {
        for (Tree child : children) {
            String header = child.header();
            if (header.startsWith(POUET_CHARACTER)) {
                List<String> parts = child.params();
                if (parts.size() >= 2) {
                    String characterName = parts.get(0);
                    String avatarName = parts.get(1);
                    return parseCharacterFromParams(characterName, avatarName, characters);
                }
            }
        }
        return null;
    }

    private TalkCharacter parseCharacterFromParams(String characterName, String reference, List<TalkCharacter> characters) {
        if (!characterName.isEmpty()) {
            return characters.stream()
                    .filter(c -> c.name().equalsIgnoreCase(characterName))
                    .filter(c -> c.reference().equalsIgnoreCase(reference))
                    .findFirst()
                    .orElse(TalkCharacter.nobody());
        }
        return TalkCharacter.nobody();
    }







    private static Image buildImage(String typeStr, String imagePath) {
        Image.Type imageType = "WEB".equalsIgnoreCase(typeStr) ? Image.Type.WEB : Image.Type.ASSET;
        return new Image(imageType, imagePath);
    }



    private TalkItem.Options parseMultipleOptionsFromTree(Tree optionsTree, List<TalkCharacter> characters) {
        // Extraire la référence du header si elle existe : "Options(ref OPTIONS_ABCD)"
        //String referenceName = optionsTree.reference();

        TalkItem.Id id = new TalkItem.Id();

        I18n label = new I18n(Map.of()); // label par défaut
        List<TalkItem.Options.Option> options = new ArrayList<>();

        for (Tree child : optionsTree.children()) {
            String header = child.header();
            if (header.equals("LABEL")) {
                // Parse l'I18n pour le label
                Optional<I18n> labelOpt = i18nGenerator.apply(child.children());
                label = labelOpt.orElse(new I18n(Map.of()));
            } else if (header.equals(PARAM_KEY_TALK_OPTION)) {


                // Parse l'I18n pour chaque option
                // Structure:
                // Option (ref WAHUP_YES)
                //   value
                //     FR:Oui
                //     EN:Yes
                //   next:TALK002
                List<Tree> childrenToParseI18n = child.children();
                Optional<Tree> valueTree = child.children().stream()
                        .filter(t -> t.header().equals("VALUE"))
                        .findFirst();

                if (valueTree.isPresent()) {
                    childrenToParseI18n = valueTree.get().children();
                }

                Optional<I18n> optionOpt = i18nGenerator.apply(childrenToParseI18n);
                I18n optionMessage = optionOpt.orElse(new I18n(Map.of()));

                // Create a reference that will be resolved later
                TalkItem.Options.Option option = new TalkItem.Options.Option(new TalkItem.Options.Option.Id(), options.size(), optionMessage,
                        Optional.empty());

                // Chercher la référence nextId via "next:TALK002"
                Optional<String> nextIdRef = child.children().stream()
                        .filter(t -> t.header().equals("NEXT"))
                        .flatMap(t -> t.params().stream())
                        .findFirst();

                nextIdRef.ifPresent(nextId -> nextLocalReferences.put(option.id().value(),  nextId));

                options.add(option);

                // Enregistrer la référence si elle existe
                if (child.reference() != null) {
                    context.registerReference(child.reference(), option);
                }
                context.registerOptionToTalkItemMapping(option.id(), id);
            }
        }

        // Parse character from children (new format: Character:Alice:alice-sad)
        TalkCharacter talkCharacter = parseCharacterFromTreeChildren(optionsTree.children(), characters);
        if (talkCharacter == null) {
            talkCharacter = TalkCharacter.nobody();
        }

        return new TalkItem.Options(id, label, talkCharacter, options);
    }

}

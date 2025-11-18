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
        List<TalkItem> items = items(tree);
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


    private List<TalkCharacter.Reference> characterReferences(Tree tree) {
        return tree.children().stream().flatMap(child -> {
            if (child.header().contains(POUET_CHARACTER)) {
                return parseCharacterReferences(child).stream();
            }
            return Stream.empty();
        }).toList();
    }

    private List<TalkItem> items(Tree tree) {
        List<TalkCharacter.Reference> characterReferences = characterReferences(tree);
        return tree.children().stream()
            .flatMap(child -> {
                Optional<TalkItem> optItem = parseItem(child, characterReferences);
                if (child.reference() != null) {
                    optItem.ifPresent(talkItem -> {
                        context.registerReference(child.reference(), talkItem);
                        localReferences.put(child.reference(), talkItem.id());
                    });
                }
                return optItem.stream();
            }).toList();
    }

    private Optional<TalkItem> parseItem(Tree child, List<TalkCharacter.Reference> characterReferences) {

        /*if (talkCharacterReference == null) {
            List<String> params = child.params();
            if (params.size() >= 2) {
                talkCharacterReference = parseCharacterReferenceFromParams(params.getFirst(), params.get(1), characterReferences);
            }
        }*/
        return switch (child.header()) {
            case "SIMPLE" -> Optional.of(parseSimpleFromTree(child, parseCharacterReference(child, characterReferences)));
            case "CONTINUE" -> Optional.of(parseContinueFromTree(child, parseCharacterReference(child, characterReferences)));
            case "OPTIONS" -> Optional.of(parseMultipleOptionsFromTree(child, parseCharacterReference(child, characterReferences)));
            default -> Optional.empty();
        };
    }

    private List<TalkCharacter.Reference> parseCharacterReferences(Tree characterTree) {
        List<TalkCharacter.Reference> result = new ArrayList<>();
        for (Tree characterChild : characterTree.children()) {
            TalkCharacter character = new TalkCharacter(characterChild.headerKeepCase());

            for (Tree avatarTree : characterChild.children()) {
                List<String> params = avatarTree.params();
                if(avatarTree.hasUniqueParam()) {
                    result.add(new TalkCharacter.Reference(character, avatarTree.header(), buildImage("ASSET", params.getFirst())));
                } else if (params.size() >= 2) {
                    result.add(new TalkCharacter.Reference(character, avatarTree.header(), buildImage(params.getFirst(), params.get(1))));
                } /*else if (reference.contains(SEPARATOR)) {
                    // Format 2: tout dans le header "AvatarName:Type:image_path.jpg"
                    String[] parts = reference.split(SEPARATOR, 3);
                    if (parts.length >= 3) {
                        result.add(new TalkCharacter(characterChild.headerKeepCase(), parts[0], buildImage(parts[1], parts[2])));
                    }
                }*/
            }
        }
        return result;
    }


    private TalkItem.Simple parseSimpleFromTree(Tree simpleTree, TalkCharacter.Reference talkCharacterReference) {

        Optional<I18n> messageOpt = i18nGenerator.apply(simpleTree.children().stream()
                .filter(child -> !child.header().equals(POUET_CHARACTER)).toList());
        I18n message = messageOpt.orElse(new I18n(Map.of()));

        return new TalkItem.Simple(new TalkItem.Id(), message, talkCharacterReference);
    }

    private TalkItem.Continue parseContinueFromTree(Tree continueTree, TalkCharacter.Reference talkCharacterReference) {
        Optional<I18n> messageOpt = i18nGenerator.apply(continueTree.children().stream()
                .filter(child -> !child.header().equals(POUET_CHARACTER)).toList());
        I18n message = messageOpt.orElse(new I18n(Map.of()));

        TalkItem.Id id = new TalkItem.Id();
        nextLocalReferences.put(id.value(), continueTree.uniqueParam());
        return new TalkItem.Continue(id, message, talkCharacterReference, null);
    }


    private TalkCharacter.Reference parseCharacterReference(Tree tree, List<TalkCharacter.Reference> characterReferences) {

        Optional<TalkCharacter.Reference> characterReference = tree.children().stream().filter(child -> child.header()
                        .equals(POUET_CHARACTER))
                .findFirst()
                .map(child -> {
                    List<String> parts = child.params();
                    if (parts.size() >= 2) {
                        return parseCharacterReferenceFromParams(parts.get(0), parts.get(1), characterReferences);
                    }
                    if(!child.children().isEmpty() && !child.children().getFirst().children().isEmpty()) {
                        String characterName = child.children().getFirst().headerKeepCase();
                        String reference = child.children().getFirst().children().getFirst().headerKeepCase();
                        return parseCharacterReferenceFromParams(characterName, reference, characterReferences);
                    }
                    throw new RuntimeException("Character not found");
        });
        return characterReference.orElseGet(() -> {
            if(tree.params().size() == 2) {
                return parseCharacterReferenceFromParams(tree.params().get(0), tree.params().get(1), characterReferences);
            }
            throw new RuntimeException("Character not found");
        });
    }

    private TalkCharacter.Reference parseCharacterReferenceFromParams(String characterName, String reference, List<TalkCharacter.Reference> characterReferences) {
        if (!characterName.isEmpty()) {
            return characterReferences.stream()
                    .filter(r -> r.character().name().equalsIgnoreCase(characterName))
                    .filter(r -> r.value().equalsIgnoreCase(reference))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Character not found: " + characterName + ", " + reference));
        }
        throw new RuntimeException("Character not found: " + characterName + ", " + reference);
    }

    private static Image buildImage(String typeStr, String imagePath) {
        Image.Type imageType = "WEB".equalsIgnoreCase(typeStr) ? Image.Type.WEB : Image.Type.ASSET;
        return new Image(imageType, imagePath);
    }



    private TalkItem.Options parseMultipleOptionsFromTree(Tree optionsTree, TalkCharacter.Reference talkCharacterReference) {
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

        return new TalkItem.Options(id, label, talkCharacterReference, options);
    }

}

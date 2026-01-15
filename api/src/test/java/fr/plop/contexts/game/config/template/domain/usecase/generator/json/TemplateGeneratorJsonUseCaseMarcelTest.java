package fr.plop.contexts.game.config.template.domain.usecase.generator.json;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.session.situation.domain.GameSessionSituation;
import fr.plop.subs.i18n.domain.Language;
import fr.plop.subs.image.Image;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de la génération JSON pour Marcel avec les humeurs
 */
public class TemplateGeneratorJsonUseCaseMarcelTest {

    private final TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

    @Test
    public void marcel_with_moods() throws Exception {
        // Lecture du fichier JSON de test
        String json = Files.readString(Path.of("src/test/resources/template/test_marcel_talk.json"));

        Template template = generator.apply(json);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TEST_MARCEL");

        // Vérification des items de discussion
        assertThat(template.talk().items()).hasSize(11);

        // Premier message : Marcel content
        TalkItem firstMessage = template.talk().items().getFirst();
        assertThat(firstMessage.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Bonjour l'ami. Alors, c'est aujourd'hui le grand jour, cette fois, tu trouveras le trésor, tu réussiras là où j'ai échoué.");
        assertThat(firstMessage.character().name()).isEqualTo("MARCEL");
        assertThat(firstMessage.characterReference().value()).isEqualTo("happy");
        assertThat(firstMessage.characterReference().image().value()).isEqualTo("assets/game/carte_tresor/marcel_happy.png");
        assertThat(firstMessage.characterReference().image().type()).isEqualTo(Image.Type.ASSET);
        assertThat(firstMessage.isContinue()).isTrue();
        assertThat(firstMessage.nextId()).isEqualTo(template.talk().items().get(1).id());

        // Deuxième message : Marcel content
        TalkItem secondMessage = template.talk().items().get(1);
        assertThat(secondMessage.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Comme prévu, voici ma partie de la carte du trésor.");
        assertThat(secondMessage.character().name()).isEqualTo("MARCEL");
        assertThat(secondMessage.characterReference().value()).isEqualTo("happy");
        assertThat(secondMessage.characterReference().image().value()).isEqualTo("assets/game/carte_tresor/marcel_happy.png");
        assertThat(secondMessage.isContinue()).isTrue();

        // Troisième message : Marcel default
        TalkItem thirdMessage = template.talk().items().get(2);
        assertThat(thirdMessage.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Sophie et Émilie possèdent les autres parties de la carte. Tu devrais leur parler afin de compléter la carte.");
        assertThat(thirdMessage.character().name()).isEqualTo("MARCEL");
        assertThat(thirdMessage.characterReference().value()).isEqualTo("default");
        assertThat(thirdMessage.characterReference().image().value()).isEqualTo("assets/game/carte_tresor/marcel_default.png");
        assertThat(thirdMessage.isContinue()).isTrue();

        // Cinquième message : avec options
        TalkItem fifthMessage = template.talk().items().get(4);
        assertThat(fifthMessage.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Émilie se trouve dans sa librairie.");
        assertThat(fifthMessage.character().name()).isEqualTo("MARCEL");
        assertThat(fifthMessage.characterReference().value()).isEqualTo("default");
        assertThat(fifthMessage.isOptions()).isTrue();
        assertThat(fifthMessage.options().options()).hasSize(2)
                .anySatisfy(option -> {
                    assertThat(option.value().value(Language.FR)).isEqualTo("Merci Marcel. J'aurai des questions à te poser.");
                    assertThat(option.optNextId()).hasValue(template.talk().items().get(5).id());
                })
                .anySatisfy(option -> {
                    assertThat(option.value().value(Language.FR)).isEqualTo("Merci Marcel. C'est parti pour l'aventure.");
                    assertThat(option.optNextId()).isEmpty(); // Fin de discussion
                });

        // Message racine avec carte : Marcel content
        TalkItem homeWithCard = template.talk().items().get(5);
        assertThat(homeWithCard.out().resolve(new GameSessionSituation()).value(Language.FR)).isEqualTo("Oui l'ami, que puis-je faire pour toi ?");
        assertThat(homeWithCard.character().name()).isEqualTo("MARCEL");
        assertThat(homeWithCard.characterReference().value()).isEqualTo("happy");
        assertThat(homeWithCard.isOptions()).isTrue();
        assertThat(homeWithCard.options().options()).hasSize(6);

        // TALK_MARCEL_1 : retour au message racine
        TalkItem talkMarcel1 = template.talk().items().get(6);
        assertThat(talkMarcel1.out().resolve(new GameSessionSituation()).value(Language.FR)).contains("Tu dois parler avec Sophie et Émilie");
        assertThat(talkMarcel1.character().name()).isEqualTo("MARCEL");
        assertThat(talkMarcel1.characterReference().value()).isEqualTo("default");
        assertThat(talkMarcel1.isContinue()).isTrue();
        assertThat(talkMarcel1.nextId()).isEqualTo(homeWithCard.id()); // Redirection vers message racine
    }
}
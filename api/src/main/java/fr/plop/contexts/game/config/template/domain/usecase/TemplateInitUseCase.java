package fr.plop.contexts.game.config.template.domain.usecase;

import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.tree.TemplateGeneratorTreeUseCase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TemplateInitUseCase {

    public interface OutPort {

        boolean isEmpty();

        void create(Template template);

        void deleteAll();
    }

    private final OutPort outPort;

    public TemplateInitUseCase(OutPort outPort) {
        this.outPort = outPort;
    }

    public void apply() {

        //TODO: On supprime tout comme des gros bourrins ???
        outPort.deleteAll();

        Template chezWam = chezWamTemplate();
        if (!chezWam.isValid()) {
            throw new IllegalStateException("Template chezWam invalide");
        }
        outPort.create(chezWam);

        Template lyon9 = lyon9Template();
        if (!lyon9.isValid()) {
            throw new IllegalStateException("Template lyon9 invalide");
        }
        outPort.create(lyon9);

        Template testDiscussionTemplate = testDiscussionTemplate();
        if (!testDiscussionTemplate.isValid()) {
            throw new IllegalStateException("Template test_discution invalide");
        }
        outPort.create(testDiscussionTemplate);
    }

    private Template chezWamTemplate() {
        try {
            //String scriptContent = loadScriptFromResources("template/chez_wam.txt");
            String scriptContent = loadScriptFromResources("template/archives/chez_wam_easy.txt");
            TemplateGeneratorTreeUseCase generator = new TemplateGeneratorTreeUseCase();
            TemplateGeneratorTreeUseCase.Script script = new TemplateGeneratorTreeUseCase.Script(scriptContent);
            return generator.apply(script);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du script chez_wam.txt", e);
        }
    }

    private Template lyon9Template() {
        try {
            String scriptContent = loadScriptFromResources("template/lyon9.txt");
            TemplateGeneratorTreeUseCase generator = new TemplateGeneratorTreeUseCase();
            TemplateGeneratorTreeUseCase.Script script = new TemplateGeneratorTreeUseCase.Script(scriptContent);
            return generator.apply(script);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du script chez_wam.txt", e);
        }
    }

    private Template testDiscussionTemplate() {
        try {
            String scriptContent = loadScriptFromResources("template/test_discution.txt");
            TemplateGeneratorTreeUseCase generator = new TemplateGeneratorTreeUseCase();
            TemplateGeneratorTreeUseCase.Script script = new TemplateGeneratorTreeUseCase.Script(scriptContent);
            return generator.apply(script);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du script test_discution.txt", e);
        }
    }

    private String loadScriptFromResources(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Fichier non trouvé: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}

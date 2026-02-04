package fr.plop.contexts.game.config.template.domain.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.plop.contexts.game.commun.domain.Game;
import fr.plop.contexts.game.commun.domain.GameProject;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.json.TemplateGeneratorJsonUseCase;
import fr.plop.contexts.game.config.template.domain.usecase.generator.json.TemplateGeneratorRoot;
import fr.plop.contexts.game.presentation.domain.Presentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TemplateInitUseCase {

    public interface OutPort {

        boolean isEmpty();

        Game.Id findOrCreateGame(GameProject.Code code, Game.Version version);

        void createOrUpdate(Game.Id gameId, Template template);

        void createOrUpdate(Game.Id gameId, Presentation presentation);

        void deleteAll();
    }

    private final OutPort outPort;

    public TemplateInitUseCase(OutPort outPort) {
        this.outPort = outPort;
    }

    public void apply() {

        //TODO: On supprime tout comme des gros bourrins ???
        outPort.deleteAll();

        // generate("template/archives/chez_wam_easy.txt");
        generate("template/lyon9.json");
        generate("template/test_discution.json");
        generate("template/lyon_bellecour.json");

    }

    private void generate(String path) {
        try {

            String scriptContent = loadScriptFromResources(path);
            TemplateGeneratorJsonUseCase generator = new TemplateGeneratorJsonUseCase();

            ObjectMapper mapper = new ObjectMapper();
            TemplateGeneratorRoot root = mapper.readValue(scriptContent, TemplateGeneratorRoot.class);

            GameProject.Code code = generator.code(root);
            Game.Version version = generator.version(root);
            Template template = generator.template(root);
            Presentation presentation = generator.presentation(root);

            if (!template.isValid()) {
                throw new IllegalStateException("Template " + path + " invalide");
            }

            Game.Id gameId = outPort.findOrCreateGame(code, version);
            outPort.createOrUpdate(gameId, template);
            outPort.createOrUpdate(gameId, presentation);



        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du script " + path, e);
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

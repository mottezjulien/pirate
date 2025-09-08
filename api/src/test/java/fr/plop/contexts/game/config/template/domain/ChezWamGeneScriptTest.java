package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.template.domain.model.Template;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class ChezWamGeneScriptTest {


    check



    private final TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();


    
    @Test
    public void testInlineChezWamGeneScript() {
        // Version simplifiée pour tester sans dépendance au fichier
        String scriptContent = """
            ChezWamGene:1.0::15
            
            --- Board
            ------ Space:Bureau:HIGH
            --------- bottomLeft:45.77806:4.80351:topRight:45.77820:4.80367
            
            --- Step (ref CHAPITRE_BUREAU):FR:Le bureau (tutorial):EN:The office (tutorial)
            ------ Target (ref ALLER_BUREAU):FR:Aller dans le bureau:EN:Go to the office
            
            ------ Possibility
            --------- Trigger:ABSOLUTETIME:1
            --------- Consequence:Alert
            ------------ FR:Bienvenue dans le jeu !
            ------------ EN:Welcome to the game!
            """;
        
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);
        
        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("ChezWamGene");
        assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(15);
        
        System.out.println("✅ Test inline réussi !");
    }
}
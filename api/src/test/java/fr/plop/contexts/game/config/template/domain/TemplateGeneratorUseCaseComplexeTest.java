package fr.plop.contexts.game.config.template.domain;

import fr.plop.contexts.game.config.Image.domain.ImageObject;
import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.config.condition.Condition;
import fr.plop.contexts.game.config.scenario.domain.model.PossibilityTrigger;
import fr.plop.contexts.game.config.template.domain.model.Template;
import fr.plop.contexts.game.config.template.domain.usecase.generator.TemplateGeneratorUseCase;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.generic.enumerate.Priority;
import fr.plop.subs.i18n.domain.Language;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateGeneratorUseCaseComplexeTest {

    private final TemplateGeneratorUseCase generator = new TemplateGeneratorUseCase();

    @Test
    public void testInlineChezWamGeneScriptOld() {
        // Version simplifiée pour tester sans dépendance au fichier
        String scriptContent = """
            ChezWamGene:1.0::15
            
            --- Board
            ------ Space:Bureau:HIGH
            --------- bottomLeft:45.77806:4.80351:topRight:45.77820:4.80367
            
            --- Step (ref CHAPITRE_BUREAU):FR:Le bureau (tutorial):EN:The office (tutorial)
            ------ Target (ref ALLER_BUREAU):FR:Aller dans le bureau:EN:Go to the office
            
            ------ Possibility
            --------- Trigger:ABSOLUTETIME:0
            --------- Consequence:Alert
            ------------ FR:Bienvenue dans le jeu !
            ------------ EN:Welcome to the game !
            """;
        
        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);
        
        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("ChezWamGene");
        assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(15);

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
        .anySatisfy(step -> {
           assertThat(step.label().value(Language.FR)).isEqualTo("Le bureau (tutorial)");
           assertThat(step.label().value(Language.EN)).isEqualTo("The office (tutorial)");
           assertThat(step.targets()).hasSize(1)
               .anySatisfy(target -> {
                   assertThat(target.label().value(Language.FR)).isEqualTo("Aller dans le bureau");
                   assertThat(target.label().value(Language.EN)).isEqualTo("Go to the office");
               });
           assertThat(step.possibilities())
               .hasSize(1)
               .anySatisfy(possibility -> {
                   assertThat(possibility.trigger())
                           .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                   PossibilityTrigger.AbsoluteTime absoluteTime = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                   assertThat(absoluteTime.value()).isEqualTo(new GameSessionTimeUnit(0));
                   assertThat(possibility.consequences())
                       .hasSize(1)
                       .anySatisfy(consequence -> {
                           assertThat(consequence).isInstanceOf(Consequence.DisplayMessage.class);
                           Consequence.DisplayMessage displayMessage = (Consequence.DisplayMessage) consequence;
                           assertThat(displayMessage.value().value(Language.FR)).isEqualTo("Bienvenue dans le jeu !");
                           assertThat(displayMessage.value().value(Language.EN)).isEqualTo("Welcome to the game !");
                       });
               });

        }));
    }

    @Test
    public void testInlineChezWamGeneScript() {
        String scriptContent = """
                ChezWamGene:1.0::15
                
                --- Step (ref CHAPITRE_BUREAU):FR:Le bureau (tutorial):EN:The office (tutorial)
                ------ Target (ref ALLER_BUREAU):FR:Aller dans le bureau:EN:Go to the office
               
                ------ Possibility
                --------- Trigger:ABSOLUTETIME:0
                --------- Consequence:Alert
                ------------ FR:Bienvenue dans le jeu !
                ------------ EN:Welcome to the game !
                """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("ChezWamGene");
        assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(15);

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
                .anySatisfy(step -> {
                    assertThat(step.label().value(Language.FR)).isEqualTo("Le bureau (tutorial)");
                    assertThat(step.label().value(Language.EN)).isEqualTo("The office (tutorial)");
                    assertThat(step.targets()).hasSize(1)
                            .anySatisfy(target -> {
                                assertThat(target.label().value(Language.FR)).isEqualTo("Aller dans le bureau");
                                assertThat(target.label().value(Language.EN)).isEqualTo("Go to the office");
                            });
                    assertThat(step.possibilities())
                            .hasSize(1)
                            .anySatisfy(possibility -> {
                                assertThat(possibility.trigger())
                                        .isInstanceOf(PossibilityTrigger.AbsoluteTime.class);
                                PossibilityTrigger.AbsoluteTime absoluteTime = (PossibilityTrigger.AbsoluteTime) possibility.trigger();
                                assertThat(absoluteTime.value()).isEqualTo(new GameSessionTimeUnit(0));
                                assertThat(possibility.consequences())
                                        .hasSize(1)
                                        .anySatisfy(consequence -> {
                                            assertThat(consequence).isInstanceOf(Consequence.DisplayMessage.class);
                                            Consequence.DisplayMessage alert = (Consequence.DisplayMessage) consequence;
                                            assertThat(alert.value().value(Language.FR)).isEqualTo("Bienvenue dans le jeu !");
                                            assertThat(alert.value().value(Language.EN)).isEqualTo("Welcome to the game !");
                                        });
                            });

                }));


    }

    @Test
    public void testStepTargetConditionAndClickMapObject() {
        String scriptContent = """
                TutorialGame:1.0::30
                
                --- Board
                ------ Space (ref Office):Office:HIGH
                --------- bottomLeft:45.77806:4.80351:topRight:45.77820:4.80367
                
                --- Step (ref TUTORIAL_STEP):FR:Étape tutorial:EN:Tutorial step
                ------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office
                ------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk
               
                ------ Possibility
                --------- Trigger:GOINSPACE:Office
                --------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success
                
                ------ Possibility
                --------- Trigger:CLICKMAPOBJECT:DESK_POSITION
                --------- Condition:StepTarget:ENTER_OFFICE
                --------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
                --------- Consequence:Alert
                ------------ FR:Vous avez fouillé le bureau avec succès !
                ------------ EN:You searched the desk successfully!
                
                --- Map:Asset:assets/office.png
                ------ Priority:LOWEST
                ------ object:point:0.2:0.8
                --------- Priority:HIGH
                """;

        TemplateGeneratorUseCase.Script script = new TemplateGeneratorUseCase.Script(scriptContent);
        Template template = generator.apply(script);

        assertThat(template).isNotNull();
        assertThat(template.code().value()).isEqualTo("TutorialGame");
        assertThat(template.version()).isEqualTo("1.0");
        assertThat(template.maxDuration().toMinutes()).isEqualTo(30);

        assertThat(template.scenario()).satisfies(scenario -> assertThat(scenario.steps()).hasSize(1)
                .anySatisfy(step -> {
                    assertThat(step.label().value(Language.FR)).isEqualTo("Étape tutorial");
                    assertThat(step.label().value(Language.EN)).isEqualTo("Tutorial step");

                    assertThat(step.targets()).hasSize(2);

                    assertThat(step.possibilities()).hasSize(2)
                        .anySatisfy(possibility -> {
                            // Test du trigger CLICKMAPOBJECT avec condition StepTarget
                            if (possibility.trigger() instanceof PossibilityTrigger.ClickMapObject clickMapObject) {
                                assertThat(clickMapObject.objectReference()).isEqualTo("DESK_POSITION");

                                assertThat(possibility.optCondition())
                                    .hasValueSatisfying(condition -> {
                                        assertThat(condition).isInstanceOf(Condition.Target.class);
                                        Condition.Target target = (Condition.Target) condition;
                                        assertThat(target.targetId().value()).isEqualTo("ENTER_OFFICE");
                                    });
                            }
                        });
                }));
        
        // Test de la carte
        assertThat(template.map()).satisfies(mapConfig -> {
            // TODO: Compléter les assertions selon la structure réelle de MapConfig
            assertThat(mapConfig).isNotNull();
            assertThat(mapConfig.items())
                    .hasSize(1)
                    .anySatisfy(item -> {
                        assertThat(item.priority()).isEqualTo(Priority.LOWEST);
                        assertThat(item.imageObjects())
                                .hasSize(1)
                                .anySatisfy(position -> {
                                    assertThat(position).isInstanceOf(ImageObject.Point.class);
                                    ImageObject.Point point = (ImageObject.Point) position;
                                    assertThat(point.top()).isCloseTo(0.2, Offset.offset(0.01));
                                    assertThat(point.left()).isCloseTo(0.8, Offset.offset(0.01));
                                });
                    });
        });
    }



}
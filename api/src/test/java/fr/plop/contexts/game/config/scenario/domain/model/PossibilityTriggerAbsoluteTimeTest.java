package fr.plop.contexts.game.config.scenario.domain.model;

import fr.plop.contexts.game.session.time.TimeClick;
import org.junit.jupiter.api.Test;

class PossibilityTriggerAbsoluteTimeTest {


    @Test
    public void test() {
        PossibilityTrigger.AbsoluteTime absoluteTime = new PossibilityTrigger.AbsoluteTime(new PossibilityTrigger.Id(), TimeClick.ofMinutes(10));

    }

}
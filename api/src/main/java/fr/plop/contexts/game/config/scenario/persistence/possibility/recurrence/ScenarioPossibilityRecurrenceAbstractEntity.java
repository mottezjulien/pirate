package fr.plop.contexts.game.config.scenario.persistence.possibility.recurrence;

import fr.plop.contexts.game.config.scenario.domain.model.PossibilityRecurrence;
import jakarta.persistence.*;

@Entity
@Table(name = "LO_SCENARIO_POSSIBILITY_RECURRENCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ScenarioPossibilityRecurrenceAbstractEntity {

    @Id
    protected String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PossibilityRecurrence toModel() {
        return switch (this) {
            case ScenarioPossibilityRecurrenceAlwaysEntity ignored ->
                    new PossibilityRecurrence.Always(new PossibilityRecurrence.Id(id));
            case ScenarioPossibilityRecurrenceTimesEntity times ->
                    new PossibilityRecurrence.Times(new PossibilityRecurrence.Id(id), times.getValue());
            default -> throw new IllegalStateException("Unknown status");
        };
    }

    public static ScenarioPossibilityRecurrenceAbstractEntity fromModel(PossibilityRecurrence model) {
        return switch (model) {
            case PossibilityRecurrence.Always always -> {
                ScenarioPossibilityRecurrenceAlwaysEntity entity = new ScenarioPossibilityRecurrenceAlwaysEntity();
                entity.setId(always.id().value());
                yield entity;
            }
            case PossibilityRecurrence.Times times -> {
                ScenarioPossibilityRecurrenceTimesEntity entity = new ScenarioPossibilityRecurrenceTimesEntity();
                entity.setId(times.id().value());
                entity.setValue(times.value());
                yield entity;
            }
        };
    }
}

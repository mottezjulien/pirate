package fr.plop.contexts.game.config.talk.persistence;


import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import fr.plop.subs.i18n.persistence.I18nEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LO_TALK_ITEM")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("SIMPLE")
public class TalkItemEntity {

    public enum OutputType {
        FIXED, CONDITIONAL
    }

    @Id
    protected String id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    protected TalkConfigEntity config;

    @ManyToOne
    @JoinColumn(name = "value_i18n_id")
    protected I18nEntity value;

    @ManyToOne
    @JoinColumn(name = "character_reference_id")
    protected TalkCharacterReferenceEntity characterReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_type")
    protected OutputType outputType = OutputType.FIXED;

    @OneToMany(mappedBy = "talkItem", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<TalkItemBranchEntity> branches = new ArrayList<>();

    public static TalkItemEntity fromModelId(TalkItem.Id talkId) {
        TalkItemEntity  entity = new TalkItemEntity();
        entity.id = talkId.value();
        return entity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setConfig(TalkConfigEntity config) {
        this.config = config;
    }

    public void setValue(I18nEntity value) {
        this.value = value;
    }


    public void setCharacterReference(TalkCharacterReferenceEntity characterReference) {
        this.characterReference = characterReference;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public List<TalkItemBranchEntity> getBranches() {
        return branches;
    }

    public TalkItem toModel() {
        TalkItemOut out = switch (outputType) {
            case FIXED -> TalkItemOut.fixed(value.toModel());
            case CONDITIONAL -> TalkItemOut.conditional(value.toModel(), branches.stream()
                    .map(branch -> new TalkItemOut.Conditional.Branch(branch.getOrder(), branch.getCondition().toModel(), branch.getValue().toModel()))
                    .toList());
        };
        return new TalkItem(new TalkItem.Id(id), out, characterReference.toModel(), toModelNext());
    }

    protected TalkItemNext toModelNext() {
        return new TalkItemNext.Empty();
    }

}

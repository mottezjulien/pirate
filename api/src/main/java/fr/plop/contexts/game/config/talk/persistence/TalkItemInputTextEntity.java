package fr.plop.contexts.game.config.talk.persistence;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.config.talk.domain.TalkItemNext;
import fr.plop.contexts.game.config.talk.domain.TalkItemOut;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.Optional;

@Entity
@DiscriminatorValue("INPUT_TEXT")
public class TalkItemInputTextEntity extends TalkItemEntity {

    @Column(name = "input_text_type")
    private String inputTextType;

    @Column(name = "input_text_size")
    private Integer inputTextSize;

    public void setInputTextType(String inputTextType) {
        this.inputTextType = inputTextType;
    }

    public void setInputTextSize(Integer inputTextSize) {
        this.inputTextSize = inputTextSize;
    }

    @Override
    public TalkItem toModel() {
        TalkItemNext.InputText.Type type = TalkItemNext.InputText.Type.valueOf(inputTextType);
        Optional<Integer> optSize = Optional.ofNullable(inputTextSize);
        TalkItemNext.InputText inputText = new TalkItemNext.InputText(type, optSize);
        return new TalkItem(new TalkItem.Id(id), TalkItemOut.fixed(value.toModel()), characterReference.toModel(), inputText);
    }

}

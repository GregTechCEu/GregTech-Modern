package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Accessors(chain = true)
public class Dialog<T> extends ModularPanel {

    private final Consumer<T> resultConsumer;
    @Getter
    @Setter
    private boolean draggable = false;
    @Setter
    private boolean disablePanelsBelow = true;
    @Setter
    private boolean closeOnOutOfBoundsClick = false;

    public Dialog(String name) {
        this(name, null);
    }

    public Dialog(String name, Consumer<T> resultConsumer) {
        super(name);
        this.resultConsumer = resultConsumer;
    }

    public void closeWith(T result) {
        if (this.resultConsumer != null) {
            this.resultConsumer.accept(result);
        }
        animateClose();
    }

    @Override
    public boolean disablePanelsBelow() {
        return this.disablePanelsBelow;
    }

    @Override
    public boolean closeOnOutOfBoundsClick() {
        return this.closeOnOutOfBoundsClick;
    }
}

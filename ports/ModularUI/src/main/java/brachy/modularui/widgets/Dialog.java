package brachy.modularui.widgets;

import brachy.modularui.screen.ModularPanel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

/**
 * A {@link ModularPanel} that can close with a result.
 *
 * @param <T> type of the result
 * @param <W> type of this dialog
 */
@Accessors(chain = true, fluent = true)
public class Dialog<T, W extends Dialog<T, W>> extends ModularPanel<W> {

    @Getter
    @Setter
    private Consumer<T> resultConsumer;

    public Dialog(String name) {
        super(name);
    }

    public void closeWith(T result) {
        if (this.resultConsumer != null) {
            this.resultConsumer.accept(result);
        }
        closeIfOpen();
    }
}

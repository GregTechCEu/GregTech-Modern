package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;

import java.util.function.Consumer;

public class SearchComponentWidget<T> extends WidgetGroup {

    private final IWidgetSearch<T> searchHandler;
    private String query = "";

    public SearchComponentWidget(int x, int y, int width, int height, IWidgetSearch<T> searchHandler) {
        super(x, y, width, height);
        this.searchHandler = searchHandler;
        setBackground(ColorPattern.GRAY.rectTexture().setRadius(2));
        addWidget(new TextFieldWidget(2, 2, Math.max(0, width - 4), Math.max(0, height - 4),
                () -> query, this::setQuery).setBordered(false));
    }

    public SearchComponentWidget<T> setQuery(String query) {
        this.query = query == null ? "" : query;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public void search(Consumer<T> consumer) {
        if (searchHandler != null) {
            searchHandler.search(query, consumer);
        }
    }

    public void select(T value) {
        if (searchHandler != null) {
            searchHandler.selectResult(value);
        }
    }

    public interface IWidgetSearch<T> {

        String resultDisplay(T value);

        void selectResult(T value);

        void search(String s, Consumer<T> consumer);
    }
}

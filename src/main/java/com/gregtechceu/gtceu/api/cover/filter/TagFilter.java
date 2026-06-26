package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class TagFilter<T, S extends Filter<T, S>> implements Filter<T, S> {

    @Getter
    protected String filterString = "";

    protected Consumer<S> itemWriter = filter -> {};
    protected Consumer<S> onUpdated = filter -> itemWriter.accept(filter);

    protected @Nullable TagExprFilter.TagExprParser.MatchExpr matchExpr = null;

    protected TagFilter() {}

    @Override
    public boolean isBlank() {
        return filterString.isBlank();
    }

    public void setFilterString(String filterExpr) {
        this.filterString = filterExpr;
        matchExpr = TagExprFilter.parseExpression(filterString);
        // noinspection unchecked
        onUpdated.accept((S) this);
    }

    @Override
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        StringSyncValue filterString = new StringSyncValue(this::getFilterString, this::setFilterString).allowC2S();
        RichTooltip infoTooltip = new RichTooltip().add("cover.tag_filter.info");

        return Flow.row()
                .coverChildren()
                .child(new TextFieldWidget().width(140).value(filterString))
                .child(GTGuiTextures.INFO.asWidget().tooltip(infoTooltip));
    }

    @Override
    public void setOnUpdated(Consumer<S> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagFilter<?, ?> tagFilter = (TagFilter<?, ?>) o;
        return filterString.equals(tagFilter.filterString);
    }

    @Override
    public int hashCode() {
        return filterString.hashCode();
    }
}

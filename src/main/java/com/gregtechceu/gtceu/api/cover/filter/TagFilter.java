package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.nbt.CompoundTag;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;

import java.util.function.Consumer;

public abstract class TagFilter<T, S extends Filter<T, S>> implements Filter<T, S> {

    @Getter
    protected String filterString = "";

    protected Consumer<S> itemWriter = filter -> {};
    protected Consumer<S> onUpdated = filter -> itemWriter.accept(filter);

    protected TagExprFilter.TagExprParser.MatchExpr matchExpr = null;

    protected TagFilter() {}

    @Override
    public boolean isBlank() {
        return filterString.isBlank();
    }

    public CompoundTag saveFilter() {
        if (isBlank()) {
            return null;
        }
        var tag = new CompoundTag();
        tag.putString("oreDict", filterString);
        return tag;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
        matchExpr = TagExprFilter.parseExpression(filterString);
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
}

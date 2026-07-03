package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagFilter<T, S> extends Filter<T> {

    @Getter
    protected String filterString;

    private final Object2BooleanMap<S> matchResultCache = new Object2BooleanOpenHashMap<>();

    protected @Nullable TagExprFilter.TagExprParser.MatchExpr matchExpr;
    private final Function<T, S> tagHolderObject;
    private final Function<T, Stream<TagKey<S>>> tagsSupplier;

    public TagFilter(ItemStack stack, Function<T, S> tagHolderObjectSupplier,
                     Function<T, Stream<TagKey<S>>> tagsSupplier) {
        super(stack);
        this.tagHolderObject = tagHolderObjectSupplier;
        this.tagsSupplier = tagsSupplier;

        var tag = stack.getOrCreateTag();

        // oreDict is backwards compat
        filterString = tag.contains("filter_string") ? tag.getString("filter_string") : tag.getString("oreDict");
        matchExpr = TagExprFilter.parseExpression(filterString);
    }

    public @Nullable CompoundTag writeFilterNBT() {
        if (filterString.isBlank()) {
            return null;
        }
        var tag = new CompoundTag();
        tag.putString("filter_string", filterString);
        return tag;
    }

    public void setFilterString(String filterString) {
        matchResultCache.clear();
        this.filterString = filterString;
        matchExpr = TagExprFilter.parseExpression(filterString);
        updateAndSaveFilter();
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

    private boolean testTagExpr(T t) {
        Set<String> tags = tagsSupplier.apply(t)
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());

        return matchExpr != null && matchExpr.matches(tags);
    }

    @Override
    public boolean test(T t) {
        var tagHolder = tagHolderObject.apply(t);

        if (filterString.isEmpty()) return false;
        if (matchResultCache.containsKey(tagHolder)) return matchResultCache.getOrDefault(tagHolder, false);

        var result = testTagExpr(t);
        matchResultCache.put(tagHolder, result);
        return result;
    }
}

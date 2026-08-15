package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.textfield.TextEditorWidget;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

    public TagFilter(String filterString, Function<T, S> tagHolderObjectSupplier,
                     Function<T, Stream<TagKey<S>>> tagsSupplier) {
        this.filterString = filterString;
        this.tagHolderObject = tagHolderObjectSupplier;
        this.tagsSupplier = tagsSupplier;
    }

    public TagFilter(Function<T, S> tagHolderObjectSupplier,
                     Function<T, Stream<TagKey<S>>> tagsSupplier) {
        this.filterString = "";
        this.tagHolderObject = tagHolderObjectSupplier;
        this.tagsSupplier = tagsSupplier;
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
        RichTooltip infoTooltip = new RichTooltip();
        LangHandler.getMultiLang("cover.tag_filter.info").forEach(infoTooltip::addLine);

        return Flow.row()
                .coverChildren()
                .child(new TextEditorWidget<>().width(140).height(50).padding(4).value(filterString))
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

    public static <T, S> Codec<TagFilter<T, S>> codec(Function<T, S> tagHolderObjectSupplier,
                                                      Function<T, Stream<TagKey<S>>> tagsSupplier) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("filterString").forGetter(v -> v.filterString))
                .apply(instance, s -> new TagFilter<>(s, tagHolderObjectSupplier, tagsSupplier)));
    }
}

package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagType;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.google.common.collect.Table;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
@Accessors(fluent = true)
public class TagPrefixBuilderJS extends BuilderBase<TagPrefix> {

    @Getter
    private final List<MaterialStack> secondaryMaterials = new ArrayList<>();

    public TagPrefixBuilderJS(ResourceLocation id) {
        super(id);
    }

    @Getter
    @Setter
    private String idPattern;

    @Setter
    @Getter
    public @Nullable String langValue;

    @Getter
    @Setter
    private long materialAmount = -1;

    @Setter
    @Getter
    private boolean unificationEnabled;
    @Setter
    @Getter
    private boolean generateRecycling = false;
    @Setter
    private boolean generateItem;
    @Getter
    @Setter
    private TagPrefix.ItemConstructor itemConstructor = TagPrefixItem::new;
    @Setter
    private boolean generateBlock;
    @Getter
    @Setter
    private TagPrefix.BlockConstructor blockConstructor = MaterialBlock::createAndAddModel;
    @Getter
    @Setter
    private TagPrefix.BlockItemConstructor blockItemConstructor = MaterialBlockItem::new;
    @Getter
    private TagPrefix.BlockProperties blockProperties = new TagPrefix.BlockProperties(() -> RenderType::translucent,
            UnaryOperator.identity());

    @Getter
    @Setter
    private @Nullable Predicate<Material> generationCondition;

    @Nullable
    @Getter
    @Setter
    protected MaterialIconType materialIconType;

    @Setter
    private @Nullable Supplier<Table<TagPrefix, Material, ? extends Supplier<? extends ItemLike>>> itemTable = null;

    @Nullable
    @Getter
    @Setter
    private BiConsumer<Material, List<Component>> tooltip;

    @Getter
    @Setter
    private int maxStackSize = Item.DEFAULT_MAX_STACK_SIZE;

    @Getter
    protected final Set<TagKey<Block>> miningToolTag = new HashSet<>();
    protected final List<TagType> tags = new ArrayList<>();

    public TagPrefixBuilderJS enableRecycling() {
        this.generateRecycling = true;
        return this;
    }

    public TagPrefixBuilderJS defaultTag(String path) {
        return this.defaultTag(path, false);
    }

    public TagPrefixBuilderJS defaultTag(String path, boolean isVanilla) {
        this.tags.add(TagType.withDefaultFormatter(path, isVanilla));
        return this;
    }

    public TagPrefixBuilderJS prefixTag(String path) {
        this.tags.add(TagType.withPrefixFormatter(path));
        return this;
    }

    public TagPrefixBuilderJS prefixOnlyTag(String path) {
        this.tags.add(TagType.withPrefixOnlyFormatter(path));
        return this;
    }

    public TagPrefixBuilderJS unformattedTag(String path) {
        return unformattedTag(path, false);
    }

    public TagPrefixBuilderJS unformattedTag(String path, boolean isVanilla) {
        this.tags.add(TagType.withNoFormatter(path, isVanilla));
        return this;
    }

    public TagPrefixBuilderJS customFormattedTag(String path, BiFunction<String, Material, TagKey<Item>> formatter) {
        this.tags.add(TagType.withCustomFormatter((self, mat) -> formatter.apply(path, mat)));
        return this;
    }

    public TagPrefixBuilderJS filteredUnformattedTag(String path, boolean isVanilla,
                                                     Predicate<Material> materialPredicate) {
        TagType entry = TagType.withNoFormatter(path, isVanilla);
        entry.setFilter(materialPredicate);
        this.tags.add(entry);
        return this;
    }

    public TagPrefixBuilderJS filteredCustomTag(String path, Predicate<Material> materialPredicate,
                                                BiFunction<String, Material, TagKey<Item>> formatter) {
        TagType entry = TagType.withCustomFormatter((self, mat) -> formatter.apply(path, mat));
        entry.setFilter(materialPredicate);
        this.tags.add(entry);
        return this;
    }

    public TagPrefixBuilderJS miningToolTag(TagKey<Block> tag) {
        this.miningToolTag.add(tag);
        return this;
    }

    public TagPrefixBuilderJS blockProperties(TagPrefix.BlockProperties blockProperties) {
        Objects.requireNonNull(blockProperties.renderType(),
                "Could not set blockProperties with null renderType in TagPrefix " + id);
        Objects.requireNonNull(blockProperties.properties(),
                "Could not set blockProperties with null properties in TagPrefix " + id);
        this.blockProperties = blockProperties;
        return this;
    }

    public TagPrefixBuilderJS blockProperties(Supplier<Supplier<RenderType>> renderType,
                                              UnaryOperator<BlockBehaviour.Properties> properties) {
        return this.blockProperties(new TagPrefix.BlockProperties(renderType, properties));
    }

    public TagPrefix create(ResourceLocation id) {
        return new TagPrefix(id)
                .idPattern(idPattern)
                .materialAmount(materialAmount)
                .unificationEnabled(unificationEnabled)
                .generateRecycling(generateRecycling)
                .generateItem(generateItem)
                .itemConstructor(itemConstructor)
                .generateBlock(generateBlock)
                .blockConstructor(blockConstructor)
                .blockItemConstructor(blockItemConstructor)
                .blockProperties(blockProperties)
                .generationCondition(generationCondition)
                .materialIconType(materialIconType)
                .customLookupTable(itemTable)
                .tooltip(tooltip)
                .maxStackSize(maxStackSize);
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        if (langValue != null) lang.add(id.toLanguageKey("tag_prefix"), langValue);
    }

    @Override
    public TagPrefix createObject() {
        TagPrefix newPrefix = create(id);

        newPrefix.setTags(tags);
        newPrefix.miningToolTag().addAll(miningToolTag);

        return newPrefix;
    }
}

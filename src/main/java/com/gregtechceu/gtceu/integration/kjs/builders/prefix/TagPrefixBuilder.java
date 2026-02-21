package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.tag.TagPrefix;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
@Accessors(chain = true)
public class TagPrefixBuilder extends BuilderBase<TagPrefix> {

    public final TagPrefix base;

    @Getter
    private final List<MaterialStack> secondaryMaterials = new ArrayList<>();

    public TagPrefixBuilder(ResourceLocation id) {
        super(id);
        this.base = create(id.getPath());
    }

    public TagPrefix create(String id) {
        return new TagPrefix(id);
    }

    public TagPrefixBuilder idPattern(String idPattern) {
        base.idPattern(idPattern);
        return this;
    }

    public TagPrefixBuilder langValue(String langValue) {
        base.langValue(langValue);
        return this;
    }

    public TagPrefixBuilder materialAmount(long materialAmount) {
        base.materialAmount(materialAmount);
        return this;
    }

    public TagPrefixBuilder unificationEnabled(boolean unificationEnabled) {
        base.unificationEnabled(unificationEnabled);
        return this;
    }

    public TagPrefixBuilder generateRecycling(boolean generateRecycling) {
        base.generateRecycling(generateRecycling);
        return this;
    }

    public TagPrefixBuilder generateItem(boolean generateItem) {
        base.generateItem(generateItem);
        return this;
    }

    public TagPrefixBuilder generateBlock(boolean generateBlock) {
        base.generateBlock(generateBlock);
        return this;
    }

    public TagPrefixBuilder blockProperties(Supplier<Supplier<RenderType>> renderType,
                                            UnaryOperator<BlockBehaviour.Properties> properties) {
        base.blockProperties(new TagPrefix.BlockProperties(renderType, properties));
        return this;
    }

    public TagPrefixBuilder generationCondition(Predicate<Material> generationCondition) {
        base.generationCondition(generationCondition);
        return this;
    }

    public TagPrefixBuilder materialIconType(MaterialIconType materialIconType) {
        base.materialIconType(materialIconType);
        return this;
    }

    public TagPrefixBuilder tooltip(BiConsumer<Material, List<Component>> tooltip) {
        base.tooltip(tooltip);
        return this;
    }

    public TagPrefixBuilder maxStackSize(int maxStackSize) {
        base.maxStackSize(maxStackSize);
        return this;
    }

    public TagPrefixBuilder setIgnored(Material material, ItemLike... items) {
        base.setIgnored(material, items);
        return this;
    }

    public TagPrefixBuilder addSecondaryMaterial(MaterialStack secondaryMaterial) {
        base.addSecondaryMaterial(secondaryMaterial);
        return this;
    }

    public TagPrefixBuilder defaultTagPath(String path) {
        base.defaultTagPath(path);
        return this;
    }

    public TagPrefixBuilder defaultTagPath(String path, boolean isVanilla) {
        base.defaultTagPath(path, isVanilla);
        return this;
    }

    public TagPrefixBuilder prefixTagPath(String path) {
        base.prefixTagPath(path);
        return this;
    }

    public TagPrefixBuilder prefixOnlyTagPath(String path) {
        base.prefixOnlyTagPath(path);
        return this;
    }

    public TagPrefixBuilder unformattedTagPath(String path) {
        base.unformattedTagPath(path);
        return this;
    }

    public TagPrefixBuilder unformattedTagPath(String path, boolean isVanilla) {
        base.unformattedTagPath(path, isVanilla);
        return this;
    }

    public TagPrefixBuilder customTagPath(String path, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        base.customTagPath(path, formatter);
        return this;
    }

    public TagPrefixBuilder customTagPredicate(String path, boolean isVanilla, Predicate<Material> materialPredicate) {
        base.customTagPredicate(path, isVanilla, materialPredicate);
        return this;
    }

    public TagPrefixBuilder miningToolTag(TagKey<Block> tag) {
        base.miningToolTag(tag);
        return this;
    }

    @Override
    public TagPrefix createObject() {
        return base;
    }
}

package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.built.KJSTagPrefix;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@Accessors(chain = true)
public abstract class TagPrefixBuilder extends BuilderBase<TagPrefix> {

    public final KJSTagPrefix base;

    @Getter
    private final List<MaterialStack> secondaryMaterials = new ArrayList<>();

    public TagPrefixBuilder(ResourceLocation id) {
        super(id);
        this.base = create(id.getPath());
    }

    public abstract KJSTagPrefix create(String id);

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

    public TagPrefixBuilder generateItem(boolean generateItem) {
        base.generateItem(generateItem);
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

    public TagPrefixBuilder customTagPath(String path, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        base.customTagPath(path, formatter);
        return this;
    }

    public TagPrefixBuilder miningToolTag(String path) {
        this.miningToolTag(TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(path)));
        return this;
    }

    public TagPrefixBuilder miningToolTag(TagKey<Block> tag) {
        base.miningToolTag(tag);
        return this;
    }

    @Override
    public TagPrefix register() {
        return value = base;
    }
}

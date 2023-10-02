package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.IGTTagLoader;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin<T> implements IGTTagLoader<T> {

    @Nullable
    @Unique
    private Registry<T> gtceu$storedRegistry;

    @Inject(method = "load", at = @At(value = "RETURN"))
    public void gtceu$load(ResourceManager resourceManager, CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        var tagMap = cir.getReturnValue();
        if (gtceu$getRegistry() == null) return;
        if (gtceu$getRegistry() == BuiltInRegistries.ITEM) {
            ChemicalHelper.UNIFICATION_ENTRY_ITEM.forEach((entry, itemLikes) -> {
                if (itemLikes.isEmpty()) return;
                var material = entry.material;
                if (material != null) {
                    var materialTags = entry.tagPrefix.getAllItemTags(material);
                    for (TagKey<Item> materialTag : materialTags) {
                        List<TagLoader.EntryWithSource> tags = new ArrayList<>();
                        itemLikes.forEach(item -> tags.add(new TagLoader.EntryWithSource(TagEntry.element(BuiltInRegistries.ITEM.getKey(item.asItem())), GTValues.CUSTOM_TAG_SOURCE)));
                        tagMap.computeIfAbsent(materialTag.location(), path -> new ArrayList<>()).addAll(tags);
                    }

                }
            });

            GTItems.TOOL_ITEMS.rowMap().forEach((toolTier, map) -> {
                map.forEach((type, item) -> {
                    if (item != null) {
                        var entry = new TagLoader.EntryWithSource(TagEntry.element(item.getId()), GTValues.CUSTOM_TAG_SOURCE);
                        //GTCEu.LOGGER.info("Tool tag registered. Tier: " + toolTier.getLevel() +  ". Item: " + item.getId() + ". Block type: " + type.harvestTag);
                        tagMap.computeIfAbsent(type.itemTag.location(), path -> new ArrayList<>()).add(entry);
                    }
                });
            });
        } else if (gtceu$getRegistry() == BuiltInRegistries.BLOCK) {
            GTBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTRegistries.MACHINES.forEach(machine -> {
                ResourceLocation id = machine.getId();
                tagMap.computeIfAbsent(GTToolType.WRENCH.harvestTag.location(), path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(id), GTValues.CUSTOM_TAG_SOURCE));
                if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                    tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_PICKAXE.location(), path -> new ArrayList<>())
                            .add(new TagLoader.EntryWithSource(TagEntry.element(id), GTValues.CUSTOM_TAG_SOURCE));
                }
            });

            GTBlocks.ALL_FUSION_CASINGS.forEach((casingType, block) -> {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block.get());
                tagMap.computeIfAbsent(CustomTags.TOOL_TIERS[casingType.getHarvestLevel()].location(), path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(blockId), GTValues.CUSTOM_TAG_SOURCE));
            });
        } else if (gtceu$getRegistry() == BuiltInRegistries.FLUID) {
            for (Material material : GTRegistries.MATERIALS) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                        Fluid fluid = property.getStorage().get(key);
                        if (fluid != null) {
                            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                            tagMap.computeIfAbsent(TagUtil.createFluidTag(fluidId.getPath()).location(), path -> new ArrayList<>())
                                    .add(new TagLoader.EntryWithSource(TagEntry.element(fluidId), GTValues.CUSTOM_TAG_SOURCE));
                        }
                    }
                }
            }
        }

    }

    @Override
    @Nullable
    public Registry<T> gtceu$getRegistry() {
        return gtceu$storedRegistry;
    }

    @Override
    public void gtceu$setRegistry(Registry<T> registry) {
        this.gtceu$storedRegistry = registry;
    }

}

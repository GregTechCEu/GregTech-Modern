package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MixinHelpers {

    public static void addMaterialBlockTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap, TagPrefix prefix, Map<Material, ? extends BlockEntry<? extends Block>> map) {
        // Add tool tags
        if (!prefix.miningToolTag().isEmpty()) {
            map.forEach((material, block) -> {
                tagMap.computeIfAbsent(CustomTags.TOOL_TIERS[material.getBlockHarvestLevel()].location(), path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(block.getId()), GTValues.CUSTOM_TAG_SOURCE));

                var entry = new TagLoader.EntryWithSource(TagEntry.element(block.getId()), GTValues.CUSTOM_TAG_SOURCE);
                if (material.hasProperty(PropertyKey.WOOD)) {
                    tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_AXE.location(), path -> new ArrayList<>()).add(entry);
                } else {
                    for (var tag : prefix.miningToolTag()) {
                        tagMap.computeIfAbsent(tag.location(), path -> new ArrayList<>()).add(entry);
                    }
                    if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                        tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_PICKAXE.location(), path -> new ArrayList<>()).add(entry);
                    }
                }
            });
        }
        // Copy item tags to blocks
        map.forEach((material, block) -> {
            for (TagKey<Block> blockTag : prefix.getAllBlockTags(material)) {
                tagMap.computeIfAbsent(blockTag.location(), path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(block.getId()), GTValues.CUSTOM_TAG_SOURCE));
            }
        });
    }

    public static void addMaterialBlockLootTables(Map<ResourceLocation, LootTable> lootTables, TagPrefix prefix, Map<Material, ? extends BlockEntry<? extends Block>> map) {
        map.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
            ((BlockBehaviourAccessor)blockEntry.get()).setDrops(lootTableId);
            lootTables.put(lootTableId, new VanillaBlockLoot().createSingleItemTable(blockEntry.get()).setParamSet(LootContextParamSets.BLOCK).build());
        });
    }


    @ExpectPlatform
    public static void addFluidTexture(Material material, FluidProperty prop) {
        throw new AssertionError();
    }
}

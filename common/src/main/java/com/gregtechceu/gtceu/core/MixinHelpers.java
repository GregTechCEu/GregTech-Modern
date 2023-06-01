package com.gregtechceu.gtceu.core;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MixinHelpers {

    public static void addMaterialBlockTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap, TagPrefix prefix, Map<Material, ? extends BlockEntry<? extends Block>> map) {
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
                }
            });
        }
    }

    public static void addMaterialBlockLootTables(ImmutableMap.Builder<ResourceLocation, LootTable> lootTables, TagPrefix prefix, Map<Material, ? extends BlockEntry<? extends Block>> map) {
        map.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
            ((BlockBehaviourAccessor)blockEntry.get()).setDrops(lootTableId);
            lootTables.put(lootTableId, BlockLoot.createSingleItemTable(blockEntry.get()).build());
        });
    }

}

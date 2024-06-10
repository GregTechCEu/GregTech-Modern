package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import com.tterrag.registrate.util.entry.BlockEntry;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MixinHelpers {

    public static <T> void generateGTDynamicTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                                 Registry<T> registry) {
        if (registry == BuiltInRegistries.ITEM) {
            ChemicalHelper.UNIFICATION_ENTRY_ITEM.forEach((entry, itemLikes) -> {
                if (itemLikes.isEmpty()) return;
                var material = entry.material;
                if (material != null) {
                    var materialTags = entry.tagPrefix.getAllItemTags(material);
                    for (TagKey<Item> materialTag : materialTags) {
                        List<TagLoader.EntryWithSource> tags = new ArrayList<>();
                        itemLikes.forEach(item -> tags.add(new TagLoader.EntryWithSource(
                                TagEntry.element(BuiltInRegistries.ITEM.getKey(item.get().asItem())),
                                GTValues.CUSTOM_TAG_SOURCE)));
                        tagMap.computeIfAbsent(materialTag.location(), path -> new ArrayList<>()).addAll(tags);
                    }

                }
            });

            GTItems.TOOL_ITEMS.rowMap().forEach((material, map) -> {
                map.forEach((type, item) -> {
                    if (item != null) {
                        var entry = new TagLoader.EntryWithSource(TagEntry.element(item.getId()),
                                GTValues.CUSTOM_TAG_SOURCE);
                        for (TagKey<Item> tag : type.itemTags) {
                            tagMap.computeIfAbsent(tag.location(), path -> new ArrayList<>()).add(entry);
                        }
                    }
                });
            });
        } else if (registry == BuiltInRegistries.BLOCK) {
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
                tagMap.computeIfAbsent(CustomTags.MINEABLE_WITH_WRENCH.location(), path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(id), GTValues.CUSTOM_TAG_SOURCE));
                if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                    tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_PICKAXE.location(), path -> new ArrayList<>())
                            .add(new TagLoader.EntryWithSource(TagEntry.element(id), GTValues.CUSTOM_TAG_SOURCE));
                }
            });

            GTBlocks.ALL_FUSION_CASINGS.forEach((casingType, block) -> {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block.get());
                tagMap.computeIfAbsent(CustomTags.TOOL_TIERS[casingType.getHarvestLevel()].location(),
                        path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(blockId), GTValues.CUSTOM_TAG_SOURCE));
            });
        } else if (registry == BuiltInRegistries.FLUID) {
            for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                        Fluid fluid = property.getStorage().get(key);
                        if (fluid != null) {
                            ChemicalHelper.FLUID_MATERIAL.put(fluid, material);

                            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                            tagMap.computeIfAbsent(TagUtil.createFluidTag(fluidId.getPath()).location(),
                                    path -> new ArrayList<>())
                                    .add(new TagLoader.EntryWithSource(TagEntry.element(fluidId),
                                            GTValues.CUSTOM_TAG_SOURCE));
                        }
                    }
                }
            }
        }
    }

    public static void addMaterialBlockTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                            TagPrefix prefix,
                                            Map<Material, ? extends BlockEntry<? extends Block>> map) {
        // Add tool tags
        if (!prefix.miningToolTag().isEmpty()) {
            map.forEach((material, block) -> {
                tagMap.computeIfAbsent(CustomTags.TOOL_TIERS[material.getBlockHarvestLevel()].location(),
                        path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(block.getId()),
                                GTValues.CUSTOM_TAG_SOURCE));

                var entry = new TagLoader.EntryWithSource(TagEntry.element(block.getId()), GTValues.CUSTOM_TAG_SOURCE);
                if (material.hasProperty(PropertyKey.WOOD)) {
                    tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_AXE.location(), path -> new ArrayList<>())
                            .add(entry);
                } else {
                    for (var tag : prefix.miningToolTag()) {
                        tagMap.computeIfAbsent(tag.location(), path -> new ArrayList<>()).add(entry);
                    }
                    if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                        tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_PICKAXE.location(), path -> new ArrayList<>())
                                .add(entry);
                    }
                }
            });
        }
        // Copy item tags to blocks
        map.forEach((material, block) -> {
            for (TagKey<Block> blockTag : prefix.getAllBlockTags(material)) {
                tagMap.computeIfAbsent(blockTag.location(), path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(block.getId()),
                                GTValues.CUSTOM_TAG_SOURCE));
            }
        });
    }


    public static void generateGTDynamicLoot(TriConsumer<ResourceLocation, LootTable, RegistryAccess.Frozen> lootTables,
                                             final RegistryAccess.Frozen access) {
        final VanillaBlockLoot blockLoot = new VanillaBlockLoot(access);

        Holder<Enchantment> fortune = access.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FORTUNE);
        GTBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                final TagPrefix.OreType type = TagPrefix.ORES.get(prefix);
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(blockEntry.getId().getNamespace(),
                            "blocks/" + blockEntry.getId().getPath());
                    Block block = blockEntry.get();

                    if (!type.shouldDropAsItem() && !ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes) {
                        TagPrefix orePrefix = type.isDoubleDrops() ? TagPrefix.oreNetherrack : TagPrefix.ore;
                        block = ChemicalHelper.getBlock(orePrefix, material);
                    }

                    ItemStack dropItem = ChemicalHelper.get(TagPrefix.rawOre, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.gem, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.dust, material);
                    int oreMultiplier = type.isDoubleDrops() ? 2 : 1;

                    LootTable.Builder builder = blockLoot.createSilkTouchDispatchTable(block,
                            blockLoot.applyExplosionDecay(block,
                                    LootItem.lootTableItem(dropItem.getItem())
                                            .apply(SetItemCountFunction.setCount(
                                                    UniformGenerator.between(1, Math.max(1, oreMultiplier))))));
                    // .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))); //disable fortune for
                    // balance reasons. (for now, until we can think of a better solution.)

                    LootPool.Builder pool = LootPool.lootPool();
                    boolean isEmpty = true;
                    for (MaterialStack secondaryMaterial : prefix.secondaryMaterials()) {
                        if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                            ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                            pool.add(LootItem.lootTableItem(dustStack.getItem())
                                    .when(hasSilkTouch(access).invert())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                    .apply(ApplyBonusCount.addUniformBonusCount(fortune))
                                    .apply(LimitCount.limitCount(IntRange.range(0, 2)))
                                    .apply(ApplyExplosionDecay.explosionDecay()));
                            isEmpty = false;
                        }
                    }
                    if (!isEmpty) {
                        builder.withPool(pool);
                    }
                    lootTables.accept(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build(), access);
                    ((BlockBehaviourAccessor) blockEntry.get())
                            .setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
                });
            } else {
                MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
            }
        });
        GTBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTBlocks.SURFACE_ROCK_BLOCKS.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(blockEntry.getId().getNamespace(),
                    "blocks/" + blockEntry.getId().getPath());
            LootTable.Builder builder = blockLoot
                    .createSingleItemTable(ChemicalHelper.get(TagPrefix.dustTiny, material).getItem(),
                            UniformGenerator.between(3, 5))
                    .apply(ApplyBonusCount.addUniformBonusCount(fortune));
            lootTables.accept(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build(), access);
            ((BlockBehaviourAccessor) blockEntry.get())
                    .setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
        });
        GTRegistries.MACHINES.forEach(machine -> {
            Block block = machine.getBlock();
            ResourceLocation id = machine.getId();
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "blocks/" + id.getPath());
            ((BlockBehaviourAccessor) block).setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
            lootTables.accept(lootTableId,
                    blockLoot.createSingleItemTable(block).setParamSet(LootContextParamSets.BLOCK).build(), access);
        });
    }

    public static LootItemCondition.Builder hasSilkTouch(HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = provider.lookupOrThrow(Registries.ENCHANTMENT);
        return MatchTool.toolMatches(
                ItemPredicate.Builder.item()
                        .withSubPredicate(
                                ItemSubPredicates.ENCHANTMENTS,
                                ItemEnchantmentsPredicate.enchantments(
                                        List.of(new EnchantmentPredicate(registrylookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                                )
                        )
        );
    }

    public static void addMaterialBlockLootTables(TriConsumer<ResourceLocation, LootTable, RegistryAccess.Frozen> lootTables, TagPrefix prefix,
                                                  Map<Material, ? extends BlockEntry<? extends Block>> map, VanillaBlockLoot blockLoot, RegistryAccess.Frozen access) {
        map.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(blockEntry.getId().getNamespace(),
                    "blocks/" + blockEntry.getId().getPath());
            ((BlockBehaviourAccessor) blockEntry.get())
                    .setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
            lootTables.accept(lootTableId,
                    blockLoot.createSingleItemTable(blockEntry.get()).setParamSet(LootContextParamSets.BLOCK).build(), access);
        });
    }

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (value != null) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
            if (extensions instanceof GTClientFluidTypeExtensions gtExtensions && value.getBuilder() != null) {
                gtExtensions.setFlowingTexture(value.getBuilder().flowing());
                gtExtensions.setStillTexture(value.getBuilder().still());
            }
        }
    }
}

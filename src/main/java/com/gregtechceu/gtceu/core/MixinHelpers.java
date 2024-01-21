package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.loot.DungeonLootLoader;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MixinHelpers {

    public static <T> void generateGTDynamicTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap, Registry<T> registry) {
        if (registry == BuiltInRegistries.ITEM) {
            ChemicalHelper.UNIFICATION_ENTRY_ITEM.forEach((entry, itemLikes) -> {
                if (itemLikes.isEmpty()) return;
                var material = entry.material;
                if (material != null) {
                    var materialTags = entry.tagPrefix.getAllItemTags(material);
                    for (TagKey<Item> materialTag : materialTags) {
                        List<TagLoader.EntryWithSource> tags = new ArrayList<>();
                        itemLikes.forEach(item -> tags.add(new TagLoader.EntryWithSource(TagEntry.element(BuiltInRegistries.ITEM.getKey(item.get().asItem())), GTValues.CUSTOM_TAG_SOURCE)));
                        tagMap.computeIfAbsent(materialTag.location(), path -> new ArrayList<>()).addAll(tags);
                    }

                }
            });

            GTItems.TOOL_ITEMS.rowMap().forEach((material, map) -> {
                map.forEach((type, item) -> {
                    if (item != null) {
                        var entry = new TagLoader.EntryWithSource(TagEntry.element(item.getId()), GTValues.CUSTOM_TAG_SOURCE);
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
                tagMap.computeIfAbsent(GTToolType.WRENCH.harvestTags.get(0).location(), path -> new ArrayList<>())
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
        } else if (registry == BuiltInRegistries.FLUID) {
            for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                        Fluid fluid = property.getStorage().get(key);
                        if (fluid != null) {
                            ChemicalHelper.FLUID_MATERIAL.put(fluid, material);

                            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                            tagMap.computeIfAbsent(TagUtil.createFluidTag(fluidId.getPath()).location(), path -> new ArrayList<>())
                                .add(new TagLoader.EntryWithSource(TagEntry.element(fluidId), GTValues.CUSTOM_TAG_SOURCE));
                        }
                    }
                }
            }
        }
    }

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

    private static final VanillaBlockLoot BLOCK_LOOT = new VanillaBlockLoot();

    public static void generateGTDynamicLoot(Map<ResourceLocation, LootTable> lootTables) {
        GTBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                final TagPrefix.OreType type = TagPrefix.ORES.get(prefix);
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
                    Block block = blockEntry.get();

                    if (!type.shouldDropAsItem() && !ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes) {
                        TagPrefix orePrefix = type.isDoubleDrops() ? TagPrefix.oreNetherrack : TagPrefix.ore;
                        block = ChemicalHelper.getBlock(orePrefix, material);
                    }

                    ItemStack dropItem = ChemicalHelper.get(TagPrefix.rawOre, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.gem, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.dust, material);
                    int oreMultiplier = type.isDoubleDrops() ? 2 : 1;

                    LootTable.Builder builder = BlockLootSubProvider.createSilkTouchDispatchTable(block,
                        BLOCK_LOOT.applyExplosionDecay(block,
                            LootItem.lootTableItem(dropItem.getItem())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, Math.max(1, material.getProperty(PropertyKey.ORE).getOreMultiplier() * oreMultiplier))))));
                    //.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))); //disable fortune for balance reasons. (for now, until we can think of a better solution.)

                    Supplier<Material> outputDustMat = type.material();
                    if (outputDustMat != null) {
                        builder.withPool(LootPool.lootPool().add(
                            LootItem.lootTableItem(ChemicalHelper.get(TagPrefix.dust, outputDustMat.get()).getItem())
                                .when(BlockLootSubProvider.HAS_NO_SILK_TOUCH)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                                .apply(LimitCount.limitCount(IntRange.range(0, 2)))
                                .apply(ApplyExplosionDecay.explosionDecay())));
                    }
                    lootTables.put(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build());
                    ((BlockBehaviourAccessor)blockEntry.get()).setDrops(lootTableId);
                });
            } else {
                MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
            }
        });
        GTBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map);
        });
        GTBlocks.SURFACE_ROCK_BLOCKS.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
            LootTable.Builder builder = BLOCK_LOOT.createSingleItemTable(ChemicalHelper.get(TagPrefix.dustTiny, material).getItem(), UniformGenerator.between(3, 5))
                .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE));
            lootTables.put(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build());
            ((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
        });
        GTRegistries.MACHINES.forEach(machine -> {
            Block block = machine.getBlock();
            ResourceLocation id = machine.getId();
            ResourceLocation lootTableId = new ResourceLocation(id.getNamespace(), "blocks/" + id.getPath());
            ((BlockBehaviourAccessor)block).setDrops(lootTableId);
            lootTables.put(lootTableId, BLOCK_LOOT.createSingleItemTable(block).setParamSet(LootContextParamSets.BLOCK).build());
        });
    }

    public static void addMaterialBlockLootTables(Map<ResourceLocation, LootTable> lootTables, TagPrefix prefix, Map<Material, ? extends BlockEntry<? extends Block>> map) {
        map.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = new ResourceLocation(blockEntry.getId().getNamespace(), "blocks/" + blockEntry.getId().getPath());
            ((BlockBehaviourAccessor)blockEntry.get()).setDrops(lootTableId);
            lootTables.put(lootTableId, BLOCK_LOOT.createSingleItemTable(blockEntry.get()).setParamSet(LootContextParamSets.BLOCK).build());
        });
    }

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (value != null) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
            if (extensions instanceof GTClientFluidTypeExtensions gtExtensions) {
                gtExtensions.setFlowingTexture(value.getFlowTexture());
                gtExtensions.setStillTexture(value.getStillTexture());
                gtExtensions.setTintColor(material.getMaterialARGB());
            }
        }
    }

    public static List<PackResources> addDynamicDataPack(Collection<PackResources> packs) {
        List<PackResources> packResources = new ArrayList<>(packs);
        // Clear old data
        GTDynamicDataPack.clearServer();

        // Register recipes & unification data again
        long startTime = System.currentTimeMillis();
        ChemicalHelper.reinitializeUnification();
        GTRecipes.recipeAddition(GTDynamicDataPack::addRecipe);
        // Initialize dungeon loot additions
        DungeonLootLoader.init();
        GTCEu.LOGGER.info("GregTech Data loading took {}ms", System.currentTimeMillis() - startTime);

        // Load the data
        packResources.add(new GTDynamicDataPack("gtceu:dynamic_data", AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet())));
        return packResources;
    }

    public static List<PackResources> addDynamicResourcePack(Collection<PackResources> packs) {
        List<PackResources> packResources = new ArrayList<>(packs);
        // Clear old data
        GTDynamicResourcePack.clearClient();

        // Load the data
        packResources.add(0, new GTDynamicResourcePack("gtceu:dynamic_assets", AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet())));
        return packResources;
    }

    // unused on purpose. Do not call, will destroy ram usage.
    public static void initializeDynamicTextures() {
        //MaterialBlockRenderer.initTextures();
        //TagPrefixItemRenderer.initTextures();
    }
}

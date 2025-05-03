package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.fluid.GTFluid;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.ItemMaterialData;
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
import com.gregtechceu.gtceu.data.block.GTMaterialBlocks;
import com.gregtechceu.gtceu.data.item.GTMaterialItems;
import com.gregtechceu.gtceu.data.tag.CustomTags;

import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import com.tterrag.registrate.util.entry.BlockEntry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;

public class MixinHelpers {

    /**
     * This is set at the start of:
     * <ul>
     * <li>{@link BlockEntity#loadAdditional(CompoundTag, HolderLookup.Provider)}
     * <li>{@link BlockEntity#saveAdditional(CompoundTag, HolderLookup.Provider)}
     * </ul>
     * and cleared when the method exits.
     */
    public static final ThreadLocal<HolderLookup.Provider> CURRENT_BE_SAVE_LOAD_REGISTRIES = new ThreadLocal<>();

    /**
     * @return the registry access provided to the currently saving/loading block entity
     *         or null if no saving or loading is happening.
     */
    public static HolderLookup.Provider getCurrentBERegistries() {
        return CURRENT_BE_SAVE_LOAD_REGISTRIES.get();
    }

    public static <T> void generateGTDynamicTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                                 Registry<T> registry) {
        if (registry == BuiltInRegistries.ITEM) {
            ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP.forEach((entry, itemLikes) -> {
                if (itemLikes.isEmpty()) return;
                var material = entry.material();
                if (!material.isNull()) {
                    var materialTags = entry.tagPrefix().getAllItemTags(material);
                    for (TagKey<Item> materialTag : materialTags) {
                        List<TagLoader.EntryWithSource> tags = new ArrayList<>();
                        itemLikes.forEach(item -> tags.add(new TagLoader.EntryWithSource(
                                TagEntry.element(BuiltInRegistries.ITEM.getKey(item.get().asItem())),
                                GTValues.CUSTOM_TAG_SOURCE)));
                        tagMap.computeIfAbsent(materialTag.location(), path -> new ArrayList<>()).addAll(tags);
                    }

                }
            });

            GTMaterialItems.TOOL_ITEMS.rowMap().forEach((material, map) -> {
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
            GTMaterialBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTMaterialBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTMaterialBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTMaterialBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
                MixinHelpers.addMaterialBlockTags(tagMap, prefix, map);
            });
            GTRegistries.MACHINES.forEach(machine -> {
                ResourceLocation id = machine.getId();
                tagMap.computeIfAbsent(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH.location(),
                        path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(id), GTValues.CUSTOM_TAG_SOURCE));
            });

            GTBlocks.ALL_FUSION_CASINGS.forEach((casingType, block) -> {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block.get());
                tagMap.computeIfAbsent(CustomTags.TOOL_TIERS[casingType.getHarvestLevel()].location(),
                        path -> new ArrayList<>())
                        .add(new TagLoader.EntryWithSource(TagEntry.element(blockId), GTValues.CUSTOM_TAG_SOURCE));
            });

            // if config is NOT enabled, add the pickaxe/axe tags to the "configurable" mineability tags
            if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                var tagList = tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_PICKAXE.location(),
                        path -> new ArrayList<>());

                tagList.add(new TagLoader.EntryWithSource(
                        TagEntry.tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH.location()),
                        GTValues.CUSTOM_TAG_SOURCE));
                tagList.add(new TagLoader.EntryWithSource(
                        TagEntry.tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER.location()),
                        GTValues.CUSTOM_TAG_SOURCE));
            }
        } else if (registry == BuiltInRegistries.FLUID) {
            for (Material material : GTCEuAPI.materialManager) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                        Fluid fluid = property.getStorage().get(key);
                        if (fluid != null) {
                            ItemMaterialData.FLUID_MATERIAL.put(fluid, material);

                            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                            TagLoader.EntryWithSource entry = new TagLoader.EntryWithSource(TagEntry.element(fluidId),
                                    GTValues.CUSTOM_TAG_SOURCE);
                            tagMap.computeIfAbsent(TagUtil.createFluidTag(fluidId.getPath()).location(),
                                    path -> new ArrayList<>())
                                    .add(entry);
                            if (fluid instanceof GTFluid gtFluid) {
                                tagMap.computeIfAbsent(gtFluid.getState().getTagKey().location(),
                                        path -> new ArrayList<>())
                                        .add(entry);
                            } else {
                                ResourceLocation fluidKeyTag = key.getDefaultFluidState().getTagKey().location();
                                tagMap.computeIfAbsent(fluidKeyTag, path -> new ArrayList<>())
                                        .add(entry);
                            }
                        }
                    }
                }
                if (material.hasProperty(PropertyKey.ALLOY_BLAST)) {
                    Fluid fluid = material.getProperty(PropertyKey.FLUID).getStorage().get(FluidStorageKeys.MOLTEN);
                    if (fluid != null) {
                        ResourceLocation moltenID = BuiltInRegistries.FLUID.getKey(fluid);
                        tagMap.computeIfAbsent(CustomTags.MOLTEN_FLUIDS.location(),
                                path -> new ArrayList<>())
                                .add(new TagLoader.EntryWithSource(TagEntry.element(moltenID),
                                        GTValues.CUSTOM_TAG_SOURCE));
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

        Holder<Enchantment> fortune = access.registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.FORTUNE);
        GTMaterialBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                final TagPrefix.OreType type = TagPrefix.ORES.get(prefix);
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = blockEntry.getId().withPrefix("blocks/");
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
                                            .apply(SetItemCountFunction
                                                    .setCount(ConstantValue.exactly(oreMultiplier)))));
                    // .apply(ApplyBonusCount.addOreBonusCount(Enchantments.FORTUNE)))); //disable fortune for
                    // balance reasons. (for now, until we can think of a better solution.)

                    LootPool.Builder pool = LootPool.lootPool();
                    boolean isEmpty = true;
                    for (MaterialStack secondaryMaterial : prefix.secondaryMaterials()) {
                        if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                            ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                            pool.add(LootItem.lootTableItem(dustStack.getItem())
                                    .when(blockLoot.doesNotHaveSilkTouch())
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
        GTMaterialBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTMaterialBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTMaterialBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTMaterialBlocks.SURFACE_ROCK_BLOCKS.forEach((material, blockEntry) -> {
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
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                    "blocks/" + id.getPath());
            ((BlockBehaviourAccessor) block).setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
            lootTables.accept(lootTableId,
                    blockLoot.createSingleItemTable(block).setParamSet(LootContextParamSets.BLOCK).build(), access);
        });
    }

    public static void addMaterialBlockLootTables(TriConsumer<ResourceLocation, LootTable, RegistryAccess.Frozen> lootTables,
                                                  TagPrefix prefix,
                                                  Map<Material, ? extends BlockEntry<? extends Block>> map,
                                                  VanillaBlockLoot blockLoot, RegistryAccess.Frozen access) {
        map.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = blockEntry.getId().withPrefix("blocks/");
            ((BlockBehaviourAccessor) blockEntry.get())
                    .setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
            lootTables.accept(lootTableId,
                    blockLoot.createSingleItemTable(blockEntry.get()).setParamSet(LootContextParamSets.BLOCK).build(),
                    access);
        });
    }

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (value != null) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
            if (extensions instanceof GTClientFluidTypeExtensions gtExtensions && value.getBuilder() != null) {
                value.getBuilder().determineTextures(material, value.getKey());

                gtExtensions.setFlowingTexture(value.getBuilder().flowing());
                gtExtensions.setStillTexture(value.getBuilder().still());
            }
        }
    }
}

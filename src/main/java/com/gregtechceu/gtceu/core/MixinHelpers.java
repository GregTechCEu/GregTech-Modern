package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.fluid.FluidState;
import com.gregtechceu.gtceu.api.fluid.GTFluid;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.material.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.block.GTMaterialBlocks;
import com.gregtechceu.gtceu.data.item.GTMaterialItems;
import com.gregtechceu.gtceu.data.tag.CustomTags;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockFluidVeinKubeEvent;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockOreVeinKubeEvent;
import com.gregtechceu.gtceu.integration.kjs.events.GTOreVeinKubeEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
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
import net.neoforged.neoforge.common.Tags;

import com.tterrag.registrate.util.entry.BlockEntry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
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
                if (material.isNull()) return;
                var entries = itemLikes.stream()
                        .map(Supplier::get)
                        .map(MixinHelpers::makeItemEntry)
                        .collect(toArrayList());

                var prefixTagKeys = entry.tagPrefix().getAllItemTags(material);
                for (TagKey<Item> prefixTag : prefixTagKeys) {
                    tagMap.computeIfAbsent(prefixTag.location(), path -> new ArrayList<>()).addAll(entries);
                }
                for (TagKey<Item> materialTag : material.getItemTags()) {
                    tagMap.computeIfAbsent(materialTag.location(), path -> new ArrayList<>()).addAll(entries);
                }

                if (entry.tagPrefix() == TagPrefix.crushed && material.hasProperty(PropertyKey.ORE)) {
                    OreProperty ore = material.getProperty(PropertyKey.ORE);
                    Material washedIn = ore.getWashedIn().first();
                    if (washedIn.isNull()) return;
                    ResourceLocation generalTag = CustomTags.CHEM_BATH_WASHABLE.location();
                    ResourceLocation specificTag = generalTag.withSuffix("/" + washedIn.getName());

                    tagMap.computeIfAbsent(generalTag, path -> new ArrayList<>()).addAll(entries);
                    tagMap.computeIfAbsent(specificTag, path -> new ArrayList<>()).addAll(entries);
                }
            });

            GTMaterialItems.TOOL_ITEMS.rowMap().forEach((material, map) -> {
                map.values().forEach(item -> {
                    if (item == null) return;
                    var entry = makeItemEntry(item);
                    for (TagKey<Item> tag : item.get().getToolType().itemTags) {
                        tagMap.computeIfAbsent(tag.location(), path -> new ArrayList<>()).add(entry);
                    }
                });
            });

            GTMaterialItems.ARMOR_ITEMS.rowMap().forEach((material, map) -> {
                map.forEach((type, item) -> {
                    if (item != null) {
                        var entry = new TagLoader.EntryWithSource(TagEntry.element(item.getId()),
                                GTValues.CUSTOM_TAG_SOURCE);
                        tagMap.computeIfAbsent(ItemTags.TRIMMABLE_ARMOR.location(), $ -> new ArrayList<>())
                                .add(entry);
                        tagMap.computeIfAbsent(switch (type) {
                            case HELMET -> ItemTags.HEAD_ARMOR.location();
                            case CHESTPLATE -> ItemTags.CHEST_ARMOR.location();
                            case LEGGINGS -> ItemTags.LEG_ARMOR.location();
                            case BOOTS -> ItemTags.FOOT_ARMOR.location();
                            case BODY -> Tags.Items.ARMORS.location();
                        }, $ -> new ArrayList<>()).add(entry);
                        tagMap.computeIfAbsent(switch (type) {
                            case HELMET -> ItemTags.HEAD_ARMOR_ENCHANTABLE.location();
                            case CHESTPLATE -> ItemTags.CHEST_ARMOR_ENCHANTABLE.location();
                            case LEGGINGS -> ItemTags.LEG_ARMOR_ENCHANTABLE.location();
                            case BOOTS -> ItemTags.FOOT_ARMOR_ENCHANTABLE.location();
                            case BODY -> Tags.Items.ENCHANTABLES.location();
                        }, $ -> new ArrayList<>()).add(entry);
                    }
                });
            });

            if (!GTCEu.Mods.isAE2Loaded()) {
                return;
            }
            // If AE2 is loaded, add the Fluid P2P attunement tag to all the buckets
            var p2pFluidAttunements = ResourceLocation.fromNamespaceAndPath(GTValues.MODID_APPENG,
                    "p2p_attunements/fluid_p2p_tunnel");
            for (Material material : GTCEuAPI.materialManager) {
                FluidProperty property = material.getProperty(PropertyKey.FLUID);
                if (property == null) {
                    continue;
                }
                for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                    Fluid fluid = property.get(key);
                    if (fluid == null || fluid.getBucket() == Items.AIR) {
                        continue;
                    }
                    var entry = makeItemEntry(fluid.getBucket());
                    tagMap.computeIfAbsent(p2pFluidAttunements, path -> new ArrayList<>()).add(entry);
                }
            }
        } else if (registry == BuiltInRegistries.BLOCK) {
            ItemMaterialData.MATERIAL_ENTRY_BLOCK_MAP.forEach((entry, blocks) -> {
                if (blocks.isEmpty()) return;
                var material = entry.material();
                if (material.isNull()) return;

                var entries = blocks.stream().map(MixinHelpers::makeBlockEntry).collect(toArrayList());
                var materialTags = entry.tagPrefix().getAllBlockTags(material);
                for (TagKey<Block> materialTag : materialTags) {
                    tagMap.computeIfAbsent(materialTag.location(), path -> new ArrayList<>()).addAll(entries);
                }
                // Add tool tags
                if (!entry.isIgnored() && !entry.tagPrefix().miningToolTag().isEmpty()) {
                    tagMap.computeIfAbsent(CustomTags.TOOL_TIERS[material.getBlockHarvestLevel()].location(),
                            path -> new ArrayList<>()).addAll(entries);
                    if (material.hasProperty(PropertyKey.WOOD)) {
                        tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_AXE.location(), path -> new ArrayList<>())
                                .addAll(entries);
                    } else {
                        for (var tag : entry.tagPrefix().miningToolTag()) {
                            tagMap.computeIfAbsent(tag.location(), path -> new ArrayList<>()).addAll(entries);
                        }
                    }
                }
            });

            GTRegistries.MACHINES.forEach(machine -> {
                tagMap.computeIfAbsent(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH.location(),
                        path -> new ArrayList<>()).add(makeBlockEntry(machine.getBlock()));
            });

            // if config is NOT enabled, add the "configurable" mineability tags to the pickaxe tag
            if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                var tagList = tagMap.computeIfAbsent(BlockTags.MINEABLE_WITH_PICKAXE.location(),
                        path -> new ArrayList<>());

                tagList.add(makeTagEntry(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH));
                tagList.add(makeTagEntry(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER));
            }
        } else if (registry == BuiltInRegistries.FLUID) {
            for (Material material : GTCEuAPI.materialManager) {
                FluidProperty property = material.getProperty(PropertyKey.FLUID);
                if (property == null) {
                    continue;
                }
                for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                    Fluid fluid = property.get(key);
                    if (fluid == null) {
                        continue;
                    }
                    ItemMaterialData.FLUID_MATERIAL.put(fluid, material);

                    TagLoader.EntryWithSource entry = makeFluidEntry(fluid);

                    ResourceLocation fluidIdTag = fluid.builtInRegistryHolder().key().location();
                    fluidIdTag = ResourceLocation.fromNamespaceAndPath("c", fluidIdTag.getPath());
                    tagMap.computeIfAbsent(fluidIdTag, path -> new ArrayList<>()).add(entry);

                    FluidState state;
                    if (fluid instanceof GTFluid gtFluid) {
                        state = gtFluid.getState();
                    } else {
                        state = key.getDefaultFluidState();
                    }
                    if (state != null) {
                        tagMap.computeIfAbsent(state.getTagKey().location(), path -> new ArrayList<>()).add(entry);
                    }

                    if (key.getExtraTag() != null) {
                        tagMap.computeIfAbsent(key.getExtraTag().location(), path -> new ArrayList<>()).add(entry);
                    }
                }
            }
        }
    }

    private static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return Collectors.toCollection(ArrayList::new);
    }

    public static TagLoader.EntryWithSource makeItemEntry(ItemLike item) {
        return makeElementEntry(item.asItem().builtInRegistryHolder().key().location());
    }

    public static TagLoader.EntryWithSource makeBlockEntry(Supplier<? extends Block> block) {
        return makeBlockEntry(block.get());
    }

    public static TagLoader.EntryWithSource makeBlockEntry(Block block) {
        return makeElementEntry(block.builtInRegistryHolder().key().location());
    }

    public static TagLoader.EntryWithSource makeFluidEntry(Fluid fluid) {
        return makeElementEntry(fluid.builtInRegistryHolder().key().location());
    }

    public static TagLoader.EntryWithSource makeElementEntry(ResourceLocation id) {
        return new TagLoader.EntryWithSource(TagEntry.element(id), GTValues.CUSTOM_TAG_SOURCE);
    }

    public static TagLoader.EntryWithSource makeTagEntry(TagKey<?> tag) {
        return new TagLoader.EntryWithSource(TagEntry.tag(tag.location()), GTValues.CUSTOM_TAG_SOURCE);
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

    public static void postKJSVeinEvents(WritableRegistry<?> registry) {
        if (!GTCEu.Mods.isKubeJSLoaded()) {
            return;
        }

        if (registry.key() == GTRegistries.ORE_VEIN_REGISTRY) {
            KJSCallWrapper.postOreVeinEvent();
        } else if (registry.key() == GTRegistries.BEDROCK_FLUID_REGISTRY) {
            KJSCallWrapper.postBedrockFluidEvent();
        } else if (registry.key() == GTRegistries.BEDROCK_ORE_REGISTRY) {
            KJSCallWrapper.postBedrockOreEvent();
        }
    }

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
        if (extensions instanceof GTClientFluidTypeExtensions gtExtensions && value.getBuilder() != null) {
            value.getBuilder().determineTextures(material, value.getKey());

            gtExtensions.setFlowingTexture(value.getBuilder().flowing());
            gtExtensions.setStillTexture(value.getBuilder().still());
        }
    }

    private static final class KJSCallWrapper {

        private static void postOreVeinEvent() {
            GTCEuServerEvents.ORE_VEIN_MODIFICATION.post(new GTOreVeinKubeEvent());
        }

        private static void postBedrockFluidEvent() {
            GTCEuServerEvents.FLUID_VEIN_MODIFICATION.post(new GTBedrockFluidVeinKubeEvent());
        }

        private static void postBedrockOreEvent() {
            GTCEuServerEvents.BEDROCK_ORE_VEIN_MODIFICATION.post(new GTBedrockOreVeinKubeEvent());
        }
    }

    public static final class ClientCallWrapper {

        public static Level getClientLevel() {
            return Minecraft.getInstance().level;
        }
    }
}

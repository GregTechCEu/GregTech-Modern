package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.util.*;
import java.util.function.Supplier;

public class DynamicTagLoader {

    private static final boolean LOG_DYNAMIC_TAG_ADDITIONS = false;
    private static final boolean CLEAN_DYNAMIC_UNIFICATION_TAGS = true;
    private static final int MAX_DYNAMIC_TAG_LOG_ENTRIES = 10;
    private static final String[] DYNAMIC_TAG_CLEANUP_PREFIX_BLACKLIST = {
            "forge:ores/",
            "forge:dyes/",
            "forge:lenses/"
    };
    private static final String[] DYNAMIC_TAG_CLEANUP_BLACKLIST = {
            "forge:stone",
            "forge:netherrack",
            "forge:obsidian",
            "forge:rods/blaze",
            "forge:water",
            "forge:lava",
            "forge:milk"
    };

    public static <T> void generateGTDynamicTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                                 Registry<T> registry) {
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> dynamicTags = new LinkedHashMap<>();
        Set<ResourceLocation> cleanableTags = new HashSet<>();

        if (registry == BuiltInRegistries.ITEM) {
            GTCEu.LOGGER.info("Injecting item tags");
            ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP.forEach((entry, itemLikes) -> {
                if (itemLikes.isEmpty()) return;
                var material = entry.material();
                if (material.isNull()) return;
                var entries = makeItemEntries(itemLikes);

                var prefixTagKeys = entry.tagPrefix().getItemTags(material);
                for (TagKey<Item> prefixTag : prefixTagKeys) {
                    addTagEntries(dynamicTags, prefixTag.location(), entries);
                    if (shouldCleanDynamicUnificationTag(prefixTag.location())) {
                        cleanableTags.add(prefixTag.location());
                    }
                }
                for (TagKey<Item> parentTag : entry.tagPrefix().getItemParentTags()) {
                    addTagEntries(dynamicTags, parentTag.location(), entries);
                }
                for (TagKey<Item> materialTag : material.getItemTags()) {
                    addTagEntries(dynamicTags, materialTag.location(), entries);
                }

                if (entry.tagPrefix() == TagPrefix.crushed && material.hasProperty(PropertyKey.ORE)) {
                    OreProperty ore = material.getProperty(PropertyKey.ORE);
                    Material washedIn = ore.getWashedIn().first();
                    if (washedIn.isNull()) return;
                    ResourceLocation generalTag = CustomTags.CHEM_BATH_WASHABLE.location();
                    ResourceLocation specificTag = generalTag.withSuffix("/" + washedIn.getName());

                    addTagEntries(dynamicTags, generalTag, entries);
                    addTagEntries(dynamicTags, specificTag, entries);
                }
            });

            GTMaterialItems.TOOL_ITEMS.rowMap().forEach((material, map) -> {
                map.values().forEach(item -> {
                    if (item == null) return;
                    var entry = makeItemEntry(item);
                    for (TagKey<Item> tag : item.get().getToolType().itemTags) {
                        addTagEntry(dynamicTags, tag.location(), entry);
                    }
                });
            });

            GTMaterialItems.ARMOR_ITEMS.rowMap().forEach((material, map) -> {
                map.forEach((type, item) -> {
                    if (item != null) {
                        var entry = new TagLoader.EntryWithSource(TagEntry.element(item.getId()),
                                GTValues.CUSTOM_TAG_SOURCE);
                        addTagEntry(dynamicTags, ItemTags.TRIMMABLE_ARMOR.location(), entry);
                        addTagEntry(dynamicTags, switch (type) {
                            case HELMET -> Tags.Items.ARMORS_HELMETS.location();
                            case CHESTPLATE -> Tags.Items.ARMORS_CHESTPLATES.location();
                            case LEGGINGS -> Tags.Items.ARMORS_LEGGINGS.location();
                            case BOOTS -> Tags.Items.ARMORS_BOOTS.location();
                        }, entry);
                    }
                });
            });

            if (GTCEu.Mods.isAE2Loaded()) {
                // If AE2 is loaded, add the Fluid P2P attunement tag to all the buckets
                var p2pFluidAttunements = new ResourceLocation(GTValues.MODID_APPENG,
                        "p2p_attunements/fluid_p2p_tunnel");
                for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    if (property == null) {
                        continue;
                    }
                    for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                        Fluid fluid = property.get(key);
                        if (fluid == null || fluid.getBucket() == Items.AIR) {
                            continue;
                        }
                        addTagEntry(dynamicTags, p2pFluidAttunements, makeItemEntry(fluid.getBucket()));
                    }
                }
            }
        } else if (registry == BuiltInRegistries.BLOCK) {
            GTCEu.LOGGER.info("Injecting block tags");
            ItemMaterialData.MATERIAL_ENTRY_BLOCK_MAP.forEach((entry, blocks) -> {
                if (blocks.isEmpty()) return;
                var material = entry.material();
                if (material.isNull()) return;

                var entries = makeBlockEntries(blocks);
                var materialTagLocations = new HashSet<ResourceLocation>();
                var materialTags = entry.tagPrefix().getBlockTags(material);
                for (TagKey<Block> materialTag : materialTags) {
                    addTagEntries(dynamicTags, materialTag.location(), entries);
                    materialTagLocations.add(materialTag.location());
                    if (shouldCleanDynamicUnificationTag(materialTag.location())) {
                        cleanableTags.add(materialTag.location());
                    }
                }
                for (TagKey<Block> materialTag : entry.tagPrefix().getAllBlockTags(material)) {
                    if (!materialTagLocations.contains(materialTag.location())) {
                        addTagEntries(dynamicTags, materialTag.location(), entries);
                    }
                }
                // Add tool tags
                if (!entry.isIgnored() && !entry.tagPrefix().miningToolTag().isEmpty()) {
                    addTagEntries(dynamicTags, CustomTags.TOOL_TIERS[material.getBlockHarvestLevel()].location(),
                            entries);
                    if (material.hasProperty(PropertyKey.WOOD)) {
                        // Wood blocks with this tag always allow a Wrench, but only allow an Axe if the config is
                        // not set. Pickaxe is never allowed (special case)
                        if (entry.tagPrefix().miningToolTag()
                                .contains(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)) {
                            addTagEntries(dynamicTags, CustomTags.MINEABLE_WITH_WRENCH.location(), entries);
                            if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                                addTagEntries(dynamicTags, BlockTags.MINEABLE_WITH_AXE.location(), entries);
                            }
                        } else {
                            // Other wood stuff should still get the Axe tag
                            addTagEntries(dynamicTags, BlockTags.MINEABLE_WITH_AXE.location(), entries);
                        }
                    } else {
                        for (var tag : entry.tagPrefix().miningToolTag()) {
                            addTagEntries(dynamicTags, tag.location(), entries);
                        }
                    }
                }

                if (entry.tagPrefix() == TagPrefix.oreEndstone) {
                    // Make endstone-based ores dragon-immune
                    addTagEntries(dynamicTags, BlockTags.DRAGON_IMMUNE.location(), entries);
                }

                if (entry.tagPrefix() == TagPrefix.frameGt) {
                    addTagEntries(dynamicTags, CustomTags.SLOW_WALKABLE_BLOCKS.location(), entries);
                }
            });

            GTRegistries.MACHINES.forEach(machine -> {
                addTagEntry(dynamicTags, CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH.location(),
                        makeBlockEntry(machine.getBlock()));
            });

            // if config is NOT enabled, add the "configurable" mineability tags to the pickaxe tag
            if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                addTagEntry(dynamicTags, BlockTags.MINEABLE_WITH_PICKAXE.location(),
                        makeTagEntry(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH));
                addTagEntry(dynamicTags, BlockTags.MINEABLE_WITH_PICKAXE.location(),
                        makeTagEntry(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER));
            }
        } else if (registry == BuiltInRegistries.FLUID) {
            GTCEu.LOGGER.info("Injecting fluid tags");
            for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
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
                    fluidIdTag = new ResourceLocation(ForgeVersion.MOD_ID, fluidIdTag.getPath());
                    addTagEntry(dynamicTags, fluidIdTag, entry);
                    if (shouldCleanDynamicUnificationTag(fluidIdTag)) {
                        cleanableTags.add(fluidIdTag);
                    }
                    FluidState state;

                    if (fluid instanceof GTFluid gtFluid) {
                        state = gtFluid.getState();
                    } else {
                        state = key.getDefaultFluidState();
                    }
                    addTagEntry(dynamicTags, state.getTagKey().location(), entry);

                    if (key.getExtraTag() != null) {
                        addTagEntry(dynamicTags, key.getExtraTag().location(), entry);
                    }
                }
            }
        }

        addCollectedTags(tagMap, dynamicTags, cleanableTags);
    }

    private static ArrayList<TagLoader.EntryWithSource> makeItemEntries(List<Supplier<? extends Item>> items) {
        var entries = new ArrayList<TagLoader.EntryWithSource>(items.size());
        for (Supplier<? extends Item> item : items) {
            entries.add(makeItemEntry(item));
        }
        return entries;
    }

    private static ArrayList<TagLoader.EntryWithSource> makeBlockEntries(List<Supplier<? extends Block>> blocks) {
        var entries = new ArrayList<TagLoader.EntryWithSource>(blocks.size());
        for (Supplier<? extends Block> block : blocks) {
            entries.add(makeBlockEntry(block));
        }
        return entries;
    }

    private static void addTagEntry(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags,
                                    ResourceLocation tag,
                                    TagLoader.EntryWithSource entry) {
        tags.computeIfAbsent(tag, path -> new ArrayList<>()).add(entry);
    }

    private static void addTagEntries(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags,
                                      ResourceLocation tag,
                                      Collection<TagLoader.EntryWithSource> entries) {
        tags.computeIfAbsent(tag, path -> new ArrayList<>(entries.size())).addAll(entries);
    }

    private static boolean shouldCleanDynamicUnificationTag(ResourceLocation tag) {
        String tagName = tag.toString();
        for (String blacklistedTag : DYNAMIC_TAG_CLEANUP_BLACKLIST) {
            if (tagName.equals(blacklistedTag)) {
                return false;
            }
        }
        for (String blacklistedPrefix : DYNAMIC_TAG_CLEANUP_PREFIX_BLACKLIST) {
            if (tagName.startsWith(blacklistedPrefix)) {
                return false;
            }
        }
        return true;
    }

    private static void addCollectedTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                         Map<ResourceLocation, List<TagLoader.EntryWithSource>> dynamicTags,
                                         Set<ResourceLocation> cleanableTags) {
        if (dynamicTags.isEmpty()) return;
        if (LOG_DYNAMIC_TAG_ADDITIONS) {
            logDynamicTagAdditions(tagMap, dynamicTags);
        }
        if (CLEAN_DYNAMIC_UNIFICATION_TAGS && !cleanableTags.isEmpty()) {
            cleanDynamicUnificationTags(tagMap, dynamicTags, cleanableTags);
        }
        dynamicTags.forEach((tag, entries) -> tagMap.computeIfAbsent(tag, path -> new ArrayList<>(entries.size()))
                .addAll(entries));
    }

    private static void cleanDynamicUnificationTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                                    Map<ResourceLocation, List<TagLoader.EntryWithSource>> dynamicTags,
                                                    Set<ResourceLocation> cleanableTags) {
        for (ResourceLocation tag : cleanableTags) {
            List<TagLoader.EntryWithSource> originalEntries = tagMap.get(tag);
            List<TagLoader.EntryWithSource> addedEntries = dynamicTags.get(tag);
            if (originalEntries == null || originalEntries.isEmpty() || addedEntries == null ||
                    addedEntries.isEmpty()) {
                continue;
            }

            var removedEntries = new ArrayList<TagLoader.EntryWithSource>();
            originalEntries.removeIf(entry -> {
                boolean remove = !GTValues.CUSTOM_TAG_SOURCE.equals(entry.source());
                if (remove) {
                    removedEntries.add(entry);
                }
                return remove;
            });

            if (!removedEntries.isEmpty()) {
                GTCEu.LOGGER.info(
                        "Cleaned GT dynamic tag {}. Removed original entries: [{}]. Added GT entries: [{}]", tag,
                        formatTagEntries(removedEntries), formatTagEntries(addedEntries));
            }
        }
    }

    private static void logDynamicTagAdditions(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                               Map<ResourceLocation, List<TagLoader.EntryWithSource>> dynamicTags) {
        dynamicTags.forEach((tag, addedEntries) -> {
            List<TagLoader.EntryWithSource> originalEntries = tagMap.get(tag);
            if (originalEntries == null || originalEntries.isEmpty()) return;

            var originalNonGtEntries = new ArrayList<TagLoader.EntryWithSource>();
            for (TagLoader.EntryWithSource entry : originalEntries) {
                if (!GTValues.CUSTOM_TAG_SOURCE.equals(entry.source())) {
                    originalNonGtEntries.add(entry);
                }
            }
            if (originalNonGtEntries.isEmpty()) return;

            GTCEu.LOGGER.info("Adding GT dynamic tag {}. Original entries: [{}]. Added entries: [{}]", tag,
                    formatTagEntries(originalNonGtEntries), formatTagEntries(addedEntries));
        });
    }

    private static String formatTagEntries(List<TagLoader.EntryWithSource> entries) {
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(entries.size(), MAX_DYNAMIC_TAG_LOG_ENTRIES);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(entries.get(i).entry());
        }
        if (entries.size() > limit) {
            builder.append(", ... ").append(entries.size() - limit).append(" more");
        }
        return builder.toString();
    }

    public static TagLoader.EntryWithSource makeItemEntry(Supplier<? extends Item> item) {
        return makeItemEntry(item.get());
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
}

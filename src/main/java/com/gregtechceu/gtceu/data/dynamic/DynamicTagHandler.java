package com.gregtechceu.gtceu.data.dynamic;

import com.gregtechceu.gtceu.GTCEu;
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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@ApiStatus.Internal
public final class DynamicTagHandler {

    private DynamicTagHandler() {}

    public static <T> void generateDynamicTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> parsedTags,
                                               Registry<T> registry) {
        long startTime = System.currentTimeMillis();

        if (registry == BuiltInRegistries.ITEM) {
            generateItemTags(parsedTags);
        } else if (registry == BuiltInRegistries.BLOCK) {
            generateBlockTags(parsedTags);
        } else if (registry == BuiltInRegistries.FLUID) {
            generateFluidTags(parsedTags);
        } else {
            // skip printing the "tag generation took ..." message if this isn't one of the registries we're
            // generating tags for
            return;
        }

        GTCEu.LOGGER.info("GregTech dynamic {} tag generation took {}ms", registry.key().location().getPath(),
                System.currentTimeMillis() - startTime);
    }

    private static void generateItemTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags) {
        ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP.forEach((entry, itemLikes) -> {
            if (itemLikes.isEmpty()) return;
            Material material = entry.material();
            if (material.isNull()) return;
            var entries = itemLikes.stream()
                    .map(Supplier::get)
                    .map(DynamicTagHandler::makeItemEntry)
                    .collect(toArrayList());

            var prefixTagKeys = entry.tagPrefix().getAllItemTags(material);
            for (TagKey<Item> prefixTag : prefixTagKeys) {
                tags.computeIfAbsent(prefixTag.location(), path -> new ArrayList<>()).addAll(entries);
            }
            for (TagKey<Item> materialTag : material.getItemTags()) {
                tags.computeIfAbsent(materialTag.location(), path -> new ArrayList<>()).addAll(entries);
            }

            if (entry.tagPrefix() == TagPrefix.crushed && material.hasProperty(PropertyKey.ORE)) {
                OreProperty ore = material.getProperty(PropertyKey.ORE);
                Material washedIn = ore.getWashedIn().first();
                if (washedIn.isNull()) return;
                ResourceLocation generalTag = CustomTags.CHEM_BATH_WASHABLE.location();
                ResourceLocation specificTag = generalTag.withSuffix("/" + washedIn.getName());

                tags.computeIfAbsent(generalTag, path -> new ArrayList<>()).addAll(entries);
                tags.computeIfAbsent(specificTag, path -> new ArrayList<>()).addAll(entries);
            }
        });

        GTMaterialItems.TOOL_ITEMS.rowMap().forEach((material, map) -> {
            map.values().forEach(item -> {
                if (item == null) return;
                var entry = makeItemEntry(item);
                for (TagKey<Item> tag : item.get().getToolType().itemTags) {
                    tags.computeIfAbsent(tag.location(), path -> new ArrayList<>()).add(entry);
                }
            });
        });

        GTMaterialItems.ARMOR_ITEMS.rowMap().forEach((material, map) -> {
            map.forEach((type, item) -> {
                if (type == null || type == ArmorItem.Type.BODY) {
                    return;
                }
                if (item != null) {
                    var entry = new TagLoader.EntryWithSource(TagEntry.element(item.getId()),
                            GTValues.CUSTOM_TAG_SOURCE);
                    tags.computeIfAbsent(ItemTags.TRIMMABLE_ARMOR.location(), $ -> new ArrayList<>())
                            .add(entry);
                    tags.computeIfAbsent(switch (type) {
                        case HELMET -> ItemTags.HEAD_ARMOR.location();
                        case CHESTPLATE -> ItemTags.CHEST_ARMOR.location();
                        case LEGGINGS -> ItemTags.LEG_ARMOR.location();
                        case BOOTS -> ItemTags.FOOT_ARMOR.location();
                        default -> throw new IllegalStateException("Unexpected value: " + type);
                    }, $ -> new ArrayList<>()).add(entry);
                }
            });
        });

        if (GTCEu.Mods.isAE2Loaded()) {
            // If AE2 is loaded, add the Fluid P2P attunement tag to all the buckets
            ResourceLocation p2pFluidAttunementsTag = ResourceLocation.fromNamespaceAndPath(GTValues.MODID_APPENG,
                    "p2p_attunements/fluid_p2p_tunnel");
            for (Material material : GTRegistries.MATERIALS) {
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
                    tags.computeIfAbsent(p2pFluidAttunementsTag, path -> new ArrayList<>()).add(entry);
                }
            }
        }
    }

    private static void generateBlockTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags) {
        ItemMaterialData.MATERIAL_ENTRY_BLOCK_MAP.forEach((entry, blocks) -> {
            if (blocks.isEmpty()) return;
            Material material = entry.material();
            if (material.isNull()) return;
            var entries = blocks.stream()
                    .map(DynamicTagHandler::makeBlockEntry)
                    .collect(toArrayList());

            var prefixTagKeys = entry.tagPrefix().getAllBlockTags(material);
            for (TagKey<Block> prefixTag : prefixTagKeys) {
                tags.computeIfAbsent(prefixTag.location(), path -> new ArrayList<>()).addAll(entries);
            }

            // Add mineability tags
            if (!entry.isIgnored() && !entry.tagPrefix().miningToolTag().isEmpty()) {
                tags.computeIfAbsent(CustomTags.TOOL_TIERS[material.getBlockHarvestLevel()].location(),
                        path -> new ArrayList<>()).addAll(entries);
                if (material.hasProperty(PropertyKey.WOOD)) {
                    // Wood blocks with this tag always allow a Wrench, but only allow an Axe if the config is
                    // not set. Pickaxe is never allowed (special case)
                    if (entry.tagPrefix().miningToolTag()
                            .contains(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)) {
                        tags.computeIfAbsent(CustomTags.MINEABLE_WITH_WRENCH.location(),
                                path -> new ArrayList<>()).addAll(entries);
                        if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
                            tags.computeIfAbsent(BlockTags.MINEABLE_WITH_AXE.location(),
                                            path -> new ArrayList<>())
                                    .addAll(entries);
                        }
                    } else {
                        // Other wood stuff should still get the Axe tag
                        tags.computeIfAbsent(BlockTags.MINEABLE_WITH_AXE.location(), path -> new ArrayList<>())
                                .addAll(entries);
                    }
                } else {
                    for (var tag : entry.tagPrefix().miningToolTag()) {
                        tags.computeIfAbsent(tag.location(), path -> new ArrayList<>()).addAll(entries);
                    }
                }
            }

            if (entry.tagPrefix() == TagPrefix.oreEndstone) {
                // Make endstone-based ores dragon-immune
                tags.computeIfAbsent(BlockTags.DRAGON_IMMUNE.location(), $ -> new ArrayList<>()).addAll(entries);
            }

            if (entry.tagPrefix() == TagPrefix.frameGt) {
                tags.computeIfAbsent(CustomTags.SLOW_WALKABLE_BLOCKS.location(), path -> new ArrayList<>())
                        .addAll(entries);
            }
        });

        GTRegistries.MACHINES.forEach(machine -> {
            tags.computeIfAbsent(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH.location(),
                    path -> new ArrayList<>()).add(makeBlockEntry(machine.getBlock()));
        });

        // if config is NOT enabled, add the "configurable" mineability tags to the pickaxe tag
        if (!ConfigHolder.INSTANCE.machines.requireGTToolsForBlocks) {
            var tagList = tags.computeIfAbsent(BlockTags.MINEABLE_WITH_PICKAXE.location(),
                    path -> new ArrayList<>());

            tagList.add(makeTagEntry(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH));
            tagList.add(makeTagEntry(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER));
        }
    }

    private static void generateFluidTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags) {
        for (Material material : GTRegistries.MATERIALS) {
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
                tags.computeIfAbsent(fluidIdTag, path -> new ArrayList<>()).add(entry);

                FluidState state;
                if (fluid instanceof GTFluid gtFluid) {
                    state = gtFluid.getState();
                } else {
                    state = key.getDefaultFluidState();
                }
                if (state != null) {
                    tags.computeIfAbsent(state.getTagKey().location(), path -> new ArrayList<>()).add(entry);
                }

                if (key.getExtraTag() != null) {
                    tags.computeIfAbsent(key.getExtraTag().location(), path -> new ArrayList<>()).add(entry);
                }
            }
        }
    }

    private static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return Collectors.toCollection(ArrayList::new);
    }

    private static TagLoader.EntryWithSource makeItemEntry(ItemLike item) {
        return makeElementEntry(item.asItem().builtInRegistryHolder().key().location());
    }

    private static TagLoader.EntryWithSource makeBlockEntry(Supplier<? extends Block> block) {
        return makeBlockEntry(block.get());
    }

    private static TagLoader.EntryWithSource makeBlockEntry(Block block) {
        return makeElementEntry(block.builtInRegistryHolder().key().location());
    }

    private static TagLoader.EntryWithSource makeFluidEntry(Fluid fluid) {
        return makeElementEntry(fluid.builtInRegistryHolder().key().location());
    }

    private static TagLoader.EntryWithSource makeElementEntry(ResourceLocation id) {
        return new TagLoader.EntryWithSource(TagEntry.element(id), GTValues.CUSTOM_TAG_SOURCE);
    }

    private static TagLoader.EntryWithSource makeTagEntry(TagKey<?> tag) {
        return new TagLoader.EntryWithSource(TagEntry.tag(tag.location()), GTValues.CUSTOM_TAG_SOURCE);
    }
}

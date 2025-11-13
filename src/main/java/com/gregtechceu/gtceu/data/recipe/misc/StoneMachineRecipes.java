package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.StoneTypeEntry;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

public class StoneMachineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        registerStoneRecipes(provider);
    }

    private static List<StoneTypeEntry> DEFAULT_ENTRIES;

    private static List<StoneTypeEntry> getDefaultEntries() {
        if (DEFAULT_ENTRIES == null) {
            final String mcModID = "minecraft";
            return DEFAULT_ENTRIES = Arrays.asList(
                    new StoneTypeEntry.Builder(mcModID, "stone")
                            .stone(Items.STONE)
                            .crackedStone(Items.COBBLESTONE)
                            .polishedStone(Items.STONE_BRICKS)
                            .slab(Items.STONE_SLAB)
                            .stair(Items.STONE_STAIRS)
                            .button(Items.STONE_BUTTON)
                            .pressurePlate(Items.STONE_PRESSURE_PLATE)
                            .material(GTMaterials.Stone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "smooth_stone")
                            .stone(Items.SMOOTH_STONE)
                            .slab(Items.SMOOTH_STONE_SLAB)
                            .material(GTMaterials.Stone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "stone_brick")
                            .stone(Items.STONE_BRICKS)
                            .crackedStone(Items.CRACKED_STONE_BRICKS)
                            .chiselStone(Items.CHISELED_STONE_BRICKS)
                            .slab(Items.STONE_BRICK_SLAB)
                            .stair(Items.STONE_BRICK_STAIRS)
                            .wall(Items.STONE_BRICK_WALL)
                            .material(GTMaterials.Stone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "andesite")
                            .stone(Items.ANDESITE)
                            .polishedStone(Items.POLISHED_ANDESITE)
                            .slab(Items.ANDESITE_SLAB)
                            .stair(Items.ANDESITE_STAIRS)
                            .wall(Items.ANDESITE_WALL)
                            .material(GTMaterials.Andesite)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "polished_andesite")
                            .stone(Items.POLISHED_ANDESITE)
                            .slab(Items.POLISHED_ANDESITE_SLAB)
                            .stair(Items.POLISHED_ANDESITE_STAIRS)
                            .material(GTMaterials.Andesite)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "granite")
                            .stone(Items.GRANITE)
                            .polishedStone(Items.POLISHED_GRANITE)
                            .slab(Items.GRANITE_SLAB)
                            .stair(Items.GRANITE_STAIRS)
                            .wall(Items.GRANITE_WALL)
                            .material(GTMaterials.Granite)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "polished_granite")
                            .stone(Items.POLISHED_GRANITE)
                            .slab(Items.POLISHED_GRANITE_SLAB)
                            .stair(Items.POLISHED_GRANITE_STAIRS)
                            .material(GTMaterials.Granite)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "diorite")
                            .stone(Items.DIORITE)
                            .polishedStone(Items.POLISHED_DIORITE)
                            .slab(Items.DIORITE_SLAB)
                            .stair(Items.DIORITE_STAIRS)
                            .wall(Items.DIORITE_WALL)
                            .material(GTMaterials.Diorite)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "polished_diorite")
                            .stone(Items.POLISHED_DIORITE)
                            .slab(Items.POLISHED_DIORITE_SLAB)
                            .stair(Items.POLISHED_DIORITE_STAIRS)
                            .material(GTMaterials.Diorite)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "sandstone")
                            .stone(Items.SANDSTONE)
                            .polishedStone(Items.CUT_SANDSTONE)
                            .chiselStone(Items.CHISELED_SANDSTONE)
                            .slab(Items.SANDSTONE_SLAB)
                            .stair(Items.SANDSTONE_STAIRS)
                            .wall(Items.SANDSTONE_WALL)
                            .material(GTMaterials.QuartzSand)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "smooth_sandstone")
                            .stone(Items.SMOOTH_SANDSTONE)
                            .slab(Items.SMOOTH_SANDSTONE_SLAB)
                            .stair(Items.SMOOTH_SANDSTONE_STAIRS)
                            .material(GTMaterials.QuartzSand)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "cut_sandstone")
                            .stone(Items.CUT_SANDSTONE)
                            .slab(Items.CUT_STANDSTONE_SLAB)
                            .material(GTMaterials.QuartzSand)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "red_sandstone")
                            .stone(Items.RED_SANDSTONE)
                            .polishedStone(Items.CUT_RED_SANDSTONE)
                            .chiselStone(Items.CHISELED_RED_SANDSTONE)
                            .slab(Items.RED_SANDSTONE_SLAB)
                            .stair(Items.RED_SANDSTONE_STAIRS)
                            .wall(Items.RED_SANDSTONE_WALL)
                            .material(GTMaterials.QuartzSand)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "smooth_red_sandstone")
                            .stone(Items.SMOOTH_RED_SANDSTONE)
                            .slab(Items.SMOOTH_RED_SANDSTONE_SLAB)
                            .stair(Items.SMOOTH_RED_SANDSTONE_STAIRS)
                            .material(GTMaterials.QuartzSand)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "cut_red_sandstone")
                            .stone(Items.CUT_RED_SANDSTONE)
                            .slab(Items.CUT_RED_SANDSTONE_SLAB)
                            .material(GTMaterials.QuartzSand)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "cobblestone")
                            .stone(Items.COBBLESTONE)
                            .slab(Items.COBBLESTONE_SLAB)
                            .stair(Items.COBBLESTONE_STAIRS)
                            .wall(Items.COBBLESTONE_WALL)
                            .material(GTMaterials.Stone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "mossy_cobblestone")
                            .stone(Items.MOSSY_COBBLESTONE)
                            .slab(Items.MOSSY_COBBLESTONE_SLAB)
                            .stair(Items.MOSSY_COBBLESTONE_STAIRS)
                            .wall(Items.MOSSY_COBBLESTONE_WALL)
                            .material(GTMaterials.Stone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "mossy_stone_brick")
                            .stone(Items.MOSSY_STONE_BRICKS)
                            .slab(Items.MOSSY_STONE_BRICK_SLAB)
                            .stair(Items.MOSSY_STONE_BRICK_STAIRS)
                            .wall(Items.MOSSY_STONE_BRICK_WALL)
                            .material(GTMaterials.Stone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "blackstone")
                            .stone(Items.BLACKSTONE)
                            .polishedStone(Items.POLISHED_BLACKSTONE)
                            .slab(Items.BLACKSTONE_SLAB)
                            .stair(Items.BLACKSTONE_STAIRS)
                            .wall(Items.BLACKSTONE_WALL)
                            .material(GTMaterials.Blackstone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "polished_blackstone")
                            .stone(Items.POLISHED_BLACKSTONE)
                            .polishedStone(Items.POLISHED_BLACKSTONE_BRICKS)
                            .chiselStone(Items.CHISELED_POLISHED_BLACKSTONE)
                            .crackedStone(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                            .slab(Items.POLISHED_BLACKSTONE_SLAB)
                            .stair(Items.POLISHED_BLACKSTONE_STAIRS)
                            .wall(Items.POLISHED_BLACKSTONE_WALL)
                            .pressurePlate(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
                            .button(Items.POLISHED_BLACKSTONE_BUTTON)
                            .material(GTMaterials.Blackstone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "polished_blackstone_brick")
                            .stone(Items.POLISHED_BLACKSTONE_BRICKS)
                            .slab(Items.POLISHED_BLACKSTONE_BRICK_SLAB)
                            .stair(Items.POLISHED_BLACKSTONE_BRICK_STAIRS)
                            .wall(Items.POLISHED_BLACKSTONE_BRICK_WALL)
                            .material(GTMaterials.Blackstone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "brick")
                            .stone(Items.BRICKS)
                            .slab(Items.BRICK_SLAB)
                            .stair(Items.BRICK_STAIRS)
                            .wall(Items.BRICK_WALL)
                            .material(GTMaterials.Brick, 4 * GTValues.M)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "mud_brick")
                            .stone(Items.MUD_BRICKS)
                            .slab(Items.MUD_BRICK_SLAB)
                            .stair(Items.MUD_BRICK_STAIRS)
                            .wall(Items.MUD_BRICK_WALL)
                            .material(GTMaterials.Clay) // maybe?
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "nether_bricks")
                            .stone(Items.NETHER_BRICKS)
                            .crackedStone(Items.CRACKED_NETHER_BRICKS)
                            .chiselStone(Items.CHISELED_NETHER_BRICKS)
                            .slab(Items.NETHER_BRICK_SLAB)
                            .stair(Items.NETHER_BRICK_STAIRS)
                            .wall(Items.NETHER_BRICK_WALL)
                            .material(GTMaterials.Netherrack, 4 * GTValues.M)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "red_nether_brick")
                            .stone(Items.RED_NETHER_BRICKS)
                            .slab(Items.RED_NETHER_BRICK_SLAB)
                            .stair(Items.RED_NETHER_BRICK_STAIRS)
                            .wall(Items.RED_NETHER_BRICK_WALL)
                            .material(GTMaterials.Netherrack)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "quartz")
                            .stone(Items.QUARTZ_BLOCK)
                            .polishedStone(Items.QUARTZ_BRICKS)
                            .chiselStone(Items.CHISELED_QUARTZ_BLOCK)
                            // .slab(Items.QUARTZ_SLAB) TODO: Fix plate conflict
                            .stair(Items.QUARTZ_STAIRS)
                            .material(GTMaterials.NetherQuartz, 4 * GTValues.M)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "smooth_quartz")
                            .stone(Items.SMOOTH_QUARTZ)
                            .slab(Items.SMOOTH_QUARTZ_SLAB)
                            .stair(Items.SMOOTH_QUARTZ_STAIRS)
                            .material(GTMaterials.NetherQuartz, 4 * GTValues.M)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "cut_copper")
                            .stone(Items.CUT_COPPER)
                            .slab(Items.CUT_COPPER_SLAB)
                            .stair(Items.CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "exposed_cut_copper")
                            .stone(Items.EXPOSED_CUT_COPPER)
                            .slab(Items.EXPOSED_CUT_COPPER_SLAB)
                            .stair(Items.EXPOSED_CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "oxidized_cut_copper")
                            .stone(Items.OXIDIZED_CUT_COPPER)
                            .slab(Items.OXIDIZED_CUT_COPPER_SLAB)
                            .stair(Items.OXIDIZED_CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "weathered_cut_copper")
                            .stone(Items.WEATHERED_CUT_COPPER)
                            .slab(Items.WEATHERED_CUT_COPPER_SLAB)
                            .stair(Items.WEATHERED_CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "waxed_cut_copper")
                            .stone(Items.WAXED_CUT_COPPER)
                            .slab(Items.WAXED_CUT_COPPER_SLAB)
                            .stair(Items.WAXED_CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "waxed_exposed_cut_copper")
                            .stone(Items.WAXED_EXPOSED_CUT_COPPER)
                            .slab(Items.WAXED_EXPOSED_CUT_COPPER_SLAB)
                            .stair(Items.WAXED_EXPOSED_CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "waxed_oxidized_cut_copper")
                            .stone(Items.WAXED_OXIDIZED_CUT_COPPER)
                            .slab(Items.WAXED_OXIDIZED_CUT_COPPER_SLAB)
                            .stair(Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "waxed_weathered_cut_copper")
                            .stone(Items.WAXED_WEATHERED_CUT_COPPER)
                            .slab(Items.WAXED_WEATHERED_CUT_COPPER_SLAB)
                            .stair(Items.WAXED_WEATHERED_CUT_COPPER_STAIRS)
                            .material(GTMaterials.Copper, 9 * GTValues.M / 4)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "purpur")
                            .stone(Items.PURPUR_BLOCK)
                            .chiselStone(Items.PURPUR_PILLAR)
                            .slab(Items.PURPUR_SLAB)
                            .stair(Items.PURPUR_STAIRS)
                            // .material() // TODO purpur material?
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "end_stone")
                            .stone(Items.END_STONE)
                            .polishedStone(Items.END_STONE_BRICKS)
                            .slab(Items.END_STONE_BRICK_SLAB)
                            .stair(Items.END_STONE_BRICK_STAIRS)
                            .wall(Items.END_STONE_BRICK_WALL)
                            .material(GTMaterials.Endstone)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "prismarine")
                            .stone(Items.PRISMARINE)
                            .slab(Items.PRISMARINE_SLAB)
                            .stair(Items.PRISMARINE_STAIRS)
                            .wall(Items.PRISMARINE_WALL)
                            // .material() // TODO prismarine material?
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "dark_prismarine")
                            .stone(Items.DARK_PRISMARINE)
                            .slab(Items.DARK_PRISMARINE_SLAB)
                            .stair(Items.DARK_PRISMARINE_STAIRS)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "prismarine_brick")
                            .stone(Items.PRISMARINE_BRICKS)
                            .slab(Items.PRISMARINE_BRICK_SLAB)
                            .stair(Items.PRISMARINE_BRICK_STAIRS)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "bamboo_mosaic")
                            .stone(Items.BAMBOO_MOSAIC)
                            .slab(Items.BAMBOO_MOSAIC_SLAB)
                            .stair(Items.BAMBOO_MOSAIC_STAIRS)
                            .material(GTMaterials.Wood)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "deepslate")
                            .stone(Items.DEEPSLATE)
                            .crackedStone(Items.COBBLED_DEEPSLATE)
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "cobbled_deepslate")
                            .stone(Items.COBBLED_DEEPSLATE)
                            .polishedStone(Items.POLISHED_DEEPSLATE)
                            .chiselStone(Items.CHISELED_DEEPSLATE)
                            .slab(Items.COBBLED_DEEPSLATE_SLAB)
                            .stair(Items.COBBLED_DEEPSLATE_STAIRS)
                            .wall(Items.COBBLED_DEEPSLATE_WALL)
                            .material(GTMaterials.Deepslate)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "polished_deepslate")
                            .stone(Items.POLISHED_DEEPSLATE)
                            .polishedStone(Items.DEEPSLATE_BRICKS)
                            .slab(Items.POLISHED_DEEPSLATE_SLAB)
                            .stair(Items.POLISHED_DEEPSLATE_STAIRS)
                            .wall(Items.POLISHED_DEEPSLATE_WALL)
                            .material(GTMaterials.Deepslate)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "deepslate_bricks")
                            .stone(Items.DEEPSLATE_BRICKS)
                            .polishedStone(Items.DEEPSLATE_TILES)
                            .crackedStone(Items.CRACKED_DEEPSLATE_BRICKS)
                            .slab(Items.DEEPSLATE_BRICK_SLAB)
                            .stair(Items.DEEPSLATE_BRICK_STAIRS)
                            .wall(Items.DEEPSLATE_BRICK_WALL)
                            .material(GTMaterials.Deepslate)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "deepslate_tile")
                            .stone(Items.DEEPSLATE_TILES)
                            .crackedStone(Items.CRACKED_DEEPSLATE_TILES)
                            .slab(Items.DEEPSLATE_TILE_SLAB)
                            .stair(Items.DEEPSLATE_TILE_STAIRS)
                            .wall(Items.DEEPSLATE_TILE_WALL)
                            .material(GTMaterials.Deepslate)
                            .registerAllMaterialInfo()
                            .build(),
                    new StoneTypeEntry.Builder(mcModID, "basalt")
                            .stone(Items.BASALT)
                            .polishedStone(Items.POLISHED_BASALT)
                            .material(GTMaterials.Basalt)
                            .registerAllMaterialInfo()
                            .build()

            );
        }
        return DEFAULT_ENTRIES;
    }

    public static void registerMaterialInfo() {
        for (StoneTypeEntry entry : getDefaultEntries()) {
            registerStoneMaterialInfo(entry);
        }
    }

    public static void registerStoneMaterialInfo(@NotNull StoneTypeEntry entry) {
        if (!entry.material.isNull() && entry.stone != null) {
            if (entry.addStoneOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.stone, TagPrefix.block, entry.material);
            }
            if (entry.addStoneMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.stone,
                        new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount)));
            }
        }

        if (!entry.material.isNull() && entry.polishedStone != null) {
            if (entry.addStoneOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.polishedStone, TagPrefix.block, entry.material);
            }
            if (entry.addPolishedStoneMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.polishedStone,
                        new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount)));
            }
        }

        if (!entry.material.isNull() && entry.smeltStone != null) {
            if (entry.addStoneOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.smeltStone, TagPrefix.block, entry.material);
            }
            if (entry.addSmeltStoneMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.smeltStone,
                        new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount)));
            }
        }

        if (!entry.material.isNull() && entry.chiselStone != null) {
            if (entry.addStoneOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.chiselStone, TagPrefix.block, entry.material);
            }
            if (entry.addChiselStoneMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.chiselStone,
                        new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount)));
            }
        }

        if (!entry.material.isNull() && entry.crackedStone != null) {
            if (entry.addStoneOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.crackedStone, TagPrefix.block, entry.material);
            }
            if (entry.addCrackedStoneMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.crackedStone,
                        new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount)));
            }
        }

        if (!entry.material.isNull() && entry.slab != null) {
            if (entry.addSlabOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.slab, TagPrefix.slab, entry.material);
            }
            if (entry.addSlabMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.slab,
                        new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount / 2)));
            }
        }

        if (!entry.material.isNull() && entry.stair != null) {
            if (entry.addStairOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.stair, TagPrefix.stairs, entry.material);
            }
            if (entry.addStairMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.stair,
                        new ItemMaterialInfo(new MaterialStack(entry.material, (3 * entry.materialAmount) / 4)));
            }
        }

        if (!entry.material.isNull() && entry.wall != null) {
            if (entry.addWallOreDict) {
                ItemMaterialData.registerMaterialEntry(entry.wall, TagPrefix.fence, entry.material);
            }
            if (entry.addWallMaterialInfo) {
                ItemMaterialData.registerMaterialInfo(entry.wall,
                        new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount)));
            }
        }

        if (!entry.material.isNull() && entry.pressurePlate != null && entry.addPressurePlateMaterialInfo) {
            ItemMaterialData.registerMaterialInfo(entry.pressurePlate,
                    new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount / 4)));
        }

        if (!entry.material.isNull() && entry.button != null && entry.addButtonMaterialInfo) {
            ItemMaterialData.registerMaterialInfo(entry.button,
                    new ItemMaterialInfo(new MaterialStack(entry.material, entry.materialAmount / 6)));
        }
    }

    private static void registerStoneRecipes(Consumer<FinishedRecipe> provider) {
        for (StoneTypeEntry entry : getDefaultEntries()) {
            registerStoneTypeRecipes(provider, entry);
        }
    }

    public static void registerStoneTypeRecipes(Consumer<FinishedRecipe> provider, @NotNull StoneTypeEntry entry) {
        if (entry.stone == null) {
            GTCEu.LOGGER.error("Could not find stone form of StoneTypeEntry, id: {}", entry.stoneName);
            return;
        }

        if (entry.polishedStone != null) {
            if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, entry.stoneName + "_polish_hammer",
                        new ItemStack(entry.polishedStone, 4),
                        "hSS", " SS",
                        'S', entry.stone);
            }

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("assemble_" + entry.stoneName + "_into_polished")
                    .inputItems(entry.stone)
                    .circuitMeta(4)
                    .outputItems(entry.polishedStone)
                    .duration(80)
                    .EUt(1)
                    .save(provider);
        }

        if (entry.crackedStone != null) {
            if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, entry.stoneName + "_hammer",
                        new ItemStack(entry.crackedStone),
                        "h", "S",
                        'S', entry.stone);
            }

            GTRecipeTypes.FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + entry.stoneName + "_into_cracked")
                    .inputItems(entry.stone)
                    .outputItems(entry.crackedStone)
                    .duration(12).EUt(4).save(provider);
        }

        if (entry.smeltStone != null) {
            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + entry.stoneName + "_into_" + entry.smeltStone,
                    entry.stone, entry.smeltStone, 0.1f);
        }

        if (entry.slab != null) {
            if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, entry.stoneName + "_slab_saw", new ItemStack(entry.slab),
                        "sS",
                        'S', entry.stone);
            }

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("cut_" + entry.stoneName + "_into_slab")
                    .inputItems(entry.stone)
                    .outputItems(entry.slab, 2)
                    .duration(40)
                    .EUt(8)
                    .addMaterialInfo(true)
                    .save(provider);

            if (entry.chiselStone != null) {
                if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) {
                    VanillaRecipeHelper.addShapedRecipe(provider, entry.stoneName + "_polished_hammer",
                            new ItemStack(entry.chiselStone),
                            "mSd", " S ",
                            'S', entry.slab);
                }
                GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("form_" + entry.stoneName + "_slab_into_pillar")
                        .inputItems(entry.slab, 2)
                        .outputItems(entry.chiselStone)
                        .duration(80)
                        .EUt(8)
                        .save(provider);
            }
        }

        if (entry.pressurePlate != null) {

            if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes && entry.slab != null) {
                VanillaRecipeHelper.addShapedRecipe(provider, entry.stoneName + "_pressure_plate",
                        new ItemStack(entry.pressurePlate, 2), "ShS", "LCL", "SdS",
                        'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                        'L', entry.slab,
                        'C', new MaterialEntry(TagPrefix.spring, GTMaterials.Iron));

                ASSEMBLER_RECIPES.recipeBuilder(entry.stoneName + "_pressure_plate")
                        .inputItems(TagPrefix.spring, GTMaterials.Iron)
                        .inputItems(entry.stone, 2)
                        .outputItems(entry.pressurePlate, 2)
                        .duration(100)
                        .EUt(VA[ULV])
                        .save(provider);
            } else if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) {

            }
        }

        if (entry.button != null) {
            if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes && entry.pressurePlate != null) {
                VanillaRecipeHelper.addShapedRecipe(provider, "stone_button", new ItemStack(entry.button, 6), "sP",
                        'P', entry.pressurePlate);
            }

            if (entry.pressurePlate != null) {
                GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("cut_" + entry.stoneName + "slab_into_button")
                        .inputItems(entry.pressurePlate)
                        .outputItems(entry.button, 3)
                        .duration(60)
                        .EUt(8)
                        .save(provider);
            } else {
                GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("cut_" + entry.stoneName + "_into_button")
                        .inputItems(entry.stone)
                        .notConsumable(GTItems.SHAPE_MOLD_NUGGET)
                        .outputItems(entry.button, 6)
                        .duration(60)
                        .EUt(8)
                        .save(provider);
            }
        }

        if (entry.stair != null) {
            if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, entry.stoneName + "_stair_saw",
                        new ItemStack(entry.stair, 3),
                        "Ss ", "SS ", "SSS",
                        'S', entry.stone);
            }

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("assemble_" + entry.stoneName + "_into_stair")
                    .inputItems(entry.stone, 3)
                    .circuitMeta(7)
                    .outputItems(entry.stair, 4)
                    .duration(80)
                    .EUt(8)
                    .addMaterialInfo(true)
                    .save(provider);
        }

        if (entry.wall != null) {
            if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, entry.stoneName + "_wall_saw",
                        new ItemStack(entry.wall, 2),
                        "sS", " S", " S",
                        'S', entry.stone);
            }
            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("assemble_" + entry.stoneName + "_into_wall")
                    .inputItems(entry.stone)
                    .circuitMeta(13)
                    .outputItems(entry.wall)
                    .duration(100)
                    .EUt(8)
                    .addMaterialInfo(true)
                    .save(provider);
        }
    }
}

package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;

import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.BIO_CHAFF;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class WoodMachineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        registerGTWoodRecipes(provider);
        registerWoodRecipes(provider);
        registerPyrolyseOvenRecipes(provider);
    }

    private static List<WoodTypeEntry> DEFAULT_ENTRIES;

    private static List<WoodTypeEntry> getDefaultEntries() {
        if (DEFAULT_ENTRIES == null) {
            final String mcModId = "minecraft";
            return DEFAULT_ENTRIES = Arrays.asList(
                    new WoodTypeEntry.Builder(mcModId, "oak")
                            .planks(Items.OAK_PLANKS, "oak_planks")
                            .log(Items.OAK_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_OAK_LOG)
                            .wood(Items.OAK_WOOD)
                            .strippedWood(Items.STRIPPED_OAK_WOOD)
                            .door(Items.OAK_DOOR, "oak_door")
                            .trapdoor(Items.OAK_TRAPDOOR, "oak_trapdoor")
                            .slab(Items.OAK_SLAB, "oak_slab")
                            .fence(Items.OAK_FENCE, "oak_fence")
                            .fenceGate(Items.OAK_FENCE_GATE, "oak_fence_gate")
                            .stairs(Items.OAK_STAIRS, "oak_stairs")
                            .boat(Items.OAK_BOAT, "oak_boat")
                            .chestBoat(Items.OAK_CHEST_BOAT, "oak_chest_boat")
                            .sign(Items.OAK_SIGN, "oak_sign")
                            .hangingSign(Items.OAK_HANGING_SIGN, "oak_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "spruce")
                            .planks(Items.SPRUCE_PLANKS, "spruce_planks")
                            .log(Items.SPRUCE_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_SPRUCE_LOG)
                            .wood(Items.SPRUCE_WOOD)
                            .strippedWood(Items.STRIPPED_SPRUCE_WOOD)
                            .door(Items.SPRUCE_DOOR, "spruce_door")
                            .trapdoor(Items.SPRUCE_TRAPDOOR, "spruce_trapdoor")
                            .slab(Items.SPRUCE_SLAB, "spruce_slab")
                            .fence(Items.SPRUCE_FENCE, "spruce_fence")
                            .fenceGate(Items.SPRUCE_FENCE_GATE, "spruce_fence_gate")
                            .stairs(Items.SPRUCE_STAIRS, "spruce_stairs")
                            .boat(Items.SPRUCE_BOAT, "spruce_boat")
                            .chestBoat(Items.SPRUCE_CHEST_BOAT, "spruce_chest_boat")
                            .sign(Items.SPRUCE_SIGN, "spruce_sign")
                            .hangingSign(Items.SPRUCE_HANGING_SIGN, "spruce_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "birch")
                            .planks(Items.BIRCH_PLANKS, "birch_planks")
                            .log(Items.BIRCH_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_BIRCH_LOG)
                            .wood(Items.BIRCH_WOOD)
                            .strippedWood(Items.STRIPPED_BIRCH_WOOD)
                            .door(Items.BIRCH_DOOR, "birch_door")
                            .trapdoor(Items.BIRCH_TRAPDOOR, "birch_trapdoor")
                            .slab(Items.BIRCH_SLAB, "birch_slab")
                            .fence(Items.BIRCH_FENCE, "birch_fence")
                            .fenceGate(Items.BIRCH_FENCE_GATE, "birch_fence_gate")
                            .stairs(Items.BIRCH_STAIRS, "birch_stairs")
                            .boat(Items.BIRCH_BOAT, "birch_boat")
                            .chestBoat(Items.BIRCH_CHEST_BOAT, "birch_chest_boat")
                            .sign(Items.BIRCH_SIGN, "birch_sign")
                            .hangingSign(Items.BIRCH_HANGING_SIGN, "birch_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "jungle")
                            .planks(Items.JUNGLE_PLANKS, "jungle_planks")
                            .log(Items.JUNGLE_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_JUNGLE_LOG)
                            .wood(Items.JUNGLE_WOOD)
                            .strippedWood(Items.STRIPPED_JUNGLE_WOOD)
                            .door(Items.JUNGLE_DOOR, "jungle_door")
                            .trapdoor(Items.JUNGLE_TRAPDOOR, "jungle_trapdoor")
                            .slab(Items.JUNGLE_SLAB, "jungle_slab")
                            .fence(Items.JUNGLE_FENCE, "jungle_fence")
                            .fenceGate(Items.JUNGLE_FENCE_GATE, "jungle_fence_gate")
                            .stairs(Items.JUNGLE_STAIRS, "jungle_stairs")
                            .boat(Items.JUNGLE_BOAT, "jungle_boat")
                            .chestBoat(Items.JUNGLE_CHEST_BOAT, "jungle_chest_boat")
                            .sign(Items.JUNGLE_SIGN, "jungle_sign")
                            .hangingSign(Items.JUNGLE_HANGING_SIGN, "jungle_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "acacia")
                            .planks(Items.ACACIA_PLANKS, "acacia_planks")
                            .log(Items.ACACIA_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_ACACIA_LOG)
                            .wood(Items.ACACIA_WOOD)
                            .strippedWood(Items.STRIPPED_ACACIA_WOOD)
                            .door(Items.ACACIA_DOOR, "acacia_door")
                            .trapdoor(Items.ACACIA_TRAPDOOR, "acacia_trapdoor")
                            .slab(Items.ACACIA_SLAB, "acacia_slab")
                            .fence(Items.ACACIA_FENCE, "acacia_fence")
                            .fenceGate(Items.ACACIA_FENCE_GATE, "acacia_fence_gate")
                            .stairs(Items.ACACIA_STAIRS, "acacia_stairs")
                            .boat(Items.ACACIA_BOAT, "acacia_boat")
                            .chestBoat(Items.ACACIA_CHEST_BOAT, "acacia_chest_boat")
                            .sign(Items.ACACIA_SIGN, "acacia_sign")
                            .hangingSign(Items.ACACIA_HANGING_SIGN, "acacia_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "dark_oak")
                            .planks(Items.DARK_OAK_PLANKS, "dark_oak_planks")
                            .log(Items.DARK_OAK_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_DARK_OAK_LOG)
                            .wood(Items.DARK_OAK_WOOD)
                            .strippedWood(Items.STRIPPED_DARK_OAK_WOOD)
                            .door(Items.DARK_OAK_DOOR, "dark_oak_door")
                            .trapdoor(Items.DARK_OAK_TRAPDOOR, "dark_oak_trapdoor")
                            .slab(Items.DARK_OAK_SLAB, "dark_oak_slab")
                            .fence(Items.DARK_OAK_FENCE, "dark_oak_fence")
                            .fenceGate(Items.DARK_OAK_FENCE_GATE, "dark_oak_fence_gate")
                            .stairs(Items.DARK_OAK_STAIRS, "dark_oak_stairs")
                            .boat(Items.DARK_OAK_BOAT, "dark_oak_boat")
                            .chestBoat(Items.DARK_OAK_CHEST_BOAT, "dark_oak_chest_boat")
                            .sign(Items.DARK_OAK_SIGN, "dark_oak_sign")
                            .hangingSign(Items.DARK_OAK_HANGING_SIGN, "dark_oak_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "bamboo")
                            .planks(Items.BAMBOO_PLANKS, "bamboo_planks")
                            .logTag(TagUtil.createItemTag("bamboo_blocks", true))
                            .log(Items.BAMBOO_BLOCK).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_BAMBOO_BLOCK)
                            .door(Items.BAMBOO_DOOR, "bamboo_door")
                            .trapdoor(Items.BAMBOO_TRAPDOOR, "bamboo_trapdoor")
                            .slab(Items.BAMBOO_SLAB, "bamboo_slab")
                            .fence(Items.BAMBOO_FENCE, "bamboo_fence")
                            .fenceGate(Items.BAMBOO_FENCE_GATE, "bamboo_fence_gate")
                            .stairs(Items.BAMBOO_STAIRS, "bamboo_stairs")
                            .boat(Items.BAMBOO_RAFT, "bamboo_raft")
                            .chestBoat(Items.BAMBOO_CHEST_RAFT, "bamboo_chest_raft")
                            .sign(Items.BAMBOO_SIGN, "bamboo_sign")
                            .hangingSign(Items.BAMBOO_HANGING_SIGN, "bamboo_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "cherry")
                            .planks(Items.CHERRY_PLANKS, "cherry_planks")
                            .log(Items.CHERRY_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_CHERRY_LOG)
                            .wood(Items.CHERRY_WOOD)
                            .strippedWood(Items.STRIPPED_CHERRY_WOOD)
                            .door(Items.CHERRY_DOOR, "cherry_door")
                            .trapdoor(Items.CHERRY_TRAPDOOR, "cherry_trapdoor")
                            .slab(Items.CHERRY_SLAB, "cherry_slab")
                            .fence(Items.CHERRY_FENCE, "cherry_fence")
                            .fenceGate(Items.CHERRY_FENCE_GATE, "cherry_fence_gate")
                            .stairs(Items.CHERRY_STAIRS, "cherry_stairs")
                            .boat(Items.CHERRY_BOAT, "cherry_boat")
                            .chestBoat(Items.CHERRY_CHEST_BOAT, "cherry_chest_boat")
                            .sign(Items.CHERRY_SIGN, "cherry_sign")
                            .hangingSign(Items.CHERRY_HANGING_SIGN, "cherry_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "mangrove")
                            .planks(Items.MANGROVE_PLANKS, "mangrove_planks")
                            .log(Items.MANGROVE_LOG).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_MANGROVE_LOG)
                            .wood(Items.MANGROVE_WOOD)
                            .strippedWood(Items.STRIPPED_MANGROVE_WOOD)
                            .door(Items.MANGROVE_DOOR, "mangrove_door")
                            .trapdoor(Items.MANGROVE_TRAPDOOR, "mangrove_trapdoor")
                            .slab(Items.MANGROVE_SLAB, "mangrove_slab")
                            .fence(Items.MANGROVE_FENCE, "mangrove_fence")
                            .fenceGate(Items.MANGROVE_FENCE_GATE, "mangrove_fence_gate")
                            .stairs(Items.MANGROVE_STAIRS, "mangrove_stairs")
                            .boat(Items.MANGROVE_BOAT, "mangrove_boat")
                            .chestBoat(Items.MANGROVE_CHEST_BOAT, "mangrove_chest_boat")
                            .sign(Items.MANGROVE_SIGN, "mangrove_sign")
                            .hangingSign(Items.MANGROVE_HANGING_SIGN, "mangrove_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "crimson")
                            .planks(Items.CRIMSON_PLANKS, "crimson_planks")
                            .logTag(TagUtil.createItemTag("crimson_stems", true))
                            .log(Items.CRIMSON_STEM).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_CRIMSON_STEM)
                            .wood(Items.CRIMSON_HYPHAE)
                            .strippedWood(Items.STRIPPED_CRIMSON_HYPHAE)
                            .door(Items.CRIMSON_DOOR, "crimson_door")
                            .trapdoor(Items.CRIMSON_TRAPDOOR, "crimson_trapdoor")
                            .slab(Items.CRIMSON_SLAB, "crimson_slab")
                            .fence(Items.CRIMSON_FENCE, "crimson_fence")
                            .fenceGate(Items.CRIMSON_FENCE_GATE, "crimson_fence_gate")
                            .stairs(Items.CRIMSON_STAIRS, "crimson_stairs")
                            .sign(Items.CRIMSON_SIGN, "crimson_sign")
                            .hangingSign(Items.CRIMSON_HANGING_SIGN, "crimson_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(mcModId, "warped")
                            .planks(Items.WARPED_PLANKS, "warped_planks")
                            .logTag(TagUtil.createItemTag("warped_stems", true))
                            .log(Items.WARPED_STEM).removeCharcoalRecipe()
                            .strippedLog(Items.STRIPPED_WARPED_STEM)
                            .wood(Items.WARPED_HYPHAE)
                            .strippedWood(Items.STRIPPED_WARPED_HYPHAE)
                            .door(Items.WARPED_DOOR, "warped_door")
                            .trapdoor(Items.WARPED_TRAPDOOR, "warped_trapdoor")
                            .slab(Items.WARPED_SLAB, "warped_slab")
                            .fence(Items.WARPED_FENCE, "warped_fence")
                            .fenceGate(Items.WARPED_FENCE_GATE, "warped_fence_gate")
                            .stairs(Items.WARPED_STAIRS, "warped_stairs")
                            .sign(Items.WARPED_SIGN, "warped_sign")
                            .hangingSign(Items.WARPED_HANGING_SIGN, "warped_hanging_sign")
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(GTCEu.MOD_ID, "rubber")
                            .planks(GTBlocks.RUBBER_PLANK.asItem(), null)
                            .log(GTBlocks.RUBBER_LOG.asItem()).addCharcoalRecipe()
                            .strippedLog(GTBlocks.STRIPPED_RUBBER_LOG.asItem())
                            .wood(GTBlocks.RUBBER_WOOD.asItem())
                            .strippedWood(GTBlocks.STRIPPED_RUBBER_WOOD.asItem())
                            .door(GTBlocks.RUBBER_DOOR.asItem(), null)
                            .trapdoor(GTBlocks.RUBBER_TRAPDOOR.asItem(), "rubber_trapdoor")
                            .slab(GTBlocks.RUBBER_SLAB.asItem(), null).addSlabRecipe()
                            .fence(GTBlocks.RUBBER_FENCE.asItem(), null)
                            .fenceGate(GTBlocks.RUBBER_FENCE_GATE.asItem(), null)
                            .stairs(GTBlocks.RUBBER_STAIRS.asItem(), null).addStairsRecipe()
                            .boat(GTItems.RUBBER_BOAT.asItem(), null)
                            .chestBoat(GTItems.RUBBER_CHEST_BOAT.asItem(), null)
                            .sign(GTBlocks.RUBBER_SIGN.asItem(), null)
                            .hangingSign(GTBlocks.RUBBER_HANGING_SIGN.asItem(), null)
                            .registerAllTags()
                            .registerAllUnificationInfo()
                            .build(),
                    new WoodTypeEntry.Builder(GTCEu.MOD_ID, "treated")
                            .planks(GTBlocks.TREATED_WOOD_PLANK.asItem(), null)
                            .door(GTBlocks.TREATED_WOOD_DOOR.asItem(), null)
                            .trapdoor(GTBlocks.TREATED_WOOD_TRAPDOOR.asItem(), null)
                            .slab(GTBlocks.TREATED_WOOD_SLAB.asItem(), null).addSlabRecipe()
                            .fence(GTBlocks.TREATED_WOOD_FENCE.asItem(), null)
                            .fenceGate(GTBlocks.TREATED_WOOD_FENCE_GATE.asItem(), null)
                            .stairs(GTBlocks.TREATED_WOOD_STAIRS.asItem(), null).addStairsRecipe()
                            .boat(GTItems.TREATED_WOOD_BOAT.asItem(), null)
                            .chestBoat(GTItems.TREATED_WOOD_CHEST_BOAT.asItem(), null)
                            .sign(GTBlocks.TREATED_WOOD_SIGN.asItem(), null)
                            .hangingSign(GTBlocks.TREATED_WOOD_HANGING_SIGN.asItem(), null)
                            .material(TreatedWood)
                            .generateLogToPlankRecipe(false)
                            .registerUnificationInfo(false, true, true, true, true, true, true, true)
                            .build());
        }
        return DEFAULT_ENTRIES;
    }

    public static void registerUnificationInfo() {
        for (WoodTypeEntry entry : getDefaultEntries()) {
            registerWoodUnificationInfo(entry);
        }
    }

    /**
     * Standardized processing for wood types
     */
    private static void registerWoodRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.nerfWoodCrafting) {
            VanillaRecipeHelper.addShapedRecipe(provider, "stick_saw", new ItemStack(Items.STICK, 4), "s", "P", "P",
                    'P', ItemTags.PLANKS);
            VanillaRecipeHelper.addShapedRecipe(provider, "stick_normal", new ItemStack(Items.STICK, 2), "P", "P", 'P',
                    ItemTags.PLANKS);
        }

        for (WoodTypeEntry entry : getDefaultEntries()) {
            registerWoodTypeRecipe(provider, entry);
        }
    }

    /**
     * Adds all standard unification info and ore dictionary for a wood type
     *
     * @param entry the entry to register for
     */
    public static void registerWoodUnificationInfo(@NotNull WoodTypeEntry entry) {
        for (var log_ : entry.getLogs()) {
            if (log_ != null && entry.addLogOreDict) {
                ChemicalHelper.registerUnificationItems(log, entry.material, log_);
            }
        }

        if (entry.addPlanksOreDict) {
            ChemicalHelper.registerUnificationItems(planks, entry.material, entry.planks);
        }
        if (entry.addPlanksUnificationInfo) {
            ChemicalHelper.registerMaterialInfo(entry.planks,
                    new ItemMaterialInfo(new MaterialStack(entry.material, M)));
        }

        if (entry.door != null) {
            if (entry.addDoorsOreDict) {
                ChemicalHelper.registerUnificationItems(door, entry.material, entry.door);
            }
            if (entry.addDoorsUnificationInfo) {
                ChemicalHelper.registerMaterialInfo(entry.door, ConfigHolder.INSTANCE.recipes.hardWoodRecipes ?
                        new ItemMaterialInfo(new MaterialStack(entry.material, M * 2),
                                new MaterialStack(GTMaterials.Iron, M / 9)) : // screw
                        new ItemMaterialInfo(new MaterialStack(entry.material, M * 2)));
            }
        }

        if (entry.slab != null) {
            if (entry.addSlabsOreDict) {
                ChemicalHelper.registerUnificationItems(slab, entry.material, entry.slab);
            }
            if (entry.addSlabsUnificationInfo) {
                ChemicalHelper.registerMaterialInfo(entry.slab,
                        new ItemMaterialInfo(new MaterialStack(entry.material, M / 2)));
            }
        }

        if (entry.fence != null) {
            if (entry.addFencesOreDict) {
                ChemicalHelper.registerUnificationItems(fence, entry.material, entry.fence);
            }
            if (entry.addFencesUnificationInfo) {
                ChemicalHelper.registerMaterialInfo(entry.fence,
                        new ItemMaterialInfo(new MaterialStack(entry.material, M)));
            }
        }

        if (entry.fenceGate != null) {
            if (entry.addFenceGatesOreDict) {
                ChemicalHelper.registerUnificationItems(fenceGate, entry.material, entry.fenceGate);
            }
            if (entry.addFenceGatesUnificationInfo) {
                ChemicalHelper.registerMaterialInfo(entry.fenceGate,
                        new ItemMaterialInfo(new MaterialStack(entry.material, M * 3)));
            }
        }

        if (entry.stairs != null) {
            if (entry.addStairsOreDict) {
                ChemicalHelper.registerUnificationItems(stairs, entry.material, entry.stairs);
            }
            if (entry.addStairsUnificationInfo) {
                ChemicalHelper.registerMaterialInfo(entry.stairs,
                        new ItemMaterialInfo(new MaterialStack(entry.material, (3 * M) / 2)));
            }
        }

        if (entry.boat != null && entry.addBoatsUnificationInfo) {
            ChemicalHelper.registerMaterialInfo(entry.boat,
                    new ItemMaterialInfo(new MaterialStack(entry.material, M * 5)));
        }

        /*
         * TODO should this really be included?
         * if (entry.chestBoat != null && entry.addChestBoatsUnificationInfo) {
         * ChemicalHelper.registerMaterialInfo(entry.chestBoat,
         * new ItemMaterialInfo(new MaterialStack(entry.material, M * 13)));
         * }
         */
    }

    /**
     * Adds all standard recipes for a wood type
     *
     * @param entry the entry to register for
     */
    public static void registerWoodTypeRecipe(Consumer<FinishedRecipe> provider, @NotNull WoodTypeEntry entry) {
        final String name = entry.woodName;
        TagKey<Item> logTag = entry.logTag;
        boolean hasPlanksRecipe = entry.planksRecipeName != null;

        // noinspection ConstantValue can be null if someone does an oopsie and doesn't set it.
        if (entry.planks == null) {
            throw new IllegalStateException("Could not find planks form of WoodTypeEntry '" + name + "'.");
        }

        if (entry.generateLogToPlankRecipe) {
            if (ConfigHolder.INSTANCE.recipes.nerfWoodCrafting) {
                VanillaRecipeHelper.addShapelessRecipe(provider,
                        hasPlanksRecipe ? entry.planksRecipeName : name + "_planks",
                        new ItemStack(entry.planks, 2), logTag);
            } else if (!hasPlanksRecipe) {
                VanillaRecipeHelper.addShapelessRecipe(provider, name + "_planks",
                        new ItemStack(entry.planks, 4), logTag);
            }

            // log -> plank saw crafting
            VanillaRecipeHelper.addShapedRecipe(provider, name + "_planks_saw",
                    new ItemStack(entry.planks, ConfigHolder.INSTANCE.recipes.nerfWoodCrafting ? 4 : 6),
                    "s", "L", 'L', logTag);

            // log -> plank cutting
            CUTTER_RECIPES.recipeBuilder(name + "_planks")
                    .inputItems(logTag)
                    .outputItems(new ItemStack(entry.planks, 6))
                    .outputItems(dust, Wood, 2)
                    .duration(200)
                    .EUt(VA[ULV])
                    .save(provider);
        }

        // door
        if (entry.door != null) {
            final boolean hasDoorRecipe = entry.doorRecipeName != null;
            if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
                String recipeName = hasDoorRecipe ? entry.doorRecipeName : name + "_door";
                if (entry.trapdoor != null) {
                    VanillaRecipeHelper.addShapedRecipe(provider, recipeName, new ItemStack(entry.door),
                            "PTd", "PRS", "PPs",
                            'P', entry.planks,
                            'T', entry.trapdoor,
                            'R', new UnificationEntry(ring, Iron),
                            'S', new UnificationEntry(screw, Iron));

                    // plank -> door assembling
                    ASSEMBLER_RECIPES.recipeBuilder(name + "_door")
                            .inputItems(entry.trapdoor)
                            .inputItems(new ItemStack(entry.planks, 4))
                            .inputFluids(Iron.getFluid(GTValues.L / 9))
                            .outputItems(entry.door)
                            .duration(400).EUt(4).save(provider);
                } else {
                    VanillaRecipeHelper.addShapedRecipe(provider, recipeName, new ItemStack(entry.door),
                            "PTd", "PRS", "PPs",
                            'P', entry.planks,
                            'T', ItemTags.WOODEN_TRAPDOORS,
                            'R', new UnificationEntry(ring, Iron),
                            'S', new UnificationEntry(screw, Iron));

                    // plank -> door assembling
                    ASSEMBLER_RECIPES.recipeBuilder(name + "_door")
                            .inputItems(ItemTags.WOODEN_TRAPDOORS)
                            .inputItems(new ItemStack(entry.planks, 4))
                            .inputFluids(Iron.getFluid(GTValues.L / 9))
                            .outputItems(entry.door)
                            .duration(400).EUt(4).save(provider);
                }
            } else {
                if (!hasDoorRecipe) {
                    VanillaRecipeHelper.addShapedRecipe(provider, name + "_door", new ItemStack(entry.door, 3),
                            "PP", "PP", "PP",
                            'P', entry.planks);
                }

                ASSEMBLER_RECIPES.recipeBuilder(name + "_door")
                        .inputItems(new ItemStack(entry.planks, 6))
                        .outputItems(new ItemStack(entry.door, 3))
                        .circuitMeta(6)
                        .duration(600).EUt(4)
                        .save(provider);
            }
        }

        // sign
        if (entry.sign != null && entry.slab != null) {
            final boolean hasSignRecipe = entry.signRecipeName != null;
            String recipeName = hasSignRecipe ? entry.signRecipeName : name + "_sign";
            if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, recipeName + "_iron", new ItemStack(entry.sign),
                        "LLL", "rPr", "sSd",
                        'P', entry.planks,
                        'r', new UnificationEntry(screw, Iron),
                        'L', entry.slab,
                        'S', entry.getStick());

                // plank -> sign assembling
                ASSEMBLER_RECIPES.recipeBuilder(recipeName + "_iron")
                        .circuitMeta(4)
                        .inputItems(new ItemStack(entry.slab, 1))
                        .inputItems(entry.getStick(), 1)
                        .inputFluids(Iron.getFluid(GTValues.L / 9))
                        .outputItems(entry.sign, 3)
                        .duration(200).EUt(4).save(provider);

                VanillaRecipeHelper.addShapedRecipe(provider, recipeName + "_steel", new ItemStack(entry.sign, 2),
                        "LLL", "rPr", "sSd",
                        'P', entry.planks,
                        'r', new UnificationEntry(screw, Steel),
                        'L', entry.slab,
                        'S', entry.getStick());

                // plank -> sign assembling
                ASSEMBLER_RECIPES.recipeBuilder(recipeName + "_steel")
                        .circuitMeta(4)
                        .inputItems(new ItemStack(entry.slab, 1))
                        .inputItems(entry.getStick(), 1)
                        .inputFluids(Steel.getFluid(GTValues.L / 9))
                        .outputItems(entry.sign, 5)
                        .duration(200).EUt(4).save(provider);
            } else {
                VanillaRecipeHelper.addShapedRecipe(provider, recipeName, new ItemStack(entry.sign, 3),
                        "PPP", "PPP", " s ",
                        'P', entry.planks,
                        's', entry.getStick());
            }

            if (entry.hangingSign != null) {
                final boolean hasHangingSignRecipe = entry.hangingSignRecipeName != null;
                String recipeNameHanging = hasHangingSignRecipe ? entry.hangingSignRecipeName : name + "_hanging_sign";
                ASSEMBLER_RECIPES.recipeBuilder(recipeNameHanging)
                        .inputItems(entry.sign)
                        .inputItems(ring, Iron, 2)
                        .inputItems(Items.CHAIN, 2)
                        .circuitMeta(5)
                        .duration(150)
                        .EUt(4)
                        .save(provider);
            }
        }

        // trapdoor
        if (entry.trapdoor != null) {
            final boolean hasTrapdoorRecipe = entry.trapdoorRecipeName != null;
            String recipeName = hasTrapdoorRecipe ? entry.trapdoorRecipeName : name + "_trapdoor";
            if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, recipeName + "_iron", new ItemStack(entry.trapdoor),
                        "bPS", "PdP", "SPb",
                        'P', entry.planks,
                        'b', new UnificationEntry(bolt, Iron),
                        'S', entry.getStick());

                // plank -> trapdoor assembling
                ASSEMBLER_RECIPES.recipeBuilder(recipeName + "_iron")
                        .circuitMeta(3)
                        .inputItems(new ItemStack(entry.planks, 2))
                        .inputFluids(Iron.getFluid(GTValues.L / 9))
                        .outputItems(entry.trapdoor)
                        .duration(200).EUt(4).save(provider);

                VanillaRecipeHelper.addShapedRecipe(provider, recipeName + "_steel", new ItemStack(entry.trapdoor, 2),
                        "bPS", "PdP", "SPb",
                        'P', entry.planks,
                        'b', new UnificationEntry(bolt, Steel),
                        'S', entry.getStick());

                // plank -> trapdoor assembling
                ASSEMBLER_RECIPES.recipeBuilder(recipeName + "_steel")
                        .circuitMeta(3)
                        .inputItems(new ItemStack(entry.planks, 2))
                        .inputFluids(Steel.getFluid(GTValues.L / 9))
                        .outputItems(entry.trapdoor, 2)
                        .duration(200).EUt(4).save(provider);
            } else {
                VanillaRecipeHelper.addShapedRecipe(provider, recipeName, new ItemStack(entry.trapdoor, 2),
                        "PPP", "PPP",
                        'P', entry.planks);
            }
        }

        // stairs
        if (entry.stairs != null) {
            final boolean hasStairRecipe = entry.stairsRecipeName != null;
            if (entry.addStairsCraftingRecipe) {
                VanillaRecipeHelper.addShapedRecipe(provider,
                        hasStairRecipe ? entry.stairsRecipeName : name + "_stairs",
                        new ItemStack(entry.stairs, 4),
                        "P  ", "PP ", "PPP",
                        'P', entry.planks);
            }

            // plank -> stairs assembling
            ASSEMBLER_RECIPES.recipeBuilder(name + "_stairs")
                    .inputItems(new ItemStack(entry.planks, 6))
                    .outputItems(new ItemStack(entry.stairs, 4))
                    .circuitMeta(7)
                    .EUt(1).duration(100).save(provider);
        }

        // slab
        if (entry.slab != null) {
            if (entry.addSlabCraftingRecipe && !ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
                VanillaRecipeHelper.addShapedRecipe(provider, name + "_slab", new ItemStack(entry.slab, 6),
                        "PPP", 'P', entry.planks);
            }

            // plank -> slab crafting
            VanillaRecipeHelper.addShapedRecipe(provider, name + "_slab_saw", new ItemStack(entry.slab, 2),
                    "sS", 'S', entry.planks);

            // plank -> slab cutting
            CUTTER_RECIPES.recipeBuilder(name + "_slab")
                    .inputItems(entry.planks)
                    .outputItems(new ItemStack(entry.slab, 2))
                    .duration(200).EUt(VA[ULV])
                    .save(provider);
        }

        // fence
        if (entry.fence != null) {
            final boolean hasFenceRecipe = entry.fenceRecipeName != null;
            if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {

                VanillaRecipeHelper.addShapedRecipe(provider, hasFenceRecipe ? entry.fenceRecipeName : name + "_fence",
                        new ItemStack(entry.fence),
                        "PSP", "PSP", "PSP",
                        'P', entry.planks,
                        'S', entry.getStick());
            } else {
                if (!hasFenceRecipe) {
                    VanillaRecipeHelper.addShapedRecipe(provider, name + "_fence", new ItemStack(entry.fence, 3),
                            "PSP", "PSP",
                            'P', entry.planks,
                            'S', entry.getStick());
                }
            }

            // plank -> fence assembling
            ASSEMBLER_RECIPES.recipeBuilder(name + "_fence")
                    .inputItems(entry.planks)
                    .outputItems(entry.fence)
                    .circuitMeta(13)
                    .duration(100).EUt(4)
                    .save(provider);
        }

        // fence gate
        if (entry.fenceGate != null) {
            final boolean hasFenceGateRecipe = entry.fenceGateRecipeName != null;
            if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {

                VanillaRecipeHelper.addShapedRecipe(provider,
                        hasFenceGateRecipe ? entry.fenceGateRecipeName : name + "_fence_gate",
                        new ItemStack(entry.fenceGate),
                        "F F", "SPS", "SPS",
                        'P', entry.planks,
                        'S', entry.getStick(),
                        'F', Items.FLINT);

                VanillaRecipeHelper.addShapedRecipe(provider, name + "_fence_gate_screws",
                        new ItemStack(entry.fenceGate, 2),
                        "IdI", "SPS", "SPS",
                        'P', entry.planks,
                        'S', entry.getStick(),
                        'I', new UnificationEntry(screw, Iron));
            } else {
                if (!hasFenceGateRecipe) {
                    VanillaRecipeHelper.addShapedRecipe(provider, name + "_fence_gate", new ItemStack(entry.fenceGate),
                            "SPS", "SPS",
                            'P', entry.planks,
                            'S', entry.getStick());
                }
            }

            // plank -> fence gate assembling
            ASSEMBLER_RECIPES.recipeBuilder(name + "_fence_gate")
                    .inputItems(new ItemStack(entry.planks, 2))
                    .inputItems(Tags.Items.RODS_WOODEN, 2)
                    .outputItems(entry.fenceGate)
                    .circuitMeta(2)
                    .duration(100).EUt(4).save(provider);
        }

        // boat
        if (entry.boat != null) {
            final boolean hasBoatRecipe = entry.boatRecipeName != null;
            if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
                if (entry.slab != null) {

                    VanillaRecipeHelper.addShapedRecipe(provider, hasBoatRecipe ? entry.boatRecipeName : name + "_boat",
                            new ItemStack(entry.boat),
                            "PHP", "PkP", "SSS",
                            'P', entry.planks,
                            'S', entry.slab,
                            'H', ItemTags.SHOVELS);
                }
            } else {
                if (!hasBoatRecipe) {
                    VanillaRecipeHelper.addShapedRecipe(provider, name + "_boat", new ItemStack(entry.boat),
                            "P P", "PPP",
                            'P', entry.planks);
                }
            }

            // plank -> boat assembling
            ASSEMBLER_RECIPES.recipeBuilder(name + "_boat")
                    .inputItems(new ItemStack(entry.planks, 5))
                    .outputItems(entry.boat)
                    .circuitMeta(15)
                    .duration(100).EUt(4).save(provider);

            // chest boat
            if (entry.chestBoat != null) {
                final boolean hasChestBoatRecipe = entry.chestBoatRecipeName != null;
                String recipeName = hasChestBoatRecipe ? entry.chestBoatRecipeName : name + "_chest_boat";
                if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
                    VanillaRecipeHelper.addShapedRecipe(provider, recipeName,
                            new ItemStack(entry.chestBoat),
                            " B ", "SCS", " w ",
                            'B', entry.boat,
                            'S', new UnificationEntry(bolt, Wood),
                            'C', Tags.Items.CHESTS_WOODEN);
                } else {
                    VanillaRecipeHelper.addShapelessRecipe(provider, recipeName,
                            new ItemStack(entry.chestBoat),
                            entry.boat, Tags.Items.CHESTS_WOODEN);
                }

                // boat -> chest boat assembling
                ASSEMBLER_RECIPES.recipeBuilder(name + "_chest_boat")
                        .inputItems(new ItemStack(entry.boat))
                        .inputItems(Tags.Items.CHESTS_WOODEN)
                        .outputItems(entry.chestBoat)
                        .circuitMeta(16)
                        .duration(100).EUt(4).save(provider);
            }
        }
    }

    /**
     * Standard recipes for GT woods
     */
    private static void registerGTWoodRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_planks",
                GTBlocks.TREATED_WOOD_PLANK.asStack(8),
                "PPP", "PBP", "PPP",
                'P', ItemTags.PLANKS,
                'B', FluidUtil.getFilledBucket(FluidHelperImpl.toFluidStack(Creosote.getFluid(1000))));

        VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_stick",
                ChemicalHelper.get(rod, TreatedWood, ConfigHolder.INSTANCE.recipes.nerfWoodCrafting ? 2 : 4),
                "L", "L",
                'L', GTBlocks.TREATED_WOOD_PLANK.asItem());
        if (ConfigHolder.INSTANCE.recipes.nerfWoodCrafting) {
            VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_stick_saw",
                    ChemicalHelper.get(rod, TreatedWood, 4),
                    "s", "L", "L",
                    'L', GTBlocks.TREATED_WOOD_PLANK.asItem());
        }

        VanillaRecipeHelper.addShapelessRecipe(provider, "rubber_button", GTBlocks.RUBBER_BUTTON.asStack(),
                GTBlocks.RUBBER_PLANK.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "treated_wood_button", GTBlocks.TREATED_WOOD_BUTTON.asStack(),
                GTBlocks.TREATED_WOOD_PLANK.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "rubber_pressure_plate", GTBlocks.RUBBER_PRESSURE_PLATE.asStack(),
                "aa", 'a', GTBlocks.RUBBER_PLANK.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_plate",
                GTBlocks.TREATED_WOOD_PRESSURE_PLATE.asStack(), "aa", 'a', GTBlocks.TREATED_WOOD_PLANK.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "rubber_wood",
                GTBlocks.RUBBER_WOOD.asStack(3),
                "LL", "LL", 'L', GTBlocks.RUBBER_LOG.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "stripped_rubber_wood",
                GTBlocks.STRIPPED_RUBBER_WOOD.asStack(3),
                "LL", "LL", 'L', GTBlocks.STRIPPED_RUBBER_LOG.asStack());
    }

    public static void hardWoodRecipes(Consumer<ResourceLocation> registry) {
        if (ConfigHolder.INSTANCE.recipes.nerfWoodCrafting) {
            registry.accept(new ResourceLocation("minecraft:stick"));
        }
        for (WoodTypeEntry entry : getDefaultEntries()) {
            hardWoodTypeRecipes(registry, entry);
        }
    }

    private static void hardWoodTypeRecipes(Consumer<ResourceLocation> registry, @NotNull WoodTypeEntry entry) {
        if (ConfigHolder.INSTANCE.recipes.nerfWoodCrafting) {
            if (entry.planksRecipeName != null) {
                registry.accept(new ResourceLocation(entry.modid, entry.planksRecipeName));
            }
        }

        if (ConfigHolder.INSTANCE.recipes.harderCharcoalRecipe) {
            if (entry.removeCharcoalRecipe) {
                registry.accept(new ResourceLocation("charcoal"));
            }
        }

        if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
            if (entry.door != null) {
                // hard plank -> door crafting
                if (entry.doorRecipeName != null) {
                    registry.accept(new ResourceLocation(entry.modid, entry.doorRecipeName));
                }
            }
            if (entry.slab != null) {
                if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes && entry.slabRecipeName != null) {
                    registry.accept(new ResourceLocation(entry.modid, entry.slabRecipeName));
                }
                if (entry.boat != null) {
                    // hard plank -> boat crafting
                    if (entry.boatRecipeName != null) {
                        registry.accept(new ResourceLocation(entry.modid, entry.boatRecipeName));
                    }
                }
            }
            if (entry.fence != null) {
                // hard plank -> fence crafting
                if (entry.fenceRecipeName != null) {
                    registry.accept(new ResourceLocation(entry.modid, entry.fenceRecipeName));
                }
            }
            if (entry.fenceGate != null) {
                // hard plank -> fence gate crafting
                if (entry.fenceGateRecipeName != null) {
                    registry.accept(new ResourceLocation(entry.modid, entry.fenceGateRecipeName));
                }
            }
        }
    }

    private static void registerPyrolyseOvenRecipes(Consumer<FinishedRecipe> provider) {
        // Logs ================================================

        // Charcoal Byproducts
        PYROLYSE_RECIPES.recipeBuilder("log_to_charcoal_byproducts").circuitMeta(4)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(CharcoalByproducts.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);

        // Wood Tar
        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_tar").circuitMeta(9)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(WoodTar.getFluid(1500))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_tar_nitrogen").circuitMeta(10)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(WoodTar.getFluid(1500))
                .duration(320).EUt(96)
                .save(provider);

        // Wood Gas
        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_gas").circuitMeta(5)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(WoodGas.getFluid(1500))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_gas_nitrogen").circuitMeta(6)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(WoodGas.getFluid(1500))
                .duration(320).EUt(96)
                .save(provider);

        // Wood Vinegar
        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_vinegar").circuitMeta(7)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(WoodVinegar.getFluid(3000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_vinegar_nitrogen").circuitMeta(8)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(WoodVinegar.getFluid(3000))
                .duration(320).EUt(96)
                .save(provider);

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder("log_to_creosote").circuitMeta(1)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(Creosote.getFluid(4000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_creosote_nitrogen").circuitMeta(2)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(Creosote.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);

        // Heavy Oil
        PYROLYSE_RECIPES.recipeBuilder("log_to_heavy_oil").circuitMeta(3)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(dust, Ash, 4)
                .outputFluids(OilHeavy.getFluid(200))
                .duration(320).EUt(192)
                .save(provider);

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder("coal_to_coke_creosote").circuitMeta(1)
                .inputItems(gem, Coal, 16)
                .outputItems(gem, Coke, 16)
                .outputFluids(Creosote.getFluid(8000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_to_coke_creosote_nitrogen").circuitMeta(2)
                .inputItems(gem, Coal, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(gem, Coke, 16)
                .outputFluids(Creosote.getFluid(8000))
                .duration(320).EUt(96)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_block_to_coke_creosote").circuitMeta(1)
                .inputItems(block, Coal, 8)
                .outputItems(block, Coke, 8)
                .outputFluids(Creosote.getFluid(32000))
                .duration(2560).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_block_to_coke_creosote_nitrogen").circuitMeta(2)
                .inputItems(block, Coal, 8)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(block, Coke, 8)
                .outputFluids(Creosote.getFluid(32000))
                .duration(1280).EUt(96)
                .save(provider);

        // Biomass
        PYROLYSE_RECIPES.recipeBuilder("bio_chaff_to_fermented_biomass").EUt(10).duration(200)
                .inputItems(BIO_CHAFF)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(1500))
                .outputFluids(FermentedBiomass.getFluid(1500))
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("bio_chaff_to_biomass").EUt(10).duration(900)
                .inputItems(BIO_CHAFF, 4)
                .circuitMeta(1)
                .inputFluids(Water.getFluid(4000))
                .outputFluids(Biomass.getFluid(5000))
                .save(provider);

        // Sugar to Charcoal
        PYROLYSE_RECIPES.recipeBuilder("sugar_to_charcoal").circuitMeta(1)
                .inputItems(dust, Sugar, 23)
                .outputItems(dust, Charcoal, 12)
                .outputFluids(Water.getFluid(1500))
                .duration(320).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("sugar_to_charcoal_nitrogen").circuitMeta(2)
                .inputItems(dust, Sugar, 23)
                .inputFluids(Nitrogen.getFluid(500))
                .outputItems(dust, Charcoal, 12)
                .outputFluids(Water.getFluid(1500))
                .duration(160).EUt(96)
                .save(provider);

        // COAL GAS ============================================

        // From Log
        PYROLYSE_RECIPES.recipeBuilder("log_to_coal_gas").circuitMeta(20)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Steam.getFluid(1000))
                .outputItems(Items.CHARCOAL, 20)
                .outputFluids(CoalGas.getFluid(2000))
                .duration(640).EUt(64)
                .save(provider);

        // From Coal
        PYROLYSE_RECIPES.recipeBuilder("coal_to_coal_gas").circuitMeta(22)
                .inputItems(gem, Coal, 16)
                .inputFluids(Steam.getFluid(1000))
                .outputItems(gem, Coke, 16)
                .outputFluids(CoalGas.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_block_to_coal_gas").circuitMeta(22)
                .inputItems(block, Coal, 8)
                .inputFluids(Steam.getFluid(4000))
                .outputItems(block, Coke, 8)
                .outputFluids(CoalGas.getFluid(16000))
                .duration(1280).EUt(96)
                .save(provider);

        // COAL TAR ============================================
        PYROLYSE_RECIPES.recipeBuilder("charcoal_to_coal_tar").circuitMeta(8)
                .inputItems(Items.CHARCOAL, 32)
                .chancedOutput(dust, Ash, 5000, 0)
                .outputFluids(CoalTar.getFluid(1000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_to_coal_tar").circuitMeta(8)
                .inputItems(Items.COAL, 12)
                .chancedOutput(dust, DarkAsh, 5000, 0)
                .outputFluids(CoalTar.getFluid(3000))
                .duration(320).EUt(96)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coke_to_coal_tar").circuitMeta(8)
                .inputItems(gem, Coke, 8)
                .chancedOutput(dust, Ash, 7500, 0)
                .outputFluids(CoalTar.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);
    }
}

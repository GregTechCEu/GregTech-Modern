package com.gregtechceu.gtceu.data.recipe.misc.compat;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import quek.undergarden.registry.UGBlocks;
import quek.undergarden.registry.UGItems;

import java.util.ArrayList;
import java.util.function.Consumer;

public class UndergardenRecipes {
    /*
     * 
     * TODO: add cloggrum material first?
     * 
     * private static void hardIronRecipes(Consumer<FinishedRecipe> provider) {
     * VanillaRecipeHelper.addShapedRecipe(provider, "iron_bars", new ItemStack(UGBlocks.CLOGGRUM_BARS.get(), 8), " h ",
     * "XXX",
     * "XXX",
     * 'X', new MaterialEntry(TagPrefix.rod, GTMaterials.Iron)); // CHANGE IRON TO CLOGGRUM
     * }
     */

    public static void hardRedstoneRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "compat/undergarden/sticky_piston_from_goo_ball",
                new ItemStack(Blocks.STICKY_PISTON), "h",
                "R", "P",
                'R', new ItemStack(UGItems.GOO_BALL.get()),
                'P', new ItemStack(Blocks.PISTON));
    }

    private static ArrayList<WoodTypeEntry> DEFAULT_ENTRIES;

    public static ArrayList<WoodTypeEntry> woodMachineRecipes() {
        final String undergardenModId = GTValues.MODID_UNDERGARDEN;
        if (DEFAULT_ENTRIES == null) {
            DEFAULT_ENTRIES = new ArrayList<WoodTypeEntry>();
            DEFAULT_ENTRIES.add(new WoodTypeEntry.Builder(undergardenModId, "smogstem")
                    .planks(UGBlocks.SMOGSTEM_PLANKS.get().asItem(), "smogstem_planks")
                    .log(UGBlocks.SMOGSTEM_LOG.get().asItem()).removeCharcoalRecipe()
                    .strippedLog(UGBlocks.STRIPPED_SMOGSTEM_LOG.get().asItem())
                    .wood(UGBlocks.SMOGSTEM_WOOD.get().asItem())
                    .strippedWood(UGBlocks.STRIPPED_SMOGSTEM_WOOD.get().asItem())
                    .door(UGBlocks.SMOGSTEM_DOOR.get().asItem(), "smogstem_door")
                    .trapdoor(UGBlocks.SMOGSTEM_TRAPDOOR.get().asItem(), "smogstem_trapdoor")
                    .slab(UGBlocks.SMOGSTEM_SLAB.get().asItem(), "smogstem_slab")
                    .fence(UGBlocks.SMOGSTEM_FENCE.get().asItem(), "smogstem_fence")
                    .fenceGate(UGBlocks.SMOGSTEM_FENCE_GATE.get().asItem(), "smogstem_fence_gate")
                    .stairs(UGBlocks.SMOGSTEM_STAIRS.get().asItem(), "smogstem_stairs")
                    .boat(UGItems.SMOGSTEM_BOAT.get(), "smogstem_boat")
                    .chestBoat(UGItems.SMOGSTEM_CHEST_BOAT.get(), "smogstem_chest_boat")
                    .sign(UGBlocks.SMOGSTEM_SIGN.get().asItem(), "smogstem_sign")
                    .hangingSign(UGBlocks.SMOGSTEM_HANGING_SIGN.get().asItem(), "smogstem_hanging_sign")
                    .button(UGBlocks.SMOGSTEM_BUTTON.get().asItem(), "smogstem_button")
                    .pressurePlate(UGBlocks.SMOGSTEM_PRESSURE_PLATE.get().asItem(), "smogstem_pressure_plate")
                    .registerAllMaterialInfo()
                    .build());
            DEFAULT_ENTRIES.add(new WoodTypeEntry.Builder(undergardenModId, "wigglewood")
                    .planks(UGBlocks.WIGGLEWOOD_PLANKS.get().asItem(), "wigglewood_planks")
                    .log(UGBlocks.WIGGLEWOOD_LOG.get().asItem()).removeCharcoalRecipe()
                    .strippedLog(UGBlocks.STRIPPED_WIGGLEWOOD_LOG.get().asItem())
                    .wood(UGBlocks.WIGGLEWOOD_WOOD.get().asItem())
                    .strippedWood(UGBlocks.STRIPPED_WIGGLEWOOD_WOOD.get().asItem())
                    .door(UGBlocks.WIGGLEWOOD_DOOR.get().asItem(), "wigglewood_door")
                    .trapdoor(UGBlocks.WIGGLEWOOD_TRAPDOOR.get().asItem(), "wigglewood_trapdoor")
                    .slab(UGBlocks.WIGGLEWOOD_SLAB.get().asItem(), "wigglewood_slab")
                    .fence(UGBlocks.WIGGLEWOOD_FENCE.get().asItem(), "wigglewood_fence")
                    .fenceGate(UGBlocks.WIGGLEWOOD_FENCE_GATE.get().asItem(), "wigglewood_fence_gate")
                    .stairs(UGBlocks.WIGGLEWOOD_STAIRS.get().asItem(), "wigglewood_stairs")
                    .boat(UGItems.WIGGLEWOOD_BOAT.get(), "wigglewood_boat")
                    .chestBoat(UGItems.WIGGLEWOOD_CHEST_BOAT.get(), "wigglewood_chest_boat")
                    .sign(UGBlocks.WIGGLEWOOD_SIGN.get().asItem(), "wigglewood_sign")
                    .hangingSign(UGBlocks.WIGGLEWOOD_HANGING_SIGN.get().asItem(), "wigglewood_hanging_sign")
                    .button(UGBlocks.WIGGLEWOOD_BUTTON.get().asItem(), "wigglewood_button")
                    .pressurePlate(UGBlocks.WIGGLEWOOD_PRESSURE_PLATE.get().asItem(), "wigglewood_pressure_plate")
                    .registerAllMaterialInfo()
                    .build());
            DEFAULT_ENTRIES.add(new WoodTypeEntry.Builder(undergardenModId, "grongle")
                    .planks(UGBlocks.GRONGLE_PLANKS.get().asItem(), "grongle_planks")
                    .log(UGBlocks.GRONGLE_LOG.get().asItem()).removeCharcoalRecipe()
                    .strippedLog(UGBlocks.STRIPPED_GRONGLE_LOG.get().asItem())
                    .wood(UGBlocks.GRONGLE_WOOD.get().asItem())
                    .strippedWood(UGBlocks.STRIPPED_GRONGLE_WOOD.get().asItem())
                    .door(UGBlocks.GRONGLE_DOOR.get().asItem(), "grongle_door")
                    .trapdoor(UGBlocks.GRONGLE_TRAPDOOR.get().asItem(), "grongle_trapdoor")
                    .slab(UGBlocks.GRONGLE_SLAB.get().asItem(), "grongle_slab")
                    .fence(UGBlocks.GRONGLE_FENCE.get().asItem(), "grongle_fence")
                    .fenceGate(UGBlocks.GRONGLE_FENCE_GATE.get().asItem(), "grongle_fence_gate")
                    .stairs(UGBlocks.GRONGLE_STAIRS.get().asItem(), "grongle_stairs")
                    .boat(UGItems.GRONGLE_BOAT.get(), "grongle_boat")
                    .chestBoat(UGItems.GRONGLE_CHEST_BOAT.get(), "grongle_chest_boat")
                    .sign(UGBlocks.GRONGLE_SIGN.get().asItem(), "grongle_sign")
                    .hangingSign(UGBlocks.GRONGLE_HANGING_SIGN.get().asItem(), "grongle_hanging_sign")
                    .button(UGBlocks.GRONGLE_BUTTON.get().asItem(), "grongle_button")
                    .pressurePlate(UGBlocks.GRONGLE_PRESSURE_PLATE.get().asItem(), "grongle_pressure_plate")
                    .registerAllMaterialInfo()
                    .build());
        }
        return DEFAULT_ENTRIES;
    }
}

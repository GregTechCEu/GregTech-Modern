package com.gregtechceu.gtceu.data.recipe.misc.compat;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;

import java.util.ArrayList;

public class AlexsCavesRecipes {

    private static ArrayList<WoodTypeEntry> DEFAULT_ENTRIES;

    public static ArrayList<WoodTypeEntry> woodMachineRecipes() {
        final String alexsCavesModId = GTValues.MODID_ALEXSCAVES;
        if (DEFAULT_ENTRIES == null) {
            DEFAULT_ENTRIES = new ArrayList<WoodTypeEntry>();
            DEFAULT_ENTRIES.add(new WoodTypeEntry.Builder(alexsCavesModId, "pewen")
                    .planks(ACBlockRegistry.PEWEN_PLANKS.get().asItem(), "pewen_planks")
                    .log(ACBlockRegistry.PEWEN_LOG.get().asItem()).removeCharcoalRecipe()
                    .strippedLog(ACBlockRegistry.STRIPPED_PEWEN_LOG.get().asItem())
                    .wood(ACBlockRegistry.PEWEN_WOOD.get().asItem())
                    .strippedWood(ACBlockRegistry.STRIPPED_PEWEN_WOOD.get().asItem())
                    .door(ACItemRegistry.PEWEN_DOOR.get(), "pewen_door")
                    .trapdoor(ACBlockRegistry.PEWEN_TRAPDOOR.get().asItem(), "pewen_trapdoor")
                    .slab(ACBlockRegistry.PEWEN_PLANKS_SLAB.get().asItem(), "pewen_slab")
                    .fence(ACBlockRegistry.PEWEN_PLANKS_FENCE.get().asItem(), "pewen_fence")
                    .fenceGate(ACBlockRegistry.PEWEN_FENCE_GATE.get().asItem(), "pewen_fence_gate")
                    .stairs(ACBlockRegistry.PEWEN_PLANKS_STAIRS.get().asItem(), "pewen_stairs")
                    .boat(ACItemRegistry.PEWEN_BOAT.get(), "pewen_boat")
                    .chestBoat(ACItemRegistry.PEWEN_CHEST_BOAT.get(), "pewen_chest_boat")
                    .sign(ACItemRegistry.PEWEN_SIGN.get(), "pewen_sign")
                    .hangingSign(ACItemRegistry.PEWEN_HANGING_SIGN.get(), "pewen_hanging_sign")
                    .button(ACBlockRegistry.PEWEN_BUTTON.get().asItem(), "pewen_button")
                    .pressurePlate(ACBlockRegistry.PEWEN_PRESSURE_PLATE.get().asItem(), "pewen_pressure_plate")
                    .registerAllMaterialInfo()
                    .build());
            DEFAULT_ENTRIES.add(new WoodTypeEntry.Builder(alexsCavesModId, "thornwood")
                    .planks(ACBlockRegistry.THORNWOOD_PLANKS.get().asItem(), "thornwood_planks")
                    .log(ACBlockRegistry.THORNWOOD_LOG.get().asItem()).removeCharcoalRecipe()
                    .strippedLog(ACBlockRegistry.STRIPPED_THORNWOOD_LOG.get().asItem())
                    .wood(ACBlockRegistry.THORNWOOD_WOOD.get().asItem())
                    .strippedWood(ACBlockRegistry.STRIPPED_THORNWOOD_WOOD.get().asItem())
                    .door(ACItemRegistry.THORNWOOD_DOOR.get(), "thornwood_door")
                    .trapdoor(ACBlockRegistry.THORNWOOD_TRAPDOOR.get().asItem(), "thornwood_trapdoor")
                    .slab(ACBlockRegistry.THORNWOOD_PLANKS_SLAB.get().asItem(), "thornwood_slab")
                    .fence(ACBlockRegistry.THORNWOOD_PLANKS_FENCE.get().asItem(), "thornwood_fence")
                    .fenceGate(ACBlockRegistry.THORNWOOD_FENCE_GATE.get().asItem(), "thornwood_fence_gate")
                    .stairs(ACBlockRegistry.THORNWOOD_PLANKS_STAIRS.get().asItem(), "thornwood_stairs")
                    .boat(ACItemRegistry.THORNWOOD_BOAT.get(), "thornwood_boat")
                    .chestBoat(ACItemRegistry.THORNWOOD_CHEST_BOAT.get(), "thornwood_chest_boat")
                    .sign(ACItemRegistry.THORNWOOD_SIGN.get(), "thornwood_sign")
                    .hangingSign(ACItemRegistry.THORNWOOD_HANGING_SIGN.get(), "thornwood_hanging_sign")
                    .button(ACBlockRegistry.THORNWOOD_BUTTON.get().asItem(), "thornwood_button")
                    .pressurePlate(ACBlockRegistry.THORNWOOD_PRESSURE_PLATE.get().asItem(), "thornwood_pressure_plate")
                    .registerAllMaterialInfo()
                    .build());
        }
        return DEFAULT_ENTRIES;
    }
}

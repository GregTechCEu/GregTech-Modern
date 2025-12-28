package com.gregtechceu.gtceu.data.recipe.misc.compat;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import com.simibubi.create.AllBlocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CreateRecipes {

    public static void hardRedstoneRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "compat/create/pulse_repeater",
                new ItemStack(AllBlocks.PULSE_REPEATER), "S S", "dBT",
                "RPP",
                'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                'B', new MaterialEntry(TagPrefix.plate, GTMaterials.Brass),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));

        VanillaRecipeHelper.addShapedRecipe(provider, "compat/create/pulse_extender",
                new ItemStack(AllBlocks.PULSE_EXTENDER), "SST", "dBT",
                "RPP",
                'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'B', new MaterialEntry(TagPrefix.plate, GTMaterials.Brass),
                'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));

        VanillaRecipeHelper.addShapedRecipe(provider, "compat/create/pulse_timer", new ItemStack(AllBlocks.PULSE_TIMER),
                "S S", "dBT",
                "RPP",
                'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                'B', new MaterialEntry(TagPrefix.plate, GTMaterials.Brass),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'R', new MaterialEntry(TagPrefix.rod, GTMaterials.Amethyst),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));
    }
}

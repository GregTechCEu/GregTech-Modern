package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class ComputerRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        ASSEMBLER_RECIPES.recipeBuilder("high_power_casing")
                .inputItems(frameGt, Iridium)
                .inputItems(plate, Iridium, 6)
                .inputItems(CustomTags.IV_CIRCUITS)
                .inputItems(wireFine, Cobalt, 16)
                .inputItems(wireFine, Copper, 16)
                .inputItems(wireGtSingle, NiobiumTitanium, 2)
                .outputItems(GTBlocks.HIGH_POWER_CASING.asStack(2))
                .duration(100).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("active_transformer")
                .inputItems(POWER_TRANSFORMER[LuV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(wireGtSingle, IndiumTinBariumTitaniumCuprate, 8)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputFluids(PCBCoolant.getFluid(1000))
                .outputItems(ACTIVE_TRANSFORMER)
                .duration(300).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("laser_cable")
                .inputItems(GTBlocks.CASING_LAMINATED_GLASS.get().asItem(), 1)
                .inputItems(foil, Osmiridium, 2)
                .inputFluids(Polytetrafluoroethylene.getFluid(L))
                .outputItems(LASER_PIPES[0])
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(VA[IV]).save(provider);
    }
}

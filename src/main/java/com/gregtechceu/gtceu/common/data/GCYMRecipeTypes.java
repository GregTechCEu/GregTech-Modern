package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeTextures;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUIBuilders;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.resources.language.I18n;

import static com.gregtechceu.gtceu.api.recipe.GTRecipeType.ProgressBarDirection.LEFT_TO_RIGHT;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.register;

public class GCYMRecipeTypes {

    //////////////////////////////////////
    // ******* Multiblock *******//
    //////////////////////////////////////
    public final static GTRecipeType ALLOY_BLAST_RECIPES = register("alloy_blast_smelter", MULTIBLOCK)
            .setMaxIOSize(9, 0, 3, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GTRecipeTypeTextures::PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSlotOverlay(false, false, false, GTRecipeTypeTextures::FURNACE_OVERLAY_1)
            .setSlotOverlay(false, false, true, GTRecipeTypeTextures::FURNACE_OVERLAY_1)
            .setSlotOverlay(false, true, false, GTRecipeTypeTextures::FURNACE_OVERLAY_2)
            .setSlotOverlay(false, true, true, GTRecipeTypeTextures::FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, false, GTRecipeTypeTextures::FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, true, GTRecipeTypeTextures::FURNACE_OVERLAY_2)
            .addDataInfo(data -> {
                int temp = data.getIntOr("ebf_temp", 0);
                return I18n.get("gtceu.recipe.temperature", FormattingUtil.formatTemperature(temp));
            })
            .addDataInfo(data -> {
                int temp = data.getIntOr("ebf_temp", 0);
                ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);

                if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                    return I18n.get("gtceu.recipe.coil.tier",
                            I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
                }
                return "";
            })
            .setUiBuilder((recipe, widgetGroup) -> GTRecipeTypeUIBuilders.addAlloyBlastSmelterCoilSlot(recipe,
                    widgetGroup))
            .setSound(GTSoundEntries.ARC);

    public static void init() {}
}

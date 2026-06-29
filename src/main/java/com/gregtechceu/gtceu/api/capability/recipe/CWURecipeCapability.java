package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class CWURecipeCapability extends RecipeCapability<Integer> {

    public final static CWURecipeCapability CAP = new CWURecipeCapability();

    protected CWURecipeCapability() {
        super("cwu", 0xFFEEEE00, false, 3, Codec.INT);
    }

    @Override
    public Integer fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return friendlyByteBuf.readVarInt();
    }

    @Override
    public void toNetwork(Integer ingredient, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(ingredient);
    }

    @Override
    public Integer copyInner(Integer content, int multiplier) {
        return content * multiplier;
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipeDefinition recipe, List<Integer> contents,
                           boolean perTick, boolean isInput, MutableInt yOffset) {
        if (perTick) {
            int cwu = contents.stream().mapToInt(Integer::intValue).sum();
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("gtceu.recipe.computation_per_tick", FormattingUtil.formatNumbers(cwu))));
        }
        if (recipe.data.getBoolean("duration_is_total_cwu")) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("gtceu.recipe.total_computation",
                            FormattingUtil.formatNumbers(recipe.duration))));
        }
    }

}

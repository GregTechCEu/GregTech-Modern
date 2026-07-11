package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.trait.NetworkedComputationContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.common.computation.ComputationNetworkManager;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;

import com.mojang.serialization.Codec;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class CWURecipeCapability extends RecipeCapability<Integer> {

    public final static CWURecipeCapability CAP = new CWURecipeCapability();

    protected CWURecipeCapability() {
        super("cwu", 0xFFEEEE00, false, Codec.INT);
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
    public int getMaxParallelByInput(RecipeHandlerGroup holder, GTRecipe recipe, int limit, boolean tick) {
        int requiredCWU = (tick ? recipe.tickInputs : recipe.inputs)
                .getOrDefault(this, List.of()).stream().reduce(0, Integer::sum);
        if (requiredCWU == 0) return limit;

        var availableCWU = holder.getInputHandlerMap().get(this).stream()
                .filter(NetworkedComputationContainer.class::isInstance)
                .map(NetworkedComputationContainer.class::cast)
                .mapToInt(container -> {
                    var level = container.getMachine().getLevel();
                    if (level instanceof ServerLevel serverLevel) {
                        return ComputationNetworkManager.get(serverLevel).getNetWorkAvailableCWUt(container);
                    }
                    return 0;
                })
                .sum();

        return Math.min(limit, availableCWU / requiredCWU);
    }

    @Override
    public Integer copyInner(Integer content, int multiplier) {
        return content * multiplier;
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipeDefinition recipe, List<Integer> contents,
                           int duration, boolean perTick, boolean isInput, MutableInt yOffset) {
        if (perTick) {
            int cwu = contents.stream().mapToInt(Integer::intValue).sum();
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(GTRecipeWidget.LINE_HEIGHT),
                    LocalizationUtils.format("gtceu.recipe.computation_per_tick", FormattingUtil.formatNumbers(cwu))));
        }
        if (recipe.data.getBoolean("duration_is_total_cwu")) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(GTRecipeWidget.LINE_HEIGHT),
                    LocalizationUtils.format("gtceu.recipe.total_computation",
                            FormattingUtil.formatNumbers(recipe.duration))));

        }
    }
}

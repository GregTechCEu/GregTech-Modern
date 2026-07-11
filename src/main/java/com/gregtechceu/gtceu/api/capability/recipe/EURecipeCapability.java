package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class EURecipeCapability extends RecipeCapability<Long> {

    public final static EURecipeCapability CAP = new EURecipeCapability();

    protected EURecipeCapability() {
        super("eu", 0xFFFFFF00, false, Codec.LONG);
    }

    @Override
    public Long fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return friendlyByteBuf.readVarLong();
    }

    @Override
    public void toNetwork(Long ingredient, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarLong(ingredient);
    }

    @Override
    public Long copyInner(Long content, int multiplier) {
        return content * multiplier;
    }

    @Override
    public int limitMaxParallelByOutput(RecipeHandlerGroup holder, GTRecipe recipe, int multiplier, boolean tick) {
        if (tick) {
            long recipeEUt = recipe.getOutputEUt();
            if (recipeEUt == 0) return multiplier;
            long handlerEUt = holder.getOutputHandlerMap().getOrDefault(EURecipeCapability.CAP, List.of())
                    .stream()
                    .filter(IEnergyContainer.class::isInstance)
                    .map(IEnergyContainer.class::cast)
                    .mapToLong(c -> c.getOutputVoltage() * c.getOutputAmperage())
                    .sum();

            return Math.min(multiplier, Math.abs(GTMath.saturatedCast(handlerEUt / recipeEUt)));
        } else {
            var outputs = recipe.getOutputContents(this);
            if (outputs.isEmpty()) return multiplier;

            var handlers = holder.getOutputHandlerMap().get(this);
            if (handlers == null || handlers.isEmpty()) return 0;

            int minMultiplier = 0;
            int maxMultiplier = multiplier;

            long totalEU = 0L;
            for (var content : outputs) totalEU += content;
            if (totalEU != 0 && multiplier > Long.MAX_VALUE / totalEU) {
                maxMultiplier = multiplier = GTMath.saturatedCast(Long.MAX_VALUE / totalEU);
            }

            while (minMultiplier != maxMultiplier) {
                // TODO: fix this
                List<Long> eu = GTUtil.list(totalEU);
                for (var handler : handlers) {
                    if (handler.handleRecipe(IO.OUT, recipe, eu, true)) break;
                }
                int[] bin = ParallelLogic.adjustMultiplier(eu.isEmpty(), minMultiplier, multiplier, maxMultiplier);
                minMultiplier = bin[0];
                multiplier = bin[1];
                maxMultiplier = bin[2];
            }

            return multiplier;
        }
    }

    @Override
    public int getMaxParallelByInput(RecipeHandlerGroup holder, GTRecipe recipe, int limit, boolean tick) {
        if (tick) {
            long recipeEUt = recipe.getInputEUt();
            if (recipeEUt == 0) return limit;
            long handlerEUt = holder.getInputHandlerMap().getOrDefault(EURecipeCapability.CAP, List.of())
                    .stream()
                    .filter(IEnergyContainer.class::isInstance)
                    .map(IEnergyContainer.class::cast)
                    .mapToLong(c -> c.getInputVoltage() * c.getInputAmperage())
                    .sum();

            return Math.min(limit, Math.abs(GTMath.saturatedCast(handlerEUt / recipeEUt)));
        } else {
            var inputs = recipe.getInputContents(this);
            if (inputs.isEmpty()) return limit;

            long consumable = 0;
            for (var content : inputs) {
                consumable += content;
            }

            if (consumable == 0) return limit;

            long sum = 0;
            var handlers = holder.getInputHandlerMap().get(this);
            if (handlers == null || handlers.isEmpty()) return 0;
            for (var handler : handlers) {
                for (var content : handler.getContents()) {
                    if (content instanceof Long es) sum += es;
                }
            }

            return Math.min(GTMath.saturatedCast(sum / consumable), limit);
        }
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipeDefinition recipe, List<Long> contents, int duration,
                           boolean perTick, boolean isInput, MutableInt yOffset) {
        long eut = 0;
        for (long content : contents) {
            eut += content;
        }
        if (eut > 0) {
            float amp = (float) eut / GTValues.V[recipe.tier];
            Component text1 = Component.translatable(isInput ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted",
                    FormattingUtil.formatNumber2Places(amp), GTValues.VN[recipe.tier])
                    .withStyle(ChatFormatting.UNDERLINE);
            var recipeVoltageText = new LabelWidget(3 - xOffset, yOffset.addAndGet(GTRecipeWidget.LINE_HEIGHT),
                    text1)
                    .setTextColor(-1).setDropShadow(true);
            recipeVoltageText.setHoverTooltips(
                    Component.translatable("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(eut))
                            .withStyle(ChatFormatting.UNDERLINE));
            group.addWidget(recipeVoltageText);

            if (!recipe.data.contains("duration_is_total_cwu")) {
                long euTotal = eut * duration;
                Component text2 = Component.translatable("gtceu.recipe.total", FormattingUtil.formatNumbers(euTotal));
                var totalEUText = new LabelWidget(3 - xOffset, yOffset.addAndGet(GTRecipeWidget.LINE_HEIGHT + 1), text2)
                        .setTextColor(-1).setDropShadow(true);
                group.addWidget(totalEUText);
            }
        }
    }

    /**
     * Puts an EU Singleton Content in the given content map
     * 
     * @param contents content map
     * @param eu       EU value to put inside content map
     */
    public static void putEUContent(ContentListMap contents, Long eu) {
        contents.put(EURecipeCapability.CAP, GTUtil.list(eu));
    }
}

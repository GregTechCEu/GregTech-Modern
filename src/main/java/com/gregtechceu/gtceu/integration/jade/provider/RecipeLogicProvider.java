package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RecipeLogicProvider extends MachineTraitProvider<RecipeLogic> {

    public RecipeLogicProvider() {
        super(GTCEu.id("recipe_logic_provider"), RecipeLogic.TYPE);
    }

    @Override
    protected void write(CompoundTag data, BlockAccessor blockAccessor, RecipeLogic capability) {
        data.putBoolean("Working", capability.isWorking());
        var recipeInfo = new CompoundTag();
        var recipe = capability.getLastRecipe();
        if (recipe != null) {
            var EUt = RecipeHelper.getRealEUtWithIO(recipe);

            recipeInfo.putLong("EUt", EUt.getTotalEU());
            recipeInfo.putLong("voltage", getVoltage(capability));
            recipeInfo.putBoolean("isInput", EUt.isInput());
        }

        if (!recipeInfo.isEmpty()) {
            data.put("Recipe", recipeInfo);
        }
    }

    public static long getVoltage(RecipeLogic capability) {
        long voltage = capability.machine.getDisplayRecipeVoltage();

        // default display as LV, this shouldn't happen because a machine is either electric or steam
        return voltage == -1 ? GTValues.V[GTValues.LV] : voltage;
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (capData.getBoolean("Working")) {
            var recipeInfo = capData.getCompound("Recipe");
            if (!recipeInfo.isEmpty()) {
                var EUt = recipeInfo.getLong("EUt");
                var isInput = recipeInfo.getBoolean("isInput");
                boolean isSteam = false;

                if (EUt > 0) {
                    if (blockEntity instanceof SimpleSteamMachine ssm) {
                        EUt = (long) Math.ceil(EUt * ssm.getConversionRate());
                        isSteam = true;
                    } else if (blockEntity instanceof SteamParallelMultiblockMachine smb) {
                        EUt = (long) Math.ceil(EUt * smb.getConversionRate());
                        isSteam = true;
                    }

                    MutableComponent text;

                    if (isSteam) {
                        text = Component.translatable("gtceu.jade.fluid_use", FormattingUtil.formatNumbers(EUt))
                                .withStyle(ChatFormatting.GREEN);
                    } else {
                        var voltage = recipeInfo.getLong("voltage");
                        var tier = GTUtil.getTierByVoltage(voltage);
                        float minAmperage = (float) EUt / voltage;

                        text = Component
                                .translatable("gtceu.jade.amperage_use",
                                        FormattingUtil.formatNumber2Places(minAmperage))
                                .withStyle(ChatFormatting.RED)
                                .append(Component.translatable("gtceu.jade.at").withStyle(ChatFormatting.GREEN));
                        if (tier < GTValues.TIER_COUNT) {
                            text = text.append(Component.literal(GTValues.VNF[tier])
                                    .withStyle(style -> style.withColor(GTValues.VC[tier])));
                        } else {
                            int speed = Mth.clamp(tier - GTValues.TIER_COUNT - 1, 0, GTValues.TIER_COUNT);
                            text = text.append(Component.literal("MAX")
                                    .withStyle(style -> style.withColor(TooltipHelper.rainbowColor(speed)))
                                    .append(Component.literal("+")
                                            .withStyle(style -> style.withColor(GTValues.VC[speed]))
                                            .append(FormattingUtil.formatNumbers(speed))));

                        }
                        text.append(Component.translatable("gtceu.universal.padded_parentheses",
                                (Component.translatable("gtceu.recipe.eu.total",
                                        FormattingUtil.formatNumbers(EUt))))
                                .withStyle(ChatFormatting.WHITE));
                    }

                    if (isInput) {
                        tooltip.add(Component.translatable("gtceu.top.energy_consumption").append(" ").append(text));
                    } else {
                        tooltip.add(Component.translatable("gtceu.top.energy_production").append(" ").append(text));
                    }
                }
            }
        } else {
            if (blockEntity instanceof IRecipeLogicMachine rlm) {
                var logic = rlm.getRecipeLogic();

                if (logic.showFancyTooltip() && logic.isWorkingEnabled()) {
                    Component status = logic.isWaiting() ?
                            Component.translatable("gtceu.recipe_logic.recipe_waiting")
                                    .withStyle(ChatFormatting.YELLOW) :
                            Component.translatable("gtceu.recipe_logic.setup_fail").withStyle(ChatFormatting.RED);
                    tooltip.add(status);
                    logic.getFancyTooltip().forEach(tooltip::add);
                }
            }
        }
    }
}

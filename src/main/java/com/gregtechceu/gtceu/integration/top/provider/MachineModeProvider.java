package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class MachineModeProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("machine_mode");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        if (level.getBlockEntity(iProbeHitData.getPos()) instanceof MetaMachineBlockEntity blockEntity) {
            GTRecipeType[] recipeTypes = blockEntity.getMetaMachine().getDefinition().getRecipeTypes();
            if (recipeTypes.length > 1) {
                if (blockEntity.getMetaMachine() instanceof IRecipeLogicMachine recipeLogicMachine) {
                    GTRecipeType currentRecipeType = recipeLogicMachine.getRecipeType();
                    if (player.isShiftKeyDown()) {
                        iProbeInfo.text(Component.translatable("gtceu.top.machine_mode"));

                        for (GTRecipeType recipeType : recipeTypes) {
                            IProbeInfo horizontalPane = iProbeInfo.horizontal(
                                    iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));

                            if (recipeType == currentRecipeType) {
                                horizontalPane.text(ChatFormatting.BLUE + " > ");
                                horizontalPane.text(CompoundText.create().important("%s.%s".formatted(
                                        recipeType.registryName.getNamespace(), recipeType.registryName.getPath())));
                            } else {
                                horizontalPane.text("   ");
                                horizontalPane.text(CompoundText.create().label("%s.%s".formatted(
                                        recipeType.registryName.getNamespace(), recipeType.registryName.getPath())));
                            }
                        }
                    } else {
                        iProbeInfo.text(Component.translatable("gtceu.top.machine_mode")
                                .append(Component.translatable(currentRecipeType.registryName.toLanguageKey())));
                    }
                }
            }
        }
    }
}

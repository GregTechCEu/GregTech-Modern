package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import org.apache.commons.lang3.StringUtils;

public class AutoOutputInfoProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("auto_output_info");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        var pos = iProbeHitData.getPos();

        if (MetaMachine.getMachine(level, pos) instanceof IAutoOutputItem outputItem) {
            var direction = outputItem.getOutputFacingItems();
            addAutoOutputInfo(iProbeInfo, player, level, pos, direction, "gtceu.top.item_auto_output",
                    outputItem.isAllowInputFromOutputSideItems(), outputItem.isAutoOutputItems());

        }
        if (MetaMachine.getMachine(level, pos) instanceof IAutoOutputFluid outputFluid) {
            var direction = outputFluid.getOutputFacingFluids();
            addAutoOutputInfo(iProbeInfo, player, level, pos, direction, "gtceu.top.item_auto_output",
                    outputFluid.isAllowInputFromOutputSideFluids(), outputFluid.isAutoOutputFluids());

        }
    }

    private void addAutoOutputInfo(IProbeInfo iProbeInfo, Player player, Level level, BlockPos blockPos,
                                   Direction direction, String text, boolean allowInput, boolean auto) {
        if (direction != null) {
            IProbeInfo horizontalPane = iProbeInfo
                    .horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontalPane.text(CompoundText.create()
                    .info(Component.translatable(text, StringUtils.capitalize(direction.getName()) + " ")));
            if (player.isShiftKeyDown()) {
                if (level != null) {
                    var pos = blockPos.relative(direction);
                    var block = level.getBlockState(pos).getBlock().asItem().getDefaultInstance();
                    if (!block.isEmpty()) {
                        horizontalPane.item(block, new ItemStyle().width(16).height(16)).text(" ");
                    }
                }
            }

            if (allowInput || auto) {
                var compoundText = CompoundText.create().text("(");
                if (auto) {
                    compoundText.ok(Component.translatable("gtceu.top.auto_output"));
                }

                if (allowInput && auto) {
                    compoundText.style(TextStyleClass.INFO).text("/");
                }

                if (allowInput) {
                    compoundText.ok(Component.translatable("gtceu.top.allow_output_input"));
                }
                compoundText.style(TextStyleClass.INFO).text(")");
                horizontalPane.text(compoundText);
            }
        }
    }
}

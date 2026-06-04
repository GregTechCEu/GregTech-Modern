package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableBlock extends MaterialPipeBlock<WireProperties> {



    public CableBlock(Properties properties, CableVariant cableVariant, Material material) {
        super(properties, cableVariant, GTPipeNetworks.ENERGY, material);
    }

    @Override
    public CableVariant getPipeVariant() {
        return (CableVariant)super.getPipeVariant();
    }

    @Override
    public int tinted(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        if (getPipeVariant().isCable && (index == 0 || index == 2)) {
            return 0x404040;
        }
        return super.tinted(state, level, pos, index);
    }

    @Override
    public WireProperties createRawData() {
        return material.getProperty(PropertyKey.WIRE);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        WireProperties wireProperties = createProperties();
        int tier = GTUtil.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor())
            tooltip.add(Component.translatable("gtceu.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage",
                FormattingUtil.formatNumbers(wireProperties.getVoltage()), GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage",
                FormattingUtil.formatNumbers(wireProperties.getAmperage())));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block",
                FormattingUtil.formatNumbers(wireProperties.getLossPerBlock())));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // dont apply damage if there is a frame box
        var pipe = PipeBlock.getPipeBE(level, pos);
        if (pipe == null) {
            GTCEu.LOGGER.error("Pipe was null");
            return;
        }
        if (!pipe.getFrameMaterial().isNull()) {
            BlockState frameState = GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipe.getFrameMaterial())
                    .getDefaultState();
            frameState.getBlock().entityInside(frameState, level, pos, entity);
            return;
        }
        if (level.isClientSide) return;

        CableVariant cableVariant = (CableVariant)pipe.getPipeType();

        if (cableVariant.insulationLevel == -1 && entity instanceof LivingEntity entityLiving) {
            CableBlockEntity cable = (CableBlockEntity) getPipeBE(level, pos);
            if (cable != null && cable.getFrameMaterial().isNull() &&
                    cable.getNodeData().getLossPerBlock() > 0) {
                long voltage = cable.getCurrentMaxVoltage();
                double amperage = cable.getAverageAmperage();
                if (voltage > 0L && amperage > 0L) {
                    float damageAmount = (float) ((GTUtil.getTierByVoltage(voltage) + 1) * amperage * 4);
                    entityLiving.hurt(GTDamageTypes.ELECTRIC.source(level), damageAmount);
                    if (entityLiving instanceof ServerPlayer) {
                        // TODO advancments
                        // AdvancementTriggers.ELECTROCUTION_DEATH.trigger((ServerPlayer) entityLiving);
                    }
                }
            }
        }
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }
}

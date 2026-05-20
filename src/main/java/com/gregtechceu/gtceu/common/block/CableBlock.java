package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.PipeNetworkType;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.common.pipelike.SegmentPropertyTypes;
import com.gregtechceu.gtceu.common.pipelike.cable.CableBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.cable.WireType;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableBlock extends MaterialPipeBlock<WireType, WireProperties> {

    public CableBlock(Properties properties, WireType wireType, Material material) {
        super(properties, wireType, material, material.getProperty(PropertyKey.WIRE));
    }

    @Override
    public int tinted(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        if (pipeType.isCable && (index == 0 || index == 2)) {
            return 0x404040;
        }
        return super.tinted(state, level, pos, index);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<WireType>> getBlockEntityType() {
        return GTBlockEntities.CABLE.get();
    }

    @Override
    public PipeNetworkType getPipeType() {
        return GTPipeNetworks.ENERGY;
    }

    @Override
    public boolean canPipeConnectToBlock(PipeBlockEntity<WireType> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null &&
                tile.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, side.getOpposite()).isPresent();
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        boolean isSuperconductor = defaultSegmentProperties.getPropertyValue(SegmentPropertyTypes.IS_SUPERCONDUCTOR);
        long voltage = defaultSegmentProperties.getPropertyValue(SegmentPropertyTypes.MAX_VOLTAGE);
        int amps = defaultSegmentProperties.getPropertyValue(SegmentPropertyTypes.MAX_AMPS);
        int loss = defaultSegmentProperties.getPropertyValue(SegmentPropertyTypes.LOSS_PER_BLOCK);

        int tier = GTUtil.getTierByVoltage(voltage);
        if (isSuperconductor) tooltip.add(Component.translatable("gtceu.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage",
                FormattingUtil.formatNumbers(voltage), GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage",
                FormattingUtil.formatNumbers(amps)));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block",
                FormattingUtil.formatNumbers(loss)));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // dont apply damage if there is a frame box
        var pipeNode = getPipeTile(level, pos);
        if (pipeNode == null) {
            GTCEu.LOGGER.error("Pipe was null");
            return;
        }
        if (!pipeNode.getFrameMaterial().isNull()) {
            BlockState frameState = GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipeNode.getFrameMaterial())
                    .getDefaultState();
            frameState.getBlock().entityInside(frameState, level, pos, entity);
            return;
        }
        if (level.isClientSide) return;

        WireType wireType = getPipeTile(level, pos).getPipeType();
        if (wireType.insulationLevel == -1 && entity instanceof LivingEntity entityLiving) {
            CableBlockEntity cable = (CableBlockEntity) getPipeTile(level, pos);
            if (cable != null && cable.getFrameMaterial().isNull() &&
                    defaultSegmentProperties.getPropertyValue(SegmentPropertyTypes.LOSS_PER_BLOCK) > 0) {
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

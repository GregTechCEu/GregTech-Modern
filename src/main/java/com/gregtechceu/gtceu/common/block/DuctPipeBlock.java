package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pipenet.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.PipeNetworkType;
import com.gregtechceu.gtceu.api.pipenet.property.FloatSegmentProperty;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardCleanerTrait;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardEmitterTrait;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.common.pipelike.SegmentPropertyTypes;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DuctPipeBlock extends PipeBlock<DuctPipeType> {

    public DuctPipeBlock(Properties properties, DuctPipeType type) {
        super(properties, type,
                type.buildSegmentProperties(null));
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<DuctPipeType>> getBlockEntityType() {
        return GTBlockEntities.DUCT_PIPE.get();
    }

    @Override
    public PipeNetworkType getPipeType() {
        return GTPipeNetworks.DUCT;
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return new PipeModel(this, provider, this.pipeType.getThickness(),
                GTCEu.id("block/pipe/pipe_duct_side"), GTCEu.id("block/pipe/pipe_duct_in"));
    }

    @Override
    public boolean canPipeConnectToBlock(PipeBlockEntity<DuctPipeType> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null &&
                (tile.getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, side.getOpposite()).isPresent() ||
                        tile instanceof MetaMachine metaMachine &&
                                (metaMachine.getTrait(EnvironmentalHazardCleanerTrait.TYPE) != null ||
                                        metaMachine.getTrait(EnvironmentalHazardEmitterTrait.TYPE) !=
                                                null));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FloatSegmentProperty transferRate = defaultSegmentProperties.getProperty(SegmentPropertyTypes.TRANSFER_RATE);
        tooltip.add(Component.translatable("gtceu.duct_pipe.transfer_rate", transferRate.getValue()));
    }
}

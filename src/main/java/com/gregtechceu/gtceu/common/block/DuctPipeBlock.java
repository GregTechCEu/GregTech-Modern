package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardCleaner;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardEmitter;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.common.blockentity.DuctPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;
import com.gregtechceu.gtceu.common.pipelike.duct.LevelDuctPipeNet;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DuctPipeBlock extends PipeBlock<DuctPipeType, DuctPipeProperties, LevelDuctPipeNet> {

    public final PipeBlockRenderer renderer;
    public final PipeModel model;
    private final DuctPipeProperties properties;

    public DuctPipeBlock(Properties properties, DuctPipeType type) {
        super(properties, type);
        this.properties = new DuctPipeProperties(type.getRateMultiplier());
        this.model = type.createPipeModel();
        this.renderer = new PipeBlockRenderer(this.model);
    }

    @Override
    public LevelDuctPipeNet getWorldPipeNet(ServerLevel world) {
        return LevelDuctPipeNet.getOrCreate(world);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<DuctPipeType, DuctPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.DUCT_PIPE.get();
    }

    @Override
    public DuctPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return properties;
    }

    @Override
    public DuctPipeProperties createProperties(IPipeNode<DuctPipeType, DuctPipeProperties> pipeTile) {
        DuctPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public DuctPipeProperties getFallbackType() {
        return properties;
    }

    @Override
    public @Nullable PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Override
    protected PipeModel getPipeModel() {
        return model;
    }

    @Override
    public boolean canPipesConnect(IPipeNode<DuctPipeType, DuctPipeProperties> selfTile, Direction side,
                                   IPipeNode<DuctPipeType, DuctPipeProperties> sideTile) {
        return selfTile instanceof DuctPipeBlockEntity && sideTile instanceof DuctPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<DuctPipeType, DuctPipeProperties> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null &&
                (tile.getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, side.getOpposite()).isPresent() ||
                        tile instanceof MetaMachineBlockEntity metaMachine &&
                                (metaMachine.getMetaMachine() instanceof IEnvironmentalHazardCleaner ||
                                        metaMachine.getMetaMachine() instanceof IEnvironmentalHazardEmitter));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("gtceu.duct_pipe.transfer_rate",
                this.pipeType.modifyProperties(this.properties).getTransferRate()));
    }
}

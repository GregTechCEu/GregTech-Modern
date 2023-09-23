package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeData;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeType;
import com.gregtechceu.gtceu.common.pipelike.optical.WorldOpticalPipeNet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpticalPipeBlock extends PipeBlock<OpticalPipeType, OpticalPipeData, WorldOpticalPipeNet> {

    public final PipeModel model;
    public final PipeBlockRenderer renderer;

    public OpticalPipeBlock(BlockBehaviour.Properties properties, DyeColor color) {
        super(properties, OpticalPipeType.NORMAL);
        this.model = new PipeModel(OpticalPipeType.NORMAL.getThickness(), () -> GTCEu.id("block/pipe/pipe_optical_side"), () -> GTCEu.id("block/pipe/pipe_optical_side"));
        this.renderer = new PipeBlockRenderer(this.model);
    }

    @Override
    public WorldOpticalPipeNet getWorldPipeNet(ServerLevel world) {
        return WorldOpticalPipeNet.getOrCreate(world);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<OpticalPipeType, OpticalPipeData>> getBlockEntityType() {
        return GTBlockEntities.OPTICAL_PIPE.get();
    }

    @Override
    public OpticalPipeData createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return new OpticalPipeData();
    }

    @Override
    public OpticalPipeData getFallbackType() {
        return new OpticalPipeData();
    }

    @Override
    public @org.jetbrains.annotations.Nullable PipeBlockRenderer getRenderer(BlockState state) {
        return this.renderer;
    }

    @Override
    protected PipeModel getPipeModel() {
        return this.model;
    }

    protected boolean isPipeTool(@Nonnull ItemStack stack) {
        return ToolHelper.is(stack, GTToolType.WIRE_CUTTER);
    }

    public boolean canPipesConnect(IPipeNode<OpticalPipeType, OpticalPipeData> selfTile, Direction side, IPipeNode<OpticalPipeType, OpticalPipeData> sideTile) {
        return selfTile instanceof OpticalPipeBlockEntity && sideTile instanceof OpticalPipeBlockEntity;
    }

    public boolean canPipeConnectToBlock(IPipeNode<OpticalPipeType, OpticalPipeData> selfTile, Direction side, @Nullable BlockEntity tile) {
        if (tile == null) return false;
        if (GTCapabilityHelper.getDataAccess(tile.getLevel(), tile.getBlockPos(), side.getOpposite()) != null) return true;
        return GTCapabilityHelper.getComputationProvider(tile.getLevel(), tile.getBlockPos(), side.getOpposite()) != null;
    }

    public boolean isHoldingPipe(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        return stack != ItemStack.EMPTY;// && stack.getItem() instanceof ItemBlockOpticalPipe;
    }
}

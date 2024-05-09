package com.gregtechceu.gtceu.common.blocks;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blocks.PipeBlock;
import com.gregtechceu.gtceu.api.blockentities.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeType;
import com.gregtechceu.gtceu.common.pipelike.optical.LevelOpticalPipeNet;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OpticalPipeBlock extends PipeBlock<OpticalPipeType, OpticalPipeProperties, LevelOpticalPipeNet> {

    public final PipeBlockRenderer renderer;
    @Getter
    public final PipeModel pipeModel;

    private final OpticalPipeType pipeType;
    private final OpticalPipeProperties properties;

    public OpticalPipeBlock(BlockBehaviour.Properties properties, @NotNull OpticalPipeType pipeType) {
        super(properties, pipeType);
        this.pipeType = pipeType;
        this.properties = OpticalPipeProperties.INSTANCE;
        this.pipeModel = new PipeModel(pipeType.getThickness(), () -> GTCEu.id("block/pipe/pipe_optical_side"), () -> GTCEu.id("block/pipe/pipe_optical_in"), null, null);
        this.renderer = new PipeBlockRenderer(this.pipeModel);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_DATA_ACCESS, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof OpticalPipeBlockEntity opticalPipeBlockEntity) {
                if (level.isClientSide) {
                    return opticalPipeBlockEntity.getClientDataHandler();
                }
                if (opticalPipeBlockEntity.getHandlers().isEmpty()) {
                    opticalPipeBlockEntity.initHandlers();
                }
                opticalPipeBlockEntity.checkNetwork();
                return opticalPipeBlockEntity.getHandlers().getOrDefault(side, opticalPipeBlockEntity.getDefaultHandler());
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof OpticalPipeBlockEntity opticalPipeBlockEntity) {
                if (level.isClientSide) {
                    return opticalPipeBlockEntity.getClientComputationHandler();
                }
                if (opticalPipeBlockEntity.getHandlers().isEmpty()) {
                    opticalPipeBlockEntity.initHandlers();
                }
                opticalPipeBlockEntity.checkNetwork();
                return opticalPipeBlockEntity.getHandlers().getOrDefault(side, opticalPipeBlockEntity.getDefaultHandler());
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof PipeBlockEntity<?, ?> pipe) {
                return pipe.getCoverContainer();
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_TOOLABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IToolable toolable) {
                return toolable;
            }
            return null;
        }, this);
    }

    @Override
    public LevelOpticalPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelOpticalPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<OpticalPipeType, OpticalPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.OPTICAL_PIPE.get();
    }

    @Override
    public OpticalPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return null;
    }

    @Override
    public OpticalPipeProperties createProperties(@NotNull IPipeNode<OpticalPipeType, OpticalPipeProperties> pipeTile) {
        OpticalPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public OpticalPipeProperties getFallbackType() {
        return OpticalPipeProperties.INSTANCE;
    }

    @Override
    public @Nullable PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Override
    public boolean canPipesConnect(IPipeNode<OpticalPipeType, OpticalPipeProperties> selfTile, Direction side,
                                   IPipeNode<OpticalPipeType, OpticalPipeProperties> sideTile) {
        return selfTile instanceof OpticalPipeBlockEntity && sideTile instanceof OpticalPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<OpticalPipeType, OpticalPipeProperties> selfTile, Direction side, @Nullable BlockEntity tile) {
        if (tile == null || tile.getLevel() == null) return false;
        if (tile.getLevel().getCapability(GTCapability.CAPABILITY_DATA_ACCESS, tile.getBlockPos(), side.getOpposite()) != null) return true;
        return tile.getLevel().getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, tile.getBlockPos(), side.getOpposite()) != null;
    }
}
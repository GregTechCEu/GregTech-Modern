package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.ActivablePipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.LaserPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTBlockStateProperties;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;
import com.gregtechceu.gtceu.common.pipelike.laser.LevelLaserPipeNet;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

public class LaserPipeBlock extends PipeBlock<LaserPipeType, LaserPipeProperties, LevelLaserPipeNet> {

    private final LaserPipeProperties properties;

    public LaserPipeBlock(Properties properties, LaserPipeType type) {
        super(properties, type);
        this.properties = LaserPipeProperties.INSTANCE;

        registerDefaultState(defaultBlockState().setValue(GTBlockStateProperties.ACTIVE, false));
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_LASER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof LaserPipeBlockEntity laserPipeBlockEntity) {
                if (level.isClientSide) {
                    return laserPipeBlockEntity.clientCapability;
                }
                if (laserPipeBlockEntity.getHandlers().isEmpty()) {
                    laserPipeBlockEntity.initHandlers();
                }
                laserPipeBlockEntity.checkNetwork();
                return laserPipeBlockEntity.getHandlers().getOrDefault(side, laserPipeBlockEntity.getDefaultHandler());
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

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (state, level, pos, index) -> {
            if (pos != null && level != null &&
                    level.getBlockEntity(pos) instanceof PipeBlockEntity<?, ?> pipe) {
                if (!pipe.getFrameMaterial().isNull()) {
                    if (index == 3) {
                        return pipe.getFrameMaterial().getMaterialRGB();
                    } else if (index == 4) {
                        return pipe.getFrameMaterial().getMaterialSecondaryRGB();
                    }
                }
                if (pipe.isPainted()) {
                    return pipe.getRealColor();
                }
            }
            return -1;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GTBlockStateProperties.ACTIVE);
    }

    @Override
    public LevelLaserPipeNet getWorldPipeNet(ServerLevel world) {
        return LevelLaserPipeNet.getOrCreate(world);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<LaserPipeType, LaserPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.LASER_PIPE.get();
    }

    @Override
    public LaserPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return LaserPipeProperties.INSTANCE;
    }

    @Override
    public LaserPipeProperties createProperties(IPipeNode<LaserPipeType, LaserPipeProperties> pipeTile) {
        LaserPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public LaserPipeProperties getFallbackType() {
        return LaserPipeProperties.INSTANCE;
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        ActivablePipeModel model = new ActivablePipeModel(this, LaserPipeType.NORMAL.getThickness(),
                GTCEu.id("block/pipe/pipe_laser_side"), GTCEu.id("block/pipe/pipe_laser_in"),
                provider);
        model.setSideOverlay(GTCEu.id("block/pipe/pipe_laser_side_overlay"));
        model.setSideOverlayActive(GTCEu.id("block/pipe/pipe_laser_side_overlay_emissive"));
        return model;
    }

    @Override
    public boolean canPipesConnect(IPipeNode<LaserPipeType, LaserPipeProperties> selfTile, Direction side,
                                   IPipeNode<LaserPipeType, LaserPipeProperties> sideTile) {
        return selfTile instanceof LaserPipeBlockEntity && sideTile instanceof LaserPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<LaserPipeType, LaserPipeProperties> selfTile, Direction side,
                                         Level level, BlockPos pos) {
        return GTCapabilityHelper.getLaser(level, pos, side.getOpposite()) != null;
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }
}

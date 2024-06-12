package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.common.blockentity.LaserPipeBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;
import com.gregtechceu.gtceu.common.pipelike.laser.LevelLaserPipeNet;
import com.gregtechceu.gtceu.data.blockentity.GTBlockEntities;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LaserPipeBlock extends PipeBlock<LaserPipeType, LaserPipeProperties, LevelLaserPipeNet> {

    public final PipeBlockRenderer renderer;
    public final PipeModel model;
    private final LaserPipeProperties properties;

    public LaserPipeBlock(Properties properties, LaserPipeType type) {
        super(properties, type);
        this.properties = LaserPipeProperties.INSTANCE;
        this.model = new PipeModel(LaserPipeType.NORMAL.getThickness(), () -> GTCEu.id("block/pipe/pipe_laser_side"),
                () -> GTCEu.id("block/pipe/pipe_laser_in"), null, null);
        this.renderer = new PipeBlockRenderer(this.model);
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
        return (blockState, level, blockPos, index) -> {
            if (blockPos != null && level != null &&
                    level.getBlockEntity(blockPos) instanceof PipeBlockEntity<?, ?> pipe) {
                if (pipe.getFrameMaterial() != null) {
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
    public @Nullable PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Override
    protected PipeModel getPipeModel() {
        return model;
    }

    @Override
    public boolean canPipesConnect(IPipeNode<LaserPipeType, LaserPipeProperties> selfTile, Direction side,
                                   IPipeNode<LaserPipeType, LaserPipeProperties> sideTile) {
        return selfTile instanceof LaserPipeBlockEntity && sideTile instanceof LaserPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<LaserPipeType, LaserPipeProperties> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null && tile.getLevel().getCapability(GTCapability.CAPABILITY_LASER, tile.getBlockPos(),
                tile.getBlockState(), tile, side.getOpposite()) != null;
    }
}

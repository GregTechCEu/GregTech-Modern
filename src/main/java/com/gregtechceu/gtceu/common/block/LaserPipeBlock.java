package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.PipeNetworkType;
import com.gregtechceu.gtceu.api.pipenet.PipeSegmentPropertyHolder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.ActivablePipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LaserPipeBlock extends PipeBlock<LaserPipeType> {

    public LaserPipeBlock(Properties properties, LaserPipeType type) {
        super(properties, type, new PipeSegmentPropertyHolder());

        registerDefaultState(defaultBlockState().setValue(GTBlockStateProperties.ACTIVE, false));
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (state, level, pos, index) -> {
            if (pos != null && level != null &&
                    level.getBlockEntity(pos) instanceof PipeBlockEntity<?> pipe) {
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
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        ActivablePipeModel model = new ActivablePipeModel(this, LaserPipeType.NORMAL.getThickness(),
                GTCEu.id("block/pipe/pipe_laser_side"), GTCEu.id("block/pipe/pipe_laser_in"),
                provider);
        model.setSideOverlay(GTCEu.id("block/pipe/pipe_laser_side_overlay"));
        model.setSideOverlayActive(GTCEu.id("block/pipe/pipe_laser_side_overlay_emissive"));
        return model;
    }

    @Override
    public PipeNetworkType getPipeType() {
        return GTPipeNetworks.LASER;
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<LaserPipeType>> getBlockEntityType() {
        return GTBlockEntities.LASER_PIPE.get();
    }

    @Override
    public boolean canPipeConnectToBlock(PipeBlockEntity<LaserPipeType> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null && tile.getCapability(GTCapability.CAPABILITY_LASER, side.getOpposite()).isPresent();
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }
}

package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/5/25
 * @implNote PumpHatchPartRenderer
 */
public class PumpHatchPartRenderer extends MachineRenderer {

    public static final ResourceLocation PIPE_OUT = GTCEu.id("block/overlay/machine/overlay_pipe_out");
    public static final ResourceLocation FLUID_HATCH = GTCEu.id("block/overlay/machine/overlay_fluid_hatch");

    public PumpHatchPartRenderer() {
        super(GTCEu.id("block/machine/part/pump_hatch"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
                                       RandomSource rand) {
        if (state.getBlock() instanceof MetaMachineBlock machineBlock && side == machineBlock.getFrontFacing(state)) {
            var quads = new ArrayList<>(super.renderModel(level, pos, state, side, rand));
            quads.add(StaticFaceBakery.bakeFace(side, ModelFactory.getBlockSprite(PIPE_OUT)));
            quads.add(StaticFaceBakery.bakeFace(
                    side, ModelFactory.getBlockSprite(FLUID_HATCH),
                    BlockModelRotation.X0_Y0, -101, 15));
            return quads;
        }
        return super.renderModel(level, pos, state, side, rand);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand,
                                             @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (state != null && state.getBlock() instanceof MetaMachineBlock machineBlock &&
                side == machineBlock.getFrontFacing(state)) {
            var quads = new ArrayList<>(super.getQuads(state, side, rand, modelData, renderType));
            quads.add(StaticFaceBakery.bakeFace(side, ModelFactory.getBlockSprite(PIPE_OUT)));
            quads.add(StaticFaceBakery.bakeFace(
                    side, ModelFactory.getBlockSprite(FLUID_HATCH),
                    BlockModelRotation.X0_Y0, -101, 15));
            return quads;
        }
        return super.getQuads(state, side, rand, modelData, renderType);
    }
}

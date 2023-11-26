package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * @author KilaBash
 * @date 2023/5/25
 * @implNote PumpHatchPartRenderer
 */
public class PumpHatchPartRenderer extends CTMModelRenderer {

    public static final ResourceLocation PIPE_OUT = GTCEu.id("block/overlay/machine/overlay_pipe_out");
    public static final ResourceLocation FLUID_HATCH = GTCEu.id("block/overlay/machine/overlay_fluid_hatch");

    public PumpHatchPartRenderer() {
        super(GTCEu.id("block/machine/part/pump_hatch"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand) {
        if (state.getBlock() instanceof MetaMachineBlock machineBlock && side == machineBlock.getFrontFacing(state)) {
            var quads = new ArrayList<>(super.renderModel(level, pos, state, side, rand));
            quads.add(FaceQuad.bakeFace(side, ModelFactory.getBlockSprite(PIPE_OUT)));
            quads.add(FaceQuad.bakeFace(side, ModelFactory.getBlockSprite(FLUID_HATCH), BlockModelRotation.X0_Y0, -101, 15));
            return quads;
        }
        return super.renderModel(level, pos, state, side, rand);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(PIPE_OUT);
            register.accept(FLUID_HATCH);
        }
    }
}

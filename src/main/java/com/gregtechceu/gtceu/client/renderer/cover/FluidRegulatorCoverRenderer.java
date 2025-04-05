package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.PumpCover;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote ConveyorCoverRenderer
 */
public class FluidRegulatorCoverRenderer implements ICoverRenderer {

    public final static FluidRegulatorCoverRenderer INSTANCE = new FluidRegulatorCoverRenderer();
    public final static ResourceLocation PUMP_OVERLAY_OUT = GTCEu.id("block/cover/overlay_pump");
    public final static ResourceLocation PUMP_OVERLAY_IN = GTCEu.id("block/cover/overlay_pump_inverted");

    protected FluidRegulatorCoverRenderer() {
        if (GTCEu.isClientSide()) {
            registerEvent();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, Direction modelFacing, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState) {
        if (side == coverBehavior.attachedSide && coverBehavior instanceof PumpCover pump && modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing,
                    ModelFactory.getBlockSprite(pump.getIo() == IO.OUT ? PUMP_OVERLAY_OUT : PUMP_OVERLAY_IN),
                    modelState));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(PUMP_OVERLAY_IN);
            register.accept(PUMP_OVERLAY_OUT);
        }
    }
}

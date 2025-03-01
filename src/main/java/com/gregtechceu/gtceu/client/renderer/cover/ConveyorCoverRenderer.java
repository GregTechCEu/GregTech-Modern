package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;

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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote ConveyorCoverRenderer
 */
public class ConveyorCoverRenderer implements ICoverRenderer {

    public final static ConveyorCoverRenderer INSTANCE = new ConveyorCoverRenderer();
    public final static ResourceLocation CONVEYOR_OVERLAY = GTCEu.id("block/cover/overlay_conveyor");
    public final static ResourceLocation CONVEYOR_OVERLAY_OUT = GTCEu.id("block/cover/overlay_conveyor_emissive");
    public final static ResourceLocation CONVEYOR_OVERLAY_IN = GTCEu
            .id("block/cover/overlay_conveyor_inverted_emissive");

    protected ConveyorCoverRenderer() {
        if (GTCEu.isClientSide()) {
            registerEvent();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, @Nullable Direction modelFacing, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState) {
        if (side == coverBehavior.attachedSide && coverBehavior instanceof ConveyorCover conveyor &&
                modelFacing != null) {
            quads.add(
                    StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(CONVEYOR_OVERLAY), modelState));
            quads.add(StaticFaceBakery.bakeFace(modelFacing,
                    ModelFactory
                            .getBlockSprite(conveyor.getIo() == IO.OUT ? CONVEYOR_OVERLAY_OUT : CONVEYOR_OVERLAY_IN),
                    modelState, -101, 15));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(CONVEYOR_OVERLAY);
            register.accept(CONVEYOR_OVERLAY_IN);
            register.accept(CONVEYOR_OVERLAY_OUT);
        }
    }
}

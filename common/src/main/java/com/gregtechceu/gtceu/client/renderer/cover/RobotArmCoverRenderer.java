package com.gregtechceu.gtceu.client.renderer.cover;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class RobotArmCoverRenderer implements ICoverRenderer {

    public final static RobotArmCoverRenderer INSTANCE = new RobotArmCoverRenderer();
    public final static ResourceLocation ARM_OVERLAY = GTCEu.id("block/cover/overlay_arm");
    public final static ResourceLocation ARM_OVERLAY_OUT = GTCEu.id("block/cover/overlay_arm_emissive");
    public final static ResourceLocation AR_OVERLAY_IN = GTCEu.id("block/cover/overlay_arm_inverted_emissive");

    protected RobotArmCoverRenderer() {
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand, @NotNull CoverBehavior coverBehavior, @Nullable Direction modelFacing, ModelState modelState) {
        if (side == coverBehavior.attachedSide && coverBehavior instanceof RobotArmCover robotArm && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(ARM_OVERLAY), modelState));
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(robotArm.getIo() == IO.OUT ? ARM_OVERLAY_OUT : AR_OVERLAY_IN),  modelState, -101, 15));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(ARM_OVERLAY);
            register.accept(AR_OVERLAY_IN);
            register.accept(ARM_OVERLAY_OUT);
        }
    }
}

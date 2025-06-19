package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RobotArmCoverRenderer implements ICoverRenderer {

    public final static RobotArmCoverRenderer INSTANCE = new RobotArmCoverRenderer();
    public final static ResourceLocation ARM_OVERLAY = GTCEu.id("block/cover/overlay_arm");
    public final static ResourceLocation ARM_OVERLAY_OUT = GTCEu.id("block/cover/overlay_arm_emissive");
    public final static ResourceLocation AR_OVERLAY_IN = GTCEu.id("block/cover/overlay_arm_inverted_emissive");

    protected RobotArmCoverRenderer() {
        if (GTCEu.isClientSide()) {
            ModelUtils.registerAddModelsEventListener(this::loadModels);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, @Nullable Direction elementSide, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (side == coverBehavior.attachedSide && coverBehavior instanceof RobotArmCover robotArm &&
                elementSide != null) {
            quads.add(StaticFaceBakery.bakeFace(elementSide, ModelFactory.getBlockSprite(ARM_OVERLAY), modelState));
            quads.add(StaticFaceBakery.bakeFace(elementSide,
                    ModelFactory.getBlockSprite(robotArm.getIo() == IO.OUT ? ARM_OVERLAY_OUT : AR_OVERLAY_IN),
                    modelState, -101, 15));
        }
    }

    protected void loadModels(ModelEvent.RegisterAdditional event) {
        event.register(GTCEu.id("block/cover/robot_arm"));
        event.register(GTCEu.id("block/cover/robot_arm_inverted"));
    }
}

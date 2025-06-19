package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.PumpCover;

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

public class PumpCoverRenderer implements ICoverRenderer {

    public final static PumpCoverRenderer INSTANCE = new PumpCoverRenderer();
    public final static ResourceLocation PUMP_OVERLAY_OUT = GTCEu.id("block/cover/overlay_pump");
    public final static ResourceLocation PUMP_OVERLAY_IN = GTCEu.id("block/cover/overlay_pump_inverted");

    protected PumpCoverRenderer() {
        if (GTCEu.isClientSide()) {
            ModelUtils.registerAddModelsEventListener(this::loadModels);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, Direction elementSide, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (elementSide == null) return;
        if (side == coverBehavior.attachedSide && coverBehavior instanceof PumpCover pump) {
            quads.add(StaticFaceBakery.bakeFace(elementSide,
                    ModelFactory.getBlockSprite(pump.getIo() == IO.OUT ? PUMP_OVERLAY_OUT : PUMP_OVERLAY_IN),
                    modelState));
        }
    }

    protected void loadModels(ModelEvent.RegisterAdditional event) {
        event.register(GTCEu.id("block/cover/pump"));
        event.register(GTCEu.id("block/cover/pump_inverted"));
    }
}

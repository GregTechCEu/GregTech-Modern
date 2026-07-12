package com.gregtechceu.gtceu.client.renderer.fluid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;
import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;
import com.gregtechceu.gtceu.utils.ScopedValue;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.core.SectionPos.SECTION_MASK;

@UtilityClass
public class InvertedFluidRenderer {

    public static final ScopedValue.Boolean INVERTED_FLUID_RENDERING = new ScopedValue.Boolean(false);

    private static final ThreadLocal<BlockPos.MutableBlockPos> posScratch = ThreadLocal
            .withInitial(BlockPos.MutableBlockPos::new);
    private static final AtomicBoolean CAN_RENDER_USING_SODIUM_EMBEDDIUM = new AtomicBoolean(GTCEu.Mods.isSodiumEmbeddiumLoaded());

    public static boolean maybeRenderFluidInverted(FluidType fluidType, FluidState fluidState, BlockState blockState,
                                                   BlockAndTintGetter level, BlockPos pos,
                                                   VertexConsumer vertexConsumer) {
        // this ends up calling itself intentionally; we set a state where we flip the fluid blocks' vertices
        if (INVERTED_FLUID_RENDERING.isActive()) {
            // exit early on the loop back so the fluid renderer thinks it can continue along as normal
            return false;
        }
        // config isn't enabled -> use normal rendering
        if (!ConfigHolder.INSTANCE.gameplay.lowDensityFluidsFlowUp ||
                !ConfigHolder.INSTANCE.client.lowDensityFluidsRenderUpsideDown) {
            return false;
        }
        // fluid isn't lighter than air -> use normal rendering
        if (!fluidType.isLighterThanAir()) {
            return false;
        }

        try (var $ = INVERTED_FLUID_RENDERING.setActive()) {
            BlockPos.MutableBlockPos offset = posScratch.get();
            offset.set(pos.getX() & SECTION_MASK, pos.getY() & SECTION_MASK, pos.getZ() & SECTION_MASK);

            if (CAN_RENDER_USING_SODIUM_EMBEDDIUM.get()) {

                boolean didRender = false;
                if (GTCEu.isModLoaded(GTValues.MODID_SODIUM)) {
                    didRender = GTSodiumCompat.renderFluidBlock(blockState, fluidState, level, pos, offset);
                } else if (GTCEu.isModLoaded(GTValues.MODID_EMBEDDIUM)) {
                    didRender = GTEmbeddiumCompat.renderFluidBlock(blockState, fluidState, level, pos, offset);
                }
                if (didRender) {
                    return true;
                } else {
                    CAN_RENDER_USING_SODIUM_EMBEDDIUM.set(false);
                }
            }

            // this part of the function is only ran if sodium/embeddium isn't installed or something went wrong with
            // rendering the fluid with them.
            Minecraft.getInstance().getBlockRenderer().renderLiquid(pos, level, vertexConsumer, blockState, fluidState);
        }

        return true;
    }
}

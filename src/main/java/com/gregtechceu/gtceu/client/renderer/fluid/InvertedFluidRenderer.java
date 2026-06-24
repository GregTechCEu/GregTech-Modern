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
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
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
        // heavier than air -> use normal rendering
        if (fluidType.getDensity(fluidState, level, pos) > 0) {
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
            // rendering the fluid using them.

            // create a vertex consumer that inverts the decimal part of all fluid blocks' vertices' Y coordinate.
            // this is easier to handle here rather than in LiquidBlockRendererMixin because of how vanilla MC creates
            // the vertices
            vertexConsumer = new VertexYInvertingVertexConsumer(vertexConsumer);
            Minecraft.getInstance().getBlockRenderer().renderLiquid(pos, level, vertexConsumer, blockState, fluidState);
        }

        return true;
    }

    private static class VertexYInvertingVertexConsumer extends VertexConsumerWrapper {

        public VertexYInvertingVertexConsumer(VertexConsumer parent) {
            super(parent);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            // the scratch is always up to date when VertexConsumer#addVertex is called by LiquidBlockRenderer
            float blockY = posScratch.get().getY();
            float inBlockY = y - blockY;

            // invert the added vertices' positions here
            return super.addVertex(x, blockY + (1.0f - inBlockY), z);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            // also invert the normal
            return super.setNormal(x, (1.0f - y), z);
        }
    }
}

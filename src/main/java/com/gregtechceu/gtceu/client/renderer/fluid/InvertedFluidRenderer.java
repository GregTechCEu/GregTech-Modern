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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
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
            // rendering the fluid using them.

            // create a vertex consumer that inverts the decimal part of all fluid blocks' vertices' Y coordinate.
            // this is easier to handle here rather than in LiquidBlockRendererMixin because of how vanilla MC creates
            // the vertices
            vertexConsumer = new VertexYInvertingVertexConsumer(vertexConsumer);
            Minecraft.getInstance().getBlockRenderer().renderLiquid(pos, level, vertexConsumer, blockState, fluidState);
        }

        return true;
    }

    /**
     * This class handles both inverting fluid quads' positions and their order when drawing through the vanilla path.
     *
     * <p>
     * It's done here because vanilla adds the vertices through repeated invocations of
     * {@link VertexConsumer#addVertex(float, float, float) addVertex & co}, which means it's easier handling the actual
     * vertex inversion logic ourselves after vanilla is done.
     */
    private static class VertexYInvertingVertexConsumer extends VertexConsumerWrapper {

        private final Vertex[] vertices = Vertex.uninitializedQuad();
        private int vertexCount = 0;
        private int elementsToFill = initialElementsToFill();

        public VertexYInvertingVertexConsumer(VertexConsumer parent) {
            super(parent);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            Vertex vtx = vertices[vertexCount];
            vtx.x = x;
            vtx.y = y;
            vtx.z = z;

            elementFilled(VertexFormatElement.POSITION);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            Vertex vtx = vertices[vertexCount];
            vtx.r = r;
            vtx.g = g;
            vtx.b = b;
            vtx.a = a;

            elementFilled(VertexFormatElement.COLOR);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            Vertex vtx = vertices[vertexCount];
            vtx.u = u;
            vtx.v = v;

            elementFilled(VertexFormatElement.UV0);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int bl, int sl) {
            Vertex vtx = vertices[vertexCount];
            vtx.bl = bl;
            vtx.sl = sl;

            elementFilled(VertexFormatElement.UV2);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            // also invert the normal
            Vertex vtx = vertices[vertexCount];
            vtx.nx = x;
            vtx.ny = y;
            vtx.nz = z;

            elementFilled(VertexFormatElement.NORMAL);
            return this;
        }

        private void elementFilled(VertexFormatElement element) {
            this.elementsToFill &= ~element.mask();

            // if all elements are filled
            if (elementsToFill == 0) {
                elementsToFill = initialElementsToFill();

                // increment vertex count and check if 4 have been made
                if (++vertexCount == 4) {
                    vertexCount = 0;

                    // if that's the case, write out every vertex to the original buffer *in reverse order*
                    for (int i = 0; i < 4; i++) {
                        Vertex out = vertices[(3 - i + 1) % 4];
                        //Vertex out = vertices[i];
                        out.writeTo(this.parent);
                    }
                }
            }
        }

        private static int initialElementsToFill() {
            return DefaultVertexFormat.BLOCK.getElementsMask();
        }

        private static class Vertex {
            private float x, y, z;
            private int r, g, b, a;
            private float u, v;
            private int bl, sl; // blockLight, skyLight
            private float nx, ny, nz; // normal*


            public static Vertex[] uninitializedQuad() {
                Vertex[] vertices = new Vertex[4];
                for (int i = 0; i < 4; i++) {
                    vertices[i] = new Vertex();
                }
                return vertices;
            }

            public void writeTo(VertexConsumer consumer) {
                consumer.addVertex(x, y, z)
                        .setColor(r, g, b, a)
                        .setUv(u ,v)
                        .setUv2(bl, sl)
                        .setNormal(nx, ny, nz);
            }
        }
    }
}

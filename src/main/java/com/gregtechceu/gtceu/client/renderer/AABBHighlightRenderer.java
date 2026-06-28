package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@NoArgsConstructor
public class AABBHighlightRenderer {

    public static final AABBHighlightRenderer INSTANCE = new AABBHighlightRenderer();

    private final List<AABBHighlight> highlights = new ObjectArrayList<>();

    public void tick(PoseStack stack, MultiBufferSource.BufferSource multiBuf, Camera camera) {
        if (GameRenderer.getPositionColorShader() == null || !camera.isInitialized()) return;
        Vec3 offset = camera.getPosition().reverse();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();

        stack.pushPose();
        stack.translate(offset.x, offset.y, offset.z);
        VertexConsumer buffer = multiBuf.getBuffer(GTRenderTypes.blockHighlightQuads());

        long time = System.currentTimeMillis();
        highlights.forEach(h -> h.render(buffer, stack, time));

        stack.popPose();
        multiBuf.endBatch();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public void addHighlight(AABBHighlight highlight) {
        highlights.add(highlight);
    }

    public record AABBHighlight(AABB aabb, int colorARGB, long startMillis, long durationMillis, long phaseMillis,
                                double thickness) {

        public void render(VertexConsumer buf, PoseStack pose, long currentTimeMillis) {
            if (currentTimeMillis - startMillis >= durationMillis) {
                this.remove();
                return;
            }
            if (currentTimeMillis < startMillis) return;
            if ((currentTimeMillis - startMillis) / phaseMillis % 2 == 1) return;

            RenderBufferHelper.renderAABBOutline(buf, pose, aabb(), thickness, colorARGB());
        }

        public void remove() {
            // FIXME CME inbound here!
            AABBHighlightRenderer.INSTANCE.highlights.remove(this);
        }
    }

    public static AABBHighlightBuilder builder() {
        return new AABBHighlightBuilder();
    }

    @Accessors(chain = true, fluent = true)
    public static class AABBHighlightBuilder {

        @Setter
        private AABB aabb = null;
        @Setter
        private int colorARGB = 0xFFFFFFFF;
        @Setter
        private long startMillis = -1;
        @Setter
        private long durationMillis = 10000;
        @Setter
        private long phaseMillis = 300;
        @Setter
        private double thickness = 0.01D;

        @Tolerate
        public AABBHighlightBuilder aabb(BlockPos pos) {
            this.aabb = new AABB(pos);
            return this;
        }

        public AABBHighlightBuilder colorARGB(int alpha, int red, int green, int blue) {
            this.colorARGB = FastColor.ARGB32.color(alpha, red, green, blue);
            return this;
        }

        public AABBHighlight build() {
            if (aabb == null) {
                throw new IllegalArgumentException("AABB can't be null in AABBHighlightBuilder");
            }
            if (startMillis == -1) {
                startMillis = System.currentTimeMillis();
            }
            return new AABBHighlight(aabb, colorARGB, startMillis, durationMillis, phaseMillis, thickness);
        }
    }
}

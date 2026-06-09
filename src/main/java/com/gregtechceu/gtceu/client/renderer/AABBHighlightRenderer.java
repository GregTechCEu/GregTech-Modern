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
        var buf = multiBuf.getBuffer(GTRenderTypes.blockHighlightQuads());

        highlights.forEach(h -> h.renderTick(buf, stack));

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

    public record AABBHighlight(AABB aabb, int colorARGB) {

        public AABBHighlight(AABB aabb) {
            this(aabb, FastColor.ARGB32.color(255, 255, 255, 255));
        }

        public AABBHighlight(BlockPos pos) {
            this(new AABB(pos));
        }

        public AABBHighlight(BlockPos pos, int colorARGB) {
            this(new AABB(pos), colorARGB);
        }

        public void renderTick(VertexConsumer buf, PoseStack pose) {
            RenderBufferHelper.renderAABBOutline(buf, pose, aabb(), 0.01, colorARGB());
        }

        public void remove() {
            AABBHighlightRenderer.INSTANCE.highlights.remove(this);
        }
    }
}

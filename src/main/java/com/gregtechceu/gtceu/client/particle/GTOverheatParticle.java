package com.gregtechceu.gtceu.client.particle;

import com.gregtechceu.gtceu.client.bloom.EffectRenderContext;
import com.gregtechceu.gtceu.client.bloom.IRenderSetup;
import com.gregtechceu.gtceu.client.bloom.particle.GTBloomParticle;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

/**
 * @author brachy84
 */
public class GTOverheatParticle extends GTBloomParticle {

    /**
     * <a href="http://www.vendian.org/mncharity/dir3/blackbody/">Source</a>
     */
    private static final int[] blackBodyColors = {
            0xFF3300, // 1000 K
            0xFF5300, // 1200 K
            0xFF6500, // 1400 K
            0xFF7300, // 1600 K
            0xFF7E00, // 1800 K
            0xFF8912, // 2000 K
            0xFF932C, // 2200 K
            0xFF9D3F, // 2400 K
            0xffa54f, // 2600 K
            0xffad5e, // 2800 K
            0xffb46b, // 3000 K
            0xffbb78, // 3200 K
            0xffc184, // 3400 K
            0xffc78f, // 3600 K
            0xffcc99, // 3800 K
            0xffd1a3, // 4000 K
            0xffd5ad, // 4200 K
            0xffd9b6, // 4400 K
            0xffddbe, // 4600 K
            0xffe1c6, // 4800 K
            0xffe4ce, // 5000 K
            0xffe8d5, // 5200 K
            0xffebdc, // 5400 K
            0xffeee3, // 5600 K
            0xfff0e9, // 5800 K
            0xfff3ef, // 6000 K
            0xfff5f5, // 6200 K
            0xfff8fb, // 6400 K
            0xfef9ff, // 6600 K
            0xf9f6ff, // 6800 K
            0xf5f3ff, // 7000 K
            0xf0f1ff, // 7200 K
            0xedefff, // 7400 K
            0xe9edff, // 7600 K
            0xe6ebff, // 7800 K
            0xe3e9ff, // 8000 K
            0xe0e7ff, // 8200 K
            0xdde6ff, // 8400 K
            0xdae4ff, // 8600 K
            0xd8e3ff, // 8800 K
            0xd6e1ff, // 9000 K
            0xd3e0ff, // 9200 K
            0xd1dfff, // 9400 K
            0xcfddff, // 9600 K
            0xcedcff, // 9800 K
            0xccdbff, // 10000 K
            0xcadaff, // 10200 K
            0xc9d9ff, // 10400 K
            0xc7d8ff, // 10600 K
            0xc6d8ff, // 10800 K
            0xc4d7ff, // 11000 K
            0xc3d6ff, // 11200 K
            0xc2d5ff, // 11400 K
            0xc1d4ff, // 11600 K
            0xc0d4ff, // 11800 K
            0xbfd3ff, // 12000 K
            0xbed2ff, // 12200 K
            0xbdd2ff, // 12400 K
            0xbcd1ff, // 12600 K
            0xbbd1ff, // 12800 K
            0xbad0ff, // 13000 K
            0xb9d0ff, // 13200 K
            0xb8cfff, // 13400 K
            0xb7cfff, // 13600 K
            0xb7ceff, // 13800 K
            0xb6ceff, // 14000 K
            0xb5cdff, // 14200 K
            0xb5cdff, // 14400 K
            0xb4ccff, // 14600 K
            0xb3ccff, // 14800 K
            0xb3ccff, // 15000 K
            0xb2cbff, // 15200 K
            0xb2cbff, // 15400 K
            0xb1caff, // 15600 K
            0xb1caff, // 15800 K
            0xb0caff, // 16000 K
            0xafc9ff, // 16200 K
            0xafc9ff, // 16400 K
            0xafc9ff, // 16600 K
            0xaec9ff, // 16800 K
            0xaec8ff, // 17000 K
            0xadc8ff, // 17200 K
            0xadc8ff, // 17400 K
            0xacc7ff, // 17600 K
            0xacc7ff, // 17800 K
            0xacc7ff, // 18000 K
            0xabc7ff, // 18200 K
            0xabc6ff, // 18400 K
            0xaac6ff, // 18600 K
            0xaac6ff, // 18800 K
            0xaac6ff, // 19000 K
            0xa9c6ff, // 19200 K
            0xa9c5ff, // 19400 K
            0xa9c5ff, // 19600 K
            0xa9c5ff, // 19800 K
            0xa8c5ff, // 20000 K
            // color doesn't really change onwards
    };

    public static int getBlackBodyColor(int temperature) {
        if (temperature < 1000)
            return blackBodyColors[0];
        int index = (temperature - 1000) / 200;
        if (index >= blackBodyColors.length - 1)
            return blackBodyColors[blackBodyColors.length - 1];
        int color = blackBodyColors[index];
        return RenderUtil.interpolateColor(color, blackBodyColors[index + 1], temperature % 200 / 200f);
    }

    private final CableBlockEntity blockEntity;

    private final int meltTemp;
    private int temperature;
    private final boolean insulated;

    private VoxelShape pipeShape;
    private AABB pipeBounds;

    private float alpha = 0.0f;
    private int color = blackBodyColors[0];

    public GTOverheatParticle(CableBlockEntity blockEntity, int meltTemp, boolean insulated) {
        super(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ());
        this.blockEntity = blockEntity;
        this.meltTemp = meltTemp;
        this.setTemperature(blockEntity.getTemperature());
        this.insulated = insulated;

        this.pipeShape = blockEntity.getBlockState().getVisualShape(blockEntity.getLevel(), blockEntity.getBlockPos(),
                CollisionContext.empty());
        this.pipeBounds = pipeShape.bounds().inflate(0.001).move(posX, posY, posZ);
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        if (temperature <= blockEntity.getDefaultTemp() || temperature > meltTemp) {
            setExpired();
            return;
        }
        if (temperature < 300) {
            alpha = 0.0f;
        } else if (temperature < 600) {
            alpha = 0.16f * (temperature - 300f) / 300f;
        } else if (temperature < 1000) {
            alpha = 0.8f * (temperature - 500f) / 500f;
        } else {
            alpha = 0.8f;
        }
        color = getBlackBodyColor(temperature);
    }

    @Override
    public void onUpdate() {
        // if this isn't the block entity's particle, remove both
        if (blockEntity.isRemoved() || !blockEntity.isParticleAlive()) {
            setExpired();
            blockEntity.killParticle();
            return;
        }
        // update pipeShape every tick so it doesn't desync if the pipe is disconnected
        pipeShape = blockEntity.getBlockState().getVisualShape(blockEntity.getLevel(), blockEntity.getBlockPos(),
                CollisionContext.empty());
        pipeBounds = pipeShape.bounds().inflate(0.001).move(posX, posY, posZ);

        if (temperature > 400 && blockEntity.getLevel().random.nextFloat() < 0.04f) {
            spawnSmoke();
        }
    }

    private void spawnSmoke() {
        BlockPos pos = blockEntity.getBlockPos();
        float xPos = pos.getX() + 0.5F;
        float yPos = pos.getY() + 0.9F;
        float zPos = pos.getZ() + 0.5F;

        float ySpd = 0.3F + 0.1F * blockEntity.getLevel().random.nextFloat();
        blockEntity.getLevel().addParticle(ParticleTypes.LARGE_SMOKE, xPos, yPos, zPos, 0, ySpd, 0);
    }

    @Override
    public String toString() {
        return "GTOverheatParticle{" +
                "blockEntity=" + blockEntity +
                ", pipeShape=" + pipeShape +
                ", insulated=" + insulated +
                ", alpha=" + alpha +
                ", color=" + color +
                '}';
    }

    @Override
    public boolean shouldRender(EffectRenderContext context) {
        return this.shouldRenderBloomEffect(context);
    }

    @Override
    public boolean shouldRenderBloomEffect(EffectRenderContext context) {
        if (this.insulated) return false;
        return context.frustum().isVisible(pipeBounds);
    }

    @Override
    public IRenderSetup getRenderSetup() {
        return NO_BLOOM_SETUP;
    }

    @Override
    protected IRenderSetup getBloomRenderSetup() {
        return BLOOM_SETUP;
    }

    @Override
    public void renderParticle(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context) {
        renderBloomEffect(poseStack, buffer, context);
    }

    @Override
    public void renderBloomEffect(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context) {
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;

        poseStack.pushPose();
        poseStack.translate(posX, posY, posZ);
        pipeShape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            RenderBufferHelper.renderColorCube(poseStack, buffer,
                    (float) x1 - 0.001f, (float) y1 - 0.001f, (float) z1 - 0.001f,
                    (float) x2 + 0.001f, (float) y2 + 0.001f, (float) z2 + 0.001f,
                    red, green, blue, alpha, true);
        });
        poseStack.popPose();
    }

    private static final IRenderSetup NO_BLOOM_SETUP = new IRenderSetup() {

        @Override
        @OnlyIn(Dist.CLIENT)
        public BufferBuilder preDraw() {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void postDraw(BufferBuilder buffer) {
            IRenderSetup.super.postDraw(buffer);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    };

    private static final IRenderSetup BLOOM_SETUP = new IRenderSetup() {

        @Override
        @OnlyIn(Dist.CLIENT)
        public BufferBuilder preDraw() {
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }
    };
}

package com.gregtechceu.gtceu.client.mui.schemarenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.lighting.LightEngine;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

/**
 * A version of {@link LightTexture} that's independent of the current client level.
 * <p>
 * Most of the class is based on the vanilla light texture, and the remaining parts are from Applied energistics 2's
 * <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/643dfe2e7e16dac48192d85305d35e2e74a64fb0/src/main/java/appeng/client/guidebook/scene/GuidebookLightmap.java">GuidebookLightmap</a>
 * (LGPLv3)
 * </p>
 */
public class DummyLightTexture implements AutoCloseable {

    private static final Vector3fc THREE_FOURTHS = new Vector3f(0.75f, 0.75f, 0.75f);

    private final DynamicTexture lightmapTexture;
    private final NativeImage lightmapPixels;

    public DummyLightTexture() {
        lightmapTexture = new DynamicTexture(16, 16, false);
        lightmapPixels = Objects.requireNonNull(lightmapTexture.getPixels());
        lightmapPixels.fillRect(0, 0, 16, 16, -1);
        lightmapTexture.upload();
    }

    public static float getSkyDarken(LevelTimeAccess level, float partialTick) {
        var f = level.getTimeOfDay(partialTick);
        var g = 1.0f - (Mth.cos(f * Mth.TWO_PI) * 2.0f + 0.2F);
        g = Mth.clamp(g, 0.0f, 1.0f);
        g = 1.0f - g;
        return 0.2f + g * 0.8f;
    }

    public void update(LevelTimeAccess level) {
        float skyDarkness = getSkyDarken(level, 1.0f);
        float partDarken = Mth.lerp(0.35f, skyDarkness, 1.0f);
        float skyBrightMul = skyDarkness * 0.95f + 0.05f;
        float blockBrightMul = 1.5f;
        float gamma = Minecraft.getInstance().options.gamma().get().floatValue();

        Vector3f color = new Vector3f();

        for (int skyLightLvl = 0; skyLightLvl <= LightEngine.MAX_LEVEL; ++skyLightLvl) {
            for (int blockLightLvl = 0; blockLightLvl <= LightEngine.MAX_LEVEL; ++blockLightLvl) {
                float skyLuma = LightTexture.getBrightness(level.dimensionType(), skyLightLvl) * skyBrightMul;
                float blockLuma = LightTexture.getBrightness(level.dimensionType(), blockLightLvl) * blockBrightMul;

                color.set(blockLuma,
                        blockLuma * rescale(rescale(blockLuma)),
                        blockLuma * blockLuma * rescale(blockLuma));

                Vector3f skyColor = new Vector3f(partDarken, partDarken, 1.0f).mul(skyLuma);
                color.add(skyColor);
                color.lerp(THREE_FOURTHS, 0.04f);

                Vector3f noGammaColor = new Vector3f(notGamma(color.x), notGamma(color.y), notGamma(color.z));
                color.lerp(noGammaColor, Math.max(0.0f, gamma));
                color.lerp(THREE_FOURTHS, 0.04f);

                clampColor(color);
                color.mul(255.0f);

                int r = (int) color.x();
                int g = (int) color.y();
                int b = (int) color.z();
                lightmapPixels.setPixelRGBA(blockLightLvl, skyLightLvl, 0xFF000000 | b << 16 | g << 8 | r);
            }
        }

        lightmapTexture.upload();
    }

    private static void clampColor(Vector3f color) {
        color.set(Mth.clamp(color.x, 0.0f, 1.0f),
                Mth.clamp(color.y, 0.0f, 1.0f),
                Mth.clamp(color.z, 0.0f, 1.0f));
    }

    private float notGamma(float value) {
        float f = 1.0f - value;
        return 1.0f - f * f * f * f;
    }

    // map a 0.0-1.0 range to 0.4-1.0
    private float rescale(float value) {
        return value * 0.6f + 0.4f;
    }

    public void bind() {
        RenderSystem.setShaderTexture(2, lightmapTexture.getId());
        lightmapTexture.bind();
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public void close() throws Exception {
        lightmapTexture.close();
    }
}

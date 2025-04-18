package com.gregtechceu.gtceu.client.particle;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class MufflerParticle extends TextureSheetParticle {

    private static final int COLOR = 0x1E1C1D;

    private final SpriteSet sprites;

    protected MufflerParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed,
                              double zSpeed, SimpleParticleType options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        this.xd *= 0.1F;
        this.yd *= 0.5F;
        this.zd *= 0.1F;
        float colorMultiplier = this.random.nextFloat() * 4.4F + 1.3F;
        this.rCol = this.randomizeColor(FastColor.ARGB32.red(COLOR) / 255f, colorMultiplier);
        this.gCol = this.randomizeColor(FastColor.ARGB32.green(COLOR) / 255f, colorMultiplier);
        this.bCol = this.randomizeColor(FastColor.ARGB32.blue(COLOR) / 255f, colorMultiplier + 1);
        this.quadSize *= 1.5F;
        this.lifetime = (int) (lifetime / (level.random.nextFloat() * 0.8 + 0.2) * 2);
        this.setSpriteFromAge(sprites);
        this.hasPhysics = true;
    }

    protected float randomizeColor(float coordMultiplier, float multiplier) {
        return (this.random.nextFloat() * 0.2F + 0.8F) * coordMultiplier * multiplier;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize * Mth.clamp(((float) this.age + scaleFactor) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setSpriteFromAge(this.sprites);
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.yd -= (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }
        } else {
            this.remove();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            RandomSource randomSource = level.random;
            ySpeed += (double) randomSource.nextFloat() * -1.9 * (double) randomSource.nextFloat() * 0.1 * 5.0;
            return new MufflerParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options, this.sprites);
        }
    }
}

package com.gregtechceu.gtceu.client.particle;

import com.gregtechceu.gtceu.common.particle.HazardParticleOptions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HazardParticle extends SingleQuadParticle {

    private final SpriteSet sprites;

    protected HazardParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed,
                             double zSpeed, HazardParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, null);
        this.friction = 0.96F;
        this.gravity = 0.0125F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        float colorMultiplier = this.random.nextFloat() * 0.4F + 0.6F;
        this.rCol = this.randomizeColor(ARGB.red(options.color()) / 255f, colorMultiplier);
        this.gCol = this.randomizeColor(ARGB.green(options.color()) / 255f, colorMultiplier);
        this.bCol = this.randomizeColor(ARGB.blue(options.color()) / 255f, colorMultiplier);
        this.quadSize *= 0.75F * options.scale();
        this.lifetime = (int) (lifetime / (level.getRandom().nextFloat() * 0.8 + 0.2) * 2);
        this.setSpriteFromAge(sprites);
        this.hasPhysics = false;
    }

    protected float randomizeColor(float coordMultiplier, float multiplier) {
        return (this.random.nextFloat() * 0.2F + 0.8F) * coordMultiplier * multiplier;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize * Mth.clamp(((float) this.age + scaleFactor) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<HazardParticleOptions> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(HazardParticleOptions options, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, RandomSource randomSource) {
            xSpeed += (double) randomSource.nextFloat() * -1.9 * (double) randomSource.nextFloat() * 0.1;
            ySpeed += (double) randomSource.nextFloat() * -0.5 * (double) randomSource.nextFloat() * 0.1 * 5.0;
            zSpeed += (double) randomSource.nextFloat() * -1.9 * (double) randomSource.nextFloat() * 0.1;
            return new HazardParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options, this.sprites);
        }
    }
}

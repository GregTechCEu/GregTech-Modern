package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.core.mixins.PrimedTntAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GTExplosiveEntity extends PrimedTnt {
    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type, Level level, double x, double y, double z, @Nullable LivingEntity owner) {
        this(type, level);
        this.setPos(x, y, z);
        double d = level.random.nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        ((PrimedTntAccessor)this).setOwner(owner);
    }

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type, Level world) {
        super(type, world);
    }

    /**
     * @return The strength of the explosive.
     */
    protected abstract float getStrength();

    /**
     * @return Whether to drop all blocks, or use default logic
     */
    public abstract boolean dropsAllBlocks();

    /**
     * @return The range of the explosive, if {@link #dropsAllBlocks} is true.
     */
    protected int getRange() {
        return 2;
    }

    /**
     * @return The block state of the block this explosion entity is created by.
     */
    public abstract @NotNull BlockState getExplosiveState();

    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.xOld = this.getZ();
        Vec3 motion = this.getDeltaMovement();
        if (!this.isNoGravity()) {
            motion = new Vec3(motion.x, motion.y - 0.03999999910593033D, motion.z);
        }

        this.move(MoverType.SELF, motion);
        motion = new Vec3(motion.x * 0.9800000190734863D, motion.y * 0.9800000190734863D, motion.z * 0.9800000190734863D);
        if (this.onGround()) {
            motion = new Vec3(motion.x * 0.699999988079071D, motion.y * -0.5D, motion.z * 0.699999988079071D);
        }
        this.setDeltaMovement(motion);

        setFuse(this.getFuse() - 1);
        if (this.getFuse() <= 0) {
            this.kill();
            if (!this.level().isClientSide) {
                this.explodeTNT();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D,
                0.0D);
        }
    }

    protected void explodeTNT() {
        explode(level(), this, this.getX(), this.getY() + (double) (this.getBbHeight() / 16.0F), this.getZ(),
            getStrength(), dropsAllBlocks());
    }

    protected void explode(
        Level level,
        @Nullable Entity source,
        double x,
        double y,
        double z,
        float radius,
        boolean dropBlocks) {
        Explosion explosion = new Explosion(
            level, source,
            x, y, z,
            radius,
            false,
            dropBlocks ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY
        );
        if (!ForgeEventFactory.onExplosionStart(level, explosion)) {
            explosion.explode();
            explosion.finalizeExplosion(true);
        }
    }
}
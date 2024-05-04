package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DynamiteEntity extends ThrowableItemProjectile {

    private int ticksUntilExplosion;

    public DynamiteEntity(EntityType<DynamiteEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public DynamiteEntity(double x, double y, double z, Level worldIn) {
        super(GTEntityTypes.DYNAMITE.get(), x, y, z, worldIn);
    }

    public DynamiteEntity(LivingEntity throwerIn, Level worldIn) {
        super(GTEntityTypes.DYNAMITE.get(), throwerIn, worldIn);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        ticksUntilExplosion = 80 + level().random.nextInt(60);
    }

    @Override
    @NotNull
    protected Item getDefaultItem() {
        return GTItems.DYNAMITE.get();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (result.getDirection() == Direction.UP) {
            setOnGround(true);
        } else {
            Vec3 delta = this.getDeltaMovement();
            if (result.getDirection().getAxis() == Direction.Axis.Z) {
                this.setDeltaMovement(delta.x, delta.y, 0);
            } else if (result.getDirection().getAxis() == Direction.Axis.X) {
                this.setDeltaMovement(0, delta.y, delta.z);
            } else if (result.getDirection().getAxis() == Direction.Axis.Y) {
                this.setDeltaMovement(delta.x, 0, delta.z);
            }
        }
    }

    @Override
    public void tick() {
        ticksUntilExplosion--;

        if (level().random.nextInt(3) == 2) {
            level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), -this.getDeltaMovement().x * 0.05f,
                this.onGround() ? 0.05f : -this.getDeltaMovement().y * 0.05f, -this.getDeltaMovement().z * 0.05f);
        }

        if (ticksUntilExplosion < 0 && !level().isClientSide) {
            Entity thrower = getOwner();
            level().explode(thrower == null ? this : thrower, this.getX(), this.getY(), this.getZ(), 1.5f, Level.ExplosionInteraction.TNT);
            this.kill();
            return;
        }

        super.tick();

        Vec3 motion = getDeltaMovement();
        float f = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        this.setYRot((float) (Mth.atan2(motion.z, motion.z) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(motion.y, f) * (180D / Math.PI)));

        while (this.getXRot() - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while (this.getXRot() - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while (this.getYRot() - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while (this.getYRot() - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        this.setXRot(this.xRotO + (this.getXRot() - this.xRotO) * 0.2F);
        this.setYRot(this.yRotO + (this.getYRot() - this.yRotO) * 0.2F);
    }
}
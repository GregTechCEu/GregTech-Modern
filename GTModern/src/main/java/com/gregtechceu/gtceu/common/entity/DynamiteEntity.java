package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.particles.ParticleTypes;
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
        Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
    }

    @Override
    public void tick() {
        ticksUntilExplosion--;

        if (level().random.nextInt(3) == 2) {
            level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(),
                    -this.getDeltaMovement().x * 0.05f,
                    this.onGround() ? 0.05f : -this.getDeltaMovement().y * 0.05f, -this.getDeltaMovement().z * 0.05f);
        }

        if (ticksUntilExplosion < 0 && !level().isClientSide) {
            Entity thrower = getOwner();
            level().explode(thrower == null ? this : thrower, this.getX(), this.getY(), this.getZ(), 1.5f,
                    Level.ExplosionInteraction.TNT);
            this.discard();
            return;
        }

        super.tick();
    }
}

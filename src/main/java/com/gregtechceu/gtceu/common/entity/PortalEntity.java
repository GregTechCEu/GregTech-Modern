package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.GTSoundTypes;
import com.gregtechceu.gtceu.utils.GTTeleporter;
import com.gregtechceu.gtceu.utils.TeleportHandler;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

public class PortalEntity extends Entity {

    private double targetX = 0;
    private double targetY = 0;
    private double targetZ = 0;

    private int targetDim = 0;

    @Getter
    private int timeToDespawn = 200;


    public PortalEntity(Level world) {
        super(GTEntityTypes.PORTAL.get(), world);
        boardingCooldown = -1;
    }

    public PortalEntity(Level world, double x, double y, double z) {
        super(GTEntityTypes.PORTAL.get(), world);
        this.setPos(x, y, z);
        boardingCooldown = -1;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.targetX = compound.getDouble("targetX");
        this.targetY = compound.getDouble("targetY");
        this.targetZ = compound.getDouble("targetZ");
        this.targetDim = compound.getInt("targetDim");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putDouble("targetX", this.targetX);
        compound.putDouble("targetY", this.targetY);
        compound.putDouble("targetZ", this.targetZ);
        compound.putInt("targetDim", this.targetDim);
    }

    @Override
    public void tick() {
        if(timeToDespawn < 0) {
            setRemoved(RemovalReason.KILLED);
        }

        if(!this.level().isClientSide) {
            setSharedFlag(6, this.isCurrentlyGlowing());
        }

        if(timeToDespawn == 200) {
            playSound(GTSoundEntries.PORTAL_OPENING.getMainEvent(), 0.7f, 1.0f);
        } else if(timeToDespawn == 10) {
            playSound(GTSoundEntries.PORTAL_CLOSING.getMainEvent(), 0.7f, 1.0f);
        }

        if(!this.level().isClientSide) {
            List<Entity> entities = level().getEntities(this, this.getBoundingBox(), null);
            for(Entity entity : entities) {
                if(!(entity instanceof PortalEntity)) {
                    GTTeleporter teleporter = new GTTeleporter(TeleportHandler.)
                }
            }
        }
        --timeToDespawn;
    }

    @Override
    public void setRot(float yRot, float xRot) {
        super.setRot(yRot, xRot);
    }

    public void setTargetCoordinates(int dim, double x, double y, double z) {
        this.targetDim = dim;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    public boolean isOpening() {
        return timeToDespawn >= 190;
    }

    public boolean isClosing() {
        return timeToDespawn <= 10;
    }
}

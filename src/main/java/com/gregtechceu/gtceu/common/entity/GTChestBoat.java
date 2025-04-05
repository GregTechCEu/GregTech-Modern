package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GTChestBoat extends ChestBoat {

    public GTChestBoat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public GTChestBoat(Level level, double x, double y, double z) {
        super(GTEntityTypes.CHEST_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("Type", getBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Type")) {
            entityData.set(DATA_ID_TYPE, GTBoat.BoatType.byName(compound.getString("Type")).ordinal());
        }
    }

    @Override
    public Item getDropItem() {
        return switch (GTBoat.BoatType.byId(this.entityData.get(DATA_ID_TYPE))) {
            case RUBBER -> GTItems.RUBBER_CHEST_BOAT.get();
            case TREATED_WOOD -> GTItems.TREATED_WOOD_CHEST_BOAT.get();
        };
    }

    public void setBoatType(GTBoat.BoatType type) {
        this.entityData.set(DATA_ID_TYPE, type.ordinal());
    }

    public GTBoat.BoatType getBoatType() {
        return GTBoat.BoatType.byId(entityData.get(DATA_ID_TYPE));
    }

    @Override
    public void setVariant(Type variant) {}

    @Override
    public Type getVariant() {
        return Type.OAK;
    }
}

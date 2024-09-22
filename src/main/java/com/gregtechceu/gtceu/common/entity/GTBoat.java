package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class GTBoat extends Boat {

    public GTBoat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public GTBoat(Level level, double x, double y, double z) {
        super(GTEntityTypes.BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return super.getCustomName();
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
            entityData.set(DATA_ID_TYPE, BoatType.byName(compound.getString("Type")).ordinal());
        }
    }

    @Override
    public Item getDropItem() {
        return switch (BoatType.byId(this.entityData.get(DATA_ID_TYPE))) {
            case RUBBER -> GTItems.RUBBER_BOAT.get();
            case TREATED_WOOD -> GTItems.TREATED_WOOD_BOAT.get();
        };
    }

    public void setBoatType(BoatType type) {
        this.entityData.set(DATA_ID_TYPE, type.ordinal());
    }

    public BoatType getBoatType() {
        return BoatType.byId(entityData.get(DATA_ID_TYPE));
    }

    @Override
    public void setVariant(Type variant) {}

    @Override
    public Type getVariant() {
        return Type.OAK;
    }

    public enum BoatType {

        RUBBER("rubber", GTBlocks.RUBBER_PLANK.get()),
        TREATED_WOOD("treated", GTBlocks.TREATED_WOOD_PLANK.get());

        private final String name;
        private final Block planks;

        private static final BoatType[] VALUES = values();

        private BoatType(String name, Block planks) {
            this.name = name;
            this.planks = planks;
        }

        public String getName() {
            return this.name;
        }

        public Block getPlanks() {
            return this.planks;
        }

        public String toString() {
            return this.name;
        }

        /**
         * Get a boat type by its enum ordinal
         */
        public static BoatType byId(int id) {
            if (id < 0 || id >= VALUES.length) id = 0;
            return VALUES[id];
        }

        public static BoatType byName(String name) {
            return Arrays.stream(VALUES).filter(type -> type.getName().equals(name)).findFirst().orElse(VALUES[0]);
        }
    }
}

package com.gregtechceu.gtceu.common.machine.owner;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;

import java.util.function.BooleanSupplier;

public sealed interface IMachineOwner permits GTOwner, ArgonautsOwner, FTBOwner {

    void save(CompoundTag tag);

    void load(CompoundTag tag);

    MachineOwnerType type();

    static IMachineOwner create(CompoundTag tag) {
        MachineOwnerType type = MachineOwnerType.VALUES[tag.getInt("type")];
        if (!type.isAvailable()) {
            GTCEu.LOGGER.warn("Machine ownership system: {} is not available", type.name());
            return null;
        }
        IMachineOwner owner = switch (type) {
            case GT -> new GTOwner();
            case FTB -> new FTBOwner();
            case ARGONAUTS -> new ArgonautsOwner();
        };
        owner.load(tag);
        return owner;
    }

    static CompoundTag write(IMachineOwner owner) {
        var tag = new CompoundTag();
        tag.putInt("type", owner.type().ordinal());
        owner.save(tag);
        return tag;
    }

    enum MachineOwnerType {

        GT,
        FTB(GTCEu::isFTBTeamsLoaded, "FTB Teams"),
        ARGONAUTS(GTCEu::isArgonautsLoaded, "Argonauts Guild");

        public static final MachineOwnerType[] VALUES = values();

        private BooleanSupplier availabilitySupplier;
        private boolean available;

        @Getter
        private final String name;

        private MachineOwnerType(BooleanSupplier availabilitySupplier, String name) {
            this.availabilitySupplier = availabilitySupplier;
            this.name = name;
        }

        private MachineOwnerType() {
            this.available = true;
            this.name = "Player";
        }

        public boolean isAvailable() {
            if (availabilitySupplier != null) {
                this.available = availabilitySupplier.getAsBoolean();
                this.availabilitySupplier = null;
            }
            return available;
        }
    }
}

package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualRedstone extends VirtualEntry {

    private static final String MEMBERS_KEY = "members";

    @Setter
    @Getter
    private Map<UUID, Short> members = new HashMap<>();

    public int getSignal() {
        return members.values().stream().max(Short::compareTo).orElse((short) 0);
    }

    public UUID addMember() {
        UUID uuid = UUID.randomUUID();
        members.put(uuid, (short) 0);
        return uuid;
    }

    public void setSignal(UUID uuid, int signal) {
        if (!members.containsKey(uuid)) return;
        members.put(uuid, (short) signal);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    @Override
    public EntryTypes<? extends VirtualEntry> getType() {
        return EntryTypes.ENDER_REDSTONE;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        CompoundTag tag2 = new CompoundTag();
        for (UUID uuid : members.keySet()) tag2.putShort(uuid.toString(), members.get(uuid));
        tag.put(MEMBERS_KEY, tag2);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        CompoundTag tag = nbt.getCompound(MEMBERS_KEY);
        for (String uuid : tag.getAllKeys()) {
            members.put(UUID.fromString(uuid), tag.getShort(uuid));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualRedstone other)) return false;
        return other.members == this.members;
    }

    @Override
    public boolean canRemove() {
        return super.canRemove() && members.isEmpty();
    }
}

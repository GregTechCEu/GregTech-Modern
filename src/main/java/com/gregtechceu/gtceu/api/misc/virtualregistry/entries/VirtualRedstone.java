package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;

import net.minecraft.nbt.CompoundTag;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import lombok.Getter;

import java.util.UUID;

public class VirtualRedstone extends VirtualEntry {

    private static final String MEMBERS_KEY = "members";

    @Getter
    private final Object2ShortMap<UUID> members = new Object2ShortOpenHashMap<>();

    public VirtualRedstone() {}

    public int getSignal() {
        return members.values().intStream().max().orElse(0);
    }

    public void addMember(UUID uuid) {
        members.put(uuid, (short) 0);
    }

    public void setSignal(UUID uuid, int signal) {
        if (!members.containsKey(uuid)) return;
        members.put(uuid, (short) signal);
    }

    public void removeMember(UUID uuid) {
        members.removeShort(uuid);
    }

    @Override
    public EntryTypes<? extends VirtualEntry> getType() {
        return EntryTypes.ENDER_REDSTONE;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        CompoundTag tag2 = new CompoundTag();
        for (var entry : members.object2ShortEntrySet())
            tag2.putShort(entry.getKey().toString(), entry.getShortValue());
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

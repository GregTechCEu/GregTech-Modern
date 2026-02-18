package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.common.cover.ender.EnderRedstoneLinkCover;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import lombok.Getter;

public class VirtualRedstone extends VirtualEntry {

    @Getter
    private final Object2ShortMap<EnderRedstoneLinkCover> members = new Object2ShortOpenHashMap<>();

    public VirtualRedstone() {}

    public int getSignal() {
        return members.values().intStream().max().orElse(0);
    }

    public void addMember(EnderRedstoneLinkCover cover) {
        members.put(cover, (short) 0);
    }

    public void setSignal(EnderRedstoneLinkCover cover, int signal) {
        if (!members.containsKey(cover)) return;
        members.put(cover, (short) signal);
    }

    public void removeMember(EnderRedstoneLinkCover cover) {
        members.removeShort(cover);
    }

    @Override
    public EntryTypes<? extends VirtualEntry> getType() {
        return EntryTypes.ENDER_REDSTONE;
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

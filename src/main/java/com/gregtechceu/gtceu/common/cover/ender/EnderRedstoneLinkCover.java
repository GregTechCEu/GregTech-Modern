package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualRedstone;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;

import net.minecraft.core.Direction;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EnderRedstoneLinkCover extends AbstractEnderLinkCover<VirtualRedstone> {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    private @Nullable VirtualRedstone storage = new VirtualRedstone();

    public EnderRedstoneLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    protected @Nullable VirtualRedstone getEntry() {
        return storage;
    }

    @Override
    protected void setEntry(VirtualEntry entry) {
        if (storage != null) storage.removeMember(this);
        storage = (VirtualRedstone) entry;
        storage.addMember(this);
        syncDataHolder.markClientSyncFieldDirty("storage");
    }

    @Override
    protected EntryTypes<VirtualRedstone> getEntryType() {
        return EntryTypes.ENDER_REDSTONE;
    }

    @Override
    protected void transfer() {
        switch (io) {
            case IN -> Objects.requireNonNull(storage).setSignal(this, getSignalInput());
            case OUT -> setRedstoneSignalOutput(Objects.requireNonNull(storage).getSignal());
        }
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    protected IWidget createVirtualEntryWidget(PanelSyncManager manager, VirtualEntry entry, int w, int h, int index) {
        return new ParentWidget<>().size(w, h);
    }

    @Override
    public void onRemoved() {
        if (storage != null) storage.removeMember(this);
        super.onRemoved();
    }

    protected int getSignalInput() {
        return coverHolder.getLevel().getSignal(coverHolder.getBlockPos().relative(attachedSide),
                attachedSide.getOpposite());
    }
}

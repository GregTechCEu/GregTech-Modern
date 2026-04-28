package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageCover extends CoverBehavior implements IUICover {

    @SaveField
    @SyncToClient
    public final CustomItemStackHandler inventory;
    private final int SIZE = 18;

    public StorageCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverableView,
                        @NotNull Direction attachedSide) {
        super(definition, coverableView, attachedSide);
        inventory = new CustomItemStackHandler(SIZE) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        inventory.setOnContentsChanged(() -> syncDataHolder.markClientSyncFieldDirty("inventory"));
    }

    @Override
    @NotNull
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        for (int slot = 0; slot < SIZE; slot++) {
            list.add(inventory.getStackInSlot(slot));
        }
        return list;
    }

    @Override
    public boolean canAttach() {
        if (!(coverHolder instanceof MachineCoverContainer)) return false;
        for (var dir : Direction.values()) {
            if (coverHolder.hasCover(dir) && coverHolder.getCoverAtSide(dir) instanceof StorageCover)
                return false;
        }
        return super.canAttach();
    }

    @Override
    public Object createUIWidget() {
        return StorageCoverUI.createUIWidget(this);
    }

    String getUITitle() {
        return "cover.storage.title";
    }

    @Override
    public @Nullable IFancyConfigurator getConfigurator() {
        return StorageCoverUI.createConfigurator(this);
    }
}

package com.gregtechceu.gtceu.api.item.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IModularItem {

    /**
     * @return the default module slot configuration of this item
     */
    List<ItemModuleSlot> getSlots();

    void clearModules();

    @Nullable
    AppliedItemModule getModuleInSlot(int slot);

    @NotNull
    List<AppliedItemModule> getAppliedModules();

    @Nullable
    AppliedItemModule getModule(ItemModule module);

    void setSlots(List<ItemModuleSlot> slots);

    AppliedItemModule attach(ItemModule module, int slot, boolean simulate);

    AppliedItemModule attach(ItemModule module, boolean simulate);
}

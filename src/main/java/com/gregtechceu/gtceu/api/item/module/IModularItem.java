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

    void saveModuleData();

    @Nullable
    ItemModule getModuleInSlot(int slot);

    @NotNull
    List<ItemModule> getModules();

    void setSlots(List<ItemModuleSlot> slots);

    <T extends ItemModule> T attach(ItemModuleType<T> module, int slot, boolean simulate);

    <T extends ItemModule> T attach(ItemModuleType<T> module, boolean simulate);

    boolean detachModule(ItemModule module);

    boolean detachModule(int slot);
}

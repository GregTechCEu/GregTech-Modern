package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public interface IModularItem {

    /**
     * @return the default module slot configuration of this item
     */
    List<ItemModuleSlot> getSlots();

    void clearModules();

    @Nullable
    ModuleData getModuleDataForSlot(int slot);

    List<ModuleData> getAllModuleData();

    List<ItemModule> getModules();

    @Nullable
    ModuleData getModuleData(ItemModule module);

    void setSlots(List<ItemModuleSlot> slots);

    @Nullable
    ModuleData attach(ItemModule module, ItemStack itemToApply, int slot, boolean simulate);

    @Nullable
    ModuleData attach(ItemModule module, ItemStack itemToApply, boolean simulate);

    void detach(ModuleData data);

    void detach(int slot);

    default void runForEachModule(BiConsumer<ItemModule, ModuleData> consumer) {
        getAllModuleData().forEach(v -> consumer.accept(v.getModule(), v));
    }
}

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
    ModuleContext getModuleContextForSlot(int slot);

    List<ModuleContext> getAllModuleInstances();

    List<ItemModule> getModules();

    @Nullable
    ModuleContext getModuleContext(ItemModule module);

    void setSlots(List<ItemModuleSlot> slots);

    @Nullable
    ModuleContext attach(ItemModule module, ItemStack itemToApply, int slot, boolean simulate);

    @Nullable
    ModuleContext attach(ItemModule module, ItemStack itemToApply, boolean simulate);

    void detach(ModuleContext data);

    void detach(int slot);

    void saveModuleData();

    default void runForEachModule(BiConsumer<ItemModule, ModuleContext> consumer) {
        getAllModuleInstances().forEach(v -> consumer.accept(v.getModule(), v));
    }
}

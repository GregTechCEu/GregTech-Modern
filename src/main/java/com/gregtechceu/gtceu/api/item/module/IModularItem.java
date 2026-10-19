package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.function.BiConsumer;

public interface IModularItem {

    int MAX_MODULE_SLOTS = 10;

    /**
     * @return the default module slot configuration of this item
     */
    List<ItemModuleSlot> getSlots();

    @Nullable
    ModuleContext getModuleContextForSlot(int slot);

    List<ModuleContext> getAllModuleInstances();

    ModularItemData getData();

    void setData(ModularItemData data);

    @Nullable
    ModuleContext getModuleContext(ItemModule module);

    @ApiStatus.Internal
    @VisibleForTesting
    void setSlots(List<ItemModuleSlot> slots);

    @Nullable
    ModuleContext attach(ItemModule module, ItemStack itemToApply, int slot, boolean simulate);

    @Nullable
    ModuleContext attach(ItemModule module, ItemStack itemToApply, boolean simulate);

    void detach(int slot);

    default void runForEachModule(BiConsumer<ItemModule, ModuleContext> consumer) {
        getAllModuleInstances().forEach(v -> consumer.accept(v.getModule(), v));
    }

    static boolean isSameItemSameModules(ItemStack first, ItemStack second) {
        if (!ItemStack.isSameItem(first, second)) return false;

        var firstModules = first.getOrDefault(GTDataComponents.MODULAR_ITEM_DATA, new ModularItemData(List.of()));
        var secondModules = second.getOrDefault(GTDataComponents.MODULAR_ITEM_DATA, new ModularItemData(List.of()));

        if (firstModules.getModules().size() != secondModules.getModules().size()) return false;

        for (var moduleData: firstModules.getModules()) {
            var secondData = secondModules.getModuleDataForSlot(moduleData.slot);
            if (secondData == null) return false;
            if (moduleData.getModule() != secondData.getModule()) return false;
        }

        return true;
    }
}

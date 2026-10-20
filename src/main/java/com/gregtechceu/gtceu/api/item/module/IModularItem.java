package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public interface IModularItem {

    int MAX_MODULE_SLOTS = 10;
    String MODULES_TAG = "Modules";

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

    static void setModularItemData(ItemStack stack, ModularItemData data) {
        stack.getOrCreateTag().put(MODULES_TAG,
                ModularItemData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, GTCEu.LOGGER::error));
    }

    static ModularItemData getModularItemData(ItemStack stack) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains(MODULES_TAG))
            return new ModularItemData(List.of());
        return ModularItemData.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateTagElement(MODULES_TAG))
                .getOrThrow(false, GTCEu.LOGGER::error).getFirst();
    }

    static boolean isSameItemSameModules(ItemStack first, ItemStack second) {
        if (!ItemStack.isSameItem(first, second)) return false;

        var firstModules = getModularItemData(first);
        var secondModules = getModularItemData(second);

        if (firstModules.getModules().size() != secondModules.getModules().size()) return false;

        for (var moduleData : firstModules.getModules()) {
            var secondData = secondModules.getModuleDataForSlot(moduleData.slot);
            if (secondData == null) return false;
            if (moduleData.getModule() != secondData.getModule()) return false;
        }

        return true;
    }
}

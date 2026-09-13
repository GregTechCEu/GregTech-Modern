package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModularItemStack implements IModularItem {

    // spotless:off
    //spotless:on

    public static final String MODULES_TAG = "Modules";

    private final ItemStack stack;
    private final List<ItemModuleSlot> moduleSlots;
    public ModularItemStack(ItemStack stack, List<ItemModuleSlot> slots) {
        this.stack = stack;
        this.moduleSlots = slots;
    }

    @Override
    public AppliedItemModule attach(ItemModule module, int slot, boolean simulate) {
        CompoundTag modulesTag = stack.getOrCreateTagElement(MODULES_TAG);
        ItemModuleSlot moduleSlot = getSlots().get(slot);
        if (moduleSlot == null || !moduleSlot.acceptsModule(module) || !module.canApplyTo(stack)) return null;
        if (!modulesTag.contains(String.valueOf(slot)) && !simulate)
            modulesTag.put(String.valueOf(slot), new CompoundTag());
        AppliedItemModule appliedModule = new AppliedItemModule(
                simulate ? new CompoundTag() : modulesTag.getCompound(String.valueOf(slot)),
                module,
                slot);
        appliedModule.setAppliedTo(stack);
        if (!simulate) {
            module.onAttach(appliedModule);
        }
        return appliedModule;
    }

    @Override
    public @Nullable AppliedItemModule attach(ItemModule module, boolean simulate) {
        for (int i = 0; i < getSlots().size(); i++) {
            if (getModuleInSlot(i) == null) return attach(module, i, simulate);
        }
        return null;
    }

    @Override
    public boolean detachModule(int slot) {
        AppliedItemModule module = getModuleInSlot(slot);
        if (module == null) return true;
        return detachModule(module);
    }

    @Override
    public boolean detachModule(AppliedItemModule module) {
        if (!module.getModule().canRemove(module)) return false;
        module.getModule().onRemove(module);
        stack.getOrCreateTagElement(ModularItemStack.MODULES_TAG).remove(String.valueOf(module.getSlot()));
        module.setAppliedTo(null);
        return true;
    }

    @Override
    public void clearModules() {
        stack.removeTagKey(MODULES_TAG);
    }

    @Override
    public @Nullable AppliedItemModule getModuleInSlot(int slot) {
        CompoundTag modulesTag = stack.getOrCreateTagElement(MODULES_TAG);
        if (!modulesTag.contains(String.valueOf(slot))) return null;
        return new AppliedItemModule(modulesTag.getCompound(String.valueOf(slot)), stack, slot);
    }

    @Override
    public @NotNull List<AppliedItemModule> getAppliedModules() {
        CompoundTag modulesTag = stack.getOrCreateTagElement(MODULES_TAG);
        List<AppliedItemModule> modules = new ArrayList<>();
        for (String key : modulesTag.getAllKeys()) {
            modules.add(new AppliedItemModule(modulesTag.getCompound(key), stack, Integer.parseInt(key)));
        }
        return modules;
    }

    @Override
    public @Nullable AppliedItemModule getModule(ItemModule module) {
        return getAppliedModules().stream().filter(appliedModule -> appliedModule.getModule() == module).findAny()
                .orElse(null);
    }

    @Override
    public void setSlots(List<ItemModuleSlot> slots) {
        clearModules();
        moduleSlots.clear();
        moduleSlots.addAll(slots);
    }

    @Unmodifiable
    @Override
    public List<ItemModuleSlot> getSlots() {
        return Collections.unmodifiableList(moduleSlots);
    }
}

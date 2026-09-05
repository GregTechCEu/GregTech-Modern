package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModularItemStack implements IModularItem {

    public static final String MODULE_SLOTS_KEY = "ModuleSlots";
    public static final String MODULES_TAG = "Modules";

    private final ItemStack stack;
    private final Function<ItemStack, List<ItemModuleSlot>> defaultSlotGetter;

    public ModularItemStack(ItemStack stack, Function<ItemStack, List<ItemModuleSlot>> defaultSlotGetter) {
        this.stack = stack;
        this.defaultSlotGetter = defaultSlotGetter;
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

    public List<ItemModuleSlot> getDefaultSlots() {
        return this.defaultSlotGetter.apply(stack);
    }

    @Override
    public void setSlots(List<ItemModuleSlot> slots) {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < slots.size(); i++) {
            ItemModuleSlot slot = slots.get(i);
            if (slot != null) tag.put(String.valueOf(i), slot.serializeNBT());
        }
        stack.getOrCreateTag().put(MODULE_SLOTS_KEY, tag);
    }

    @Unmodifiable
    @Override
    public List<ItemModuleSlot> getSlots() {
        if (!stack.getOrCreateTag().contains(MODULE_SLOTS_KEY, Tag.TAG_COMPOUND)) {
            List<ItemModuleSlot> slots = getDefaultSlots();
            setSlots(slots);
            return slots;
        } else {
            List<ItemModuleSlot> slots = new ArrayList<>();
            CompoundTag tag = stack.getOrCreateTagElement(MODULE_SLOTS_KEY);
            for (String key : tag.getAllKeys()) {
                int i = Integer.parseInt(key);
                while (slots.size() <= i) slots.add(null);
                slots.set(i, ItemModuleSlot.fromNBT(tag.getCompound(key)));
            }
            return slots;
        }
    }
}

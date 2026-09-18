package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.api.item.module.ModuleData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ModularItemStack implements IModularItem {

    public static final String MODULES_TAG = "Modules";

    private final ItemStack stack;
    private final List<ItemModuleSlot> slots;
    private final List<ModuleData> currentModules;

    public ModularItemStack(ItemStack stack, Function<ItemStack, List<ItemModuleSlot>> slotsGetter) {
        this.stack = stack;
        slots = slotsGetter.apply(stack);
        currentModules = new ArrayList<>();

        if (stack.getOrCreateTag().contains(MODULES_TAG)) {
            List<ModuleData> data = ModuleData.CODEC.listOf()
                    .decode(NbtOps.INSTANCE, stack.getOrCreateTagElement(MODULES_TAG))
                    .getOrThrow(false, GTCEu.LOGGER::error)
                    .getFirst();

            for (ModuleData module : data) {
                module.setModularItemStack(this);
                module.setAppliedTo(stack);
                currentModules.add(module);
            }
        }
    }

    public void saveData() {
        stack.getOrCreateTag().put(MODULES_TAG, ModuleData.CODEC.listOf().encodeStart(NbtOps.INSTANCE, currentModules).getOrThrow(false, GTCEu.LOGGER::error));
    }

    @Override
    public @Nullable ModuleData attach(ItemModule module, ItemStack itemToApply, int slot, boolean simulate) {
        if (slot >= getSlots().size()) return null;

        ItemModuleSlot moduleSlot = getSlots().get(slot);
        if (!moduleSlot.acceptsModule(module) || !module.canApplyTo(stack)) return null;

        for (int i=0; i<currentModules.size(); i++) {
            if (i == slot) continue;
            var existingModule = currentModules.get(i);
            if (existingModule.getModule() == module) return null;
        }

        ModuleData moduleData = new ModuleData(
                module,
                new CompoundTag(),
                itemToApply);

        moduleData.setAppliedTo(stack);
        currentModules.add(moduleData);
        if (!simulate) {
            module.onAttach(moduleData);
        }
        return moduleData;
    }

    @Override
    public @Nullable ModuleData attach(ItemModule module, ItemStack itemToApply, boolean simulate) {
        for (int i = 0; i < getSlots().size(); i++) {
            if (getModuleDataForSlot(i) == null) return attach(module, itemToApply, i, simulate);
        }
        return null;
    }

    @Override
    public void detach(ModuleData appliedModule) {
        if (!appliedModule.getModule().canRemove(appliedModule)) return;
        appliedModule.getModule().onRemove(appliedModule);
        saveData();
    }

    @Override
    public void clearModules() {
        stack.removeTagKey(MODULES_TAG);
    }

    @Override
    public @Nullable ModuleData getModuleDataForSlot(int slot) {
        if (slot >= currentModules.size()) return null;
        return currentModules.get(slot);
    }

    @Override
    public List<ModuleData> getAllModuleData() {
        return currentModules;
    }

    @Override
    public List<ItemModule> getModules() {
        return getAllModuleData().stream().map(ModuleData::getModule).toList();
    }

    @Override
    public @Nullable ModuleData getModuleData(ItemModule module) {
        return getAllModuleData().stream().filter(appliedModule -> appliedModule.getModule() == module).findAny()
                .orElse(null);
    }

    @Override
    public void setSlots(List<ItemModuleSlot> slots) {
        this.slots.clear();
        this.slots.addAll(slots);
    }

    @Unmodifiable
    @Override
    public List<ItemModuleSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }
}

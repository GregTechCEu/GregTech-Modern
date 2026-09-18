package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.api.item.module.ModuleData;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ModularItemStack implements IModularItem {

    public static final String MODULES_TAG = "Modules";

    private final ItemStack stack;
    private final List<ItemModuleSlot> slots;
    private final Int2ObjectMap<ModuleData> currentModules;

    private static final Codec<List<ModuleData>> DATA_CODEC = Codec.list(ModuleData.CODEC);

    public ModularItemStack(ItemStack stack, Function<ItemStack, List<ItemModuleSlot>> slotsGetter) {
        this.stack = stack;
        slots = slotsGetter.apply(stack);
        currentModules = new Int2ObjectArrayMap<>();

        if (stack.getOrCreateTagElement(MODULES_TAG).contains(MODULES_TAG)) {
            List<ModuleData> data = DATA_CODEC
                    .decode(NbtOps.INSTANCE, stack.getOrCreateTagElement(MODULES_TAG).get(MODULES_TAG))
                    .getOrThrow(false, GTCEu.LOGGER::error)
                    .getFirst();

            for (ModuleData entry : data) {
                entry.setModularItemStack(this);
                entry.setAppliedTo(stack);
                currentModules.put(entry.getSlot(), entry);
            }
        }
    }

    public void saveData() {
        stack.getOrCreateTagElement(MODULES_TAG).put(MODULES_TAG, DATA_CODEC.encodeStart(NbtOps.INSTANCE, currentModules.values().stream().toList()).getOrThrow(false, GTCEu.LOGGER::error));
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
                slot,
                module,
                new CompoundTag(),
                itemToApply);

        if (!simulate) {
            moduleData.setAppliedTo(stack);
            module.onAttach(moduleData);
            currentModules.put(slot, moduleData);
            saveData();
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
    public void detach(ModuleData data) {
        for (var entry: currentModules.int2ObjectEntrySet()) {
            if (entry.getValue() == data) detach(entry.getIntKey());
        }
    }

    @Override
    public void detach(int slot) {
        if (currentModules.containsKey(slot)) {
            var current = currentModules.get(slot);
            if (!current.getModule().canRemove(current)) return;
            current.getModule().onRemove(current);
            currentModules.remove(slot);
        }
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
        return currentModules.values().stream().sorted(Comparator.comparingInt(ModuleData::getSlot)).toList();
    }

    @Override
    public List<ItemModule> getModules() {
        return getAllModuleData().stream().sorted(Comparator.comparingInt(ModuleData::getSlot)).map(ModuleData::getModule).toList();
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

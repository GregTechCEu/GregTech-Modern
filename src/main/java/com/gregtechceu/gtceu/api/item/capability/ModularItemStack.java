package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.module.*;

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
    private final Int2ObjectMap<ModuleContext> currentModules;

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
                currentModules.put(entry.getSlot(), new ModuleContext(stack, this, entry));
            }
        }
    }

    @Override
    public void saveModuleData() {
        var data = DATA_CODEC.encodeStart(NbtOps.INSTANCE, currentModules.values().stream().map(ModuleContext::getData).toList())
                .getOrThrow(false, GTCEu.LOGGER::error);
        stack.getOrCreateTagElement(MODULES_TAG).put(MODULES_TAG, data);
    }

    @Override
    public @Nullable ModuleContext attach(ItemModule module, ItemStack itemToApply, int slot, boolean simulate) {
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

        ModuleContext context = new ModuleContext(stack, this, moduleData);

        if (!simulate) {
            moduleData.setAppliedTo(stack);
            module.onAttach(context);
            currentModules.put(slot, context);
            saveModuleData();
        }
        return context;
    }

    @Override
    public @Nullable ModuleContext attach(ItemModule module, ItemStack itemToApply, boolean simulate) {
        for (int i = 0; i < getSlots().size(); i++) {
            if (getModuleContextForSlot(i) == null) return attach(module, itemToApply, i, simulate);
        }
        return null;
    }

    @Override
    public void detach(ModuleContext data) {
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
        saveModuleData();
    }

    @Override
    public void clearModules() {
        stack.removeTagKey(MODULES_TAG);
    }

    @Override
    public @Nullable ModuleContext getModuleContextForSlot(int slot) {
        if (slot >= currentModules.size()) return null;
        return currentModules.get(slot);
    }

    @Override
    public List<ModuleContext> getAllModuleInstances() {
        return currentModules.values().stream().sorted(Comparator.comparingInt(v -> v.getData().getSlot())).toList();
    }

    @Override
    public List<ItemModule> getModules() {
        return getAllModuleInstances().stream().sorted(Comparator.comparingInt(v -> v.getData().getSlot())).map(ModuleContext::getModule).toList();
    }

    @Override
    public @Nullable ModuleContext getModuleContext(ItemModule module) {
        return getAllModuleInstances().stream().filter(appliedModule -> appliedModule.getModule() == module).findAny()
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

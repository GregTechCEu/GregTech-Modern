package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.module.*;

import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ModularItemStack implements IModularItem {

    public static final String MODULES_TAG = "Modules";

    private final ItemStack stack;
    private final List<ItemModuleSlot> slots;

    public ModularItemStack(ItemStack stack, Function<ItemStack, List<ItemModuleSlot>> slotsGetter) {
        this.stack = stack;
        slots = slotsGetter.apply(stack);
    }

    @Override
    public void setData(ModularItemData data) {
        stack.getOrCreateTag().put(MODULES_TAG,
                ModularItemData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, GTCEu.LOGGER::error));
    }

    @Override
    public ModularItemData getData() {
        if (!stack.getOrCreateTag().contains(MODULES_TAG)) return new ModularItemData(List.of());
        return ModularItemData.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateTagElement(MODULES_TAG))
                .getOrThrow(false, GTCEu.LOGGER::error).getFirst();
    }

    @Override
    public @Nullable ModuleContext attach(ItemModule module, ItemStack itemToApply, int slot, boolean simulate) {
        if (slot >= getSlots().size()) return null;

        ItemModuleSlot moduleSlot = getSlots().get(slot);
        if (!moduleSlot.acceptsModule(module) || !module.canApplyTo(stack)) return null;

        for (int i = 0; i < getData().getModules().size(); i++) {
            if (i == slot) continue;
            var existingModule = getData().getModules().get(i);
            if (existingModule.getModule() == module) return null;
        }

        var moduleData = module.defaultModuleData(slot, module, itemToApply);
        ModuleContext context = new ModuleContext(stack, this, moduleData);

        if (!simulate) {
            module.onAttach(context);
            var newData = getData().withModuleInSlot(slot, moduleData);
            setData(newData);
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
        for (var entry : getData().moduleMap().int2ObjectEntrySet()) {
            if (entry.getValue() == data.getData()) detach(entry.getIntKey());
        }
    }

    @Override
    public void detach(int slot) {
        var current = getData().getModuleDataForSlot(slot);
        if (current == null) return;
        var context = new ModuleContext(stack, this, current);

        if (!current.getModule().canRemove(context)) return;
        current.getModule().onRemove(context);
        setData(getData().withModuleRemoved(slot));
    }

    @Override
    public @Nullable ModuleContext getModuleContextForSlot(int slot) {
        ModuleData data = getData().getModuleDataForSlot(slot);
        if (data == null) return null;
        return new ModuleContext(stack, this, data);
    }

    @Override
    public List<ModuleContext> getAllModuleInstances() {
        return getData().getModules().stream().map(v -> new ModuleContext(stack, this, v)).toList();
    }

    @Override
    public @Nullable ModuleContext getModuleContext(ItemModule module) {
        var data = getData().getModuleByType(module);
        if (data == null) return null;
        return new ModuleContext(stack, this, data);
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

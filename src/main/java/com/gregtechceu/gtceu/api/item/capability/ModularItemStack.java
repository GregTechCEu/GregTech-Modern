package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;

import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModularItemStack implements IModularItem {

    public static final String MODULES_TAG = "Modules";

    private final ItemStack stack;
    private final List<ItemModuleSlot> moduleSlots;
    private final List<ItemModule> modules;

    public ModularItemStack(ItemStack stack, List<ItemModuleSlot> slots) {
        this.stack = stack;
        this.moduleSlots = slots;

        modules = new ArrayList<>();
        if (stack.getOrCreateTag().contains(MODULES_TAG)) {
            ModularItemData data = ModularItemData.CODEC
                    .decode(NbtOps.INSTANCE, stack.getOrCreateTagElement(MODULES_TAG))
                    .getOrThrow(false, GTCEu.LOGGER::error).getFirst();

            for (int i = 0; i < data.modules.size(); i++) {
                var module = data.modules.get(i);
                module.setModularItemStack(this);
                module.setAppliedTo(stack);
                modules.add(module);
            }
        }
    }

    public void saveModuleData() {
        stack.getOrCreateTag().put(MODULES_TAG, ModularItemData.CODEC
                .encodeStart(NbtOps.INSTANCE, new ModularItemData(modules))
                .getOrThrow(false, GTCEu.LOGGER::error));
    }

    @Override
    public @Nullable <T extends ItemModule> T attach(ItemModuleType<T> moduleType, ItemStack attachItem, int slot,
                                                     boolean simulate) {
        T module = moduleType.defaultInstance().apply(attachItem);

        ItemModuleSlot moduleSlot = getSlots().get(slot);
        if (!moduleSlot.acceptsModule(module) || !module.canApplyTo(stack)) return null;

        if (!simulate) {
            module.setModularItemStack(this);
            module.setAppliedTo(stack);
            module.onAttach();
            modules.add(module);
            saveModuleData();
        }
        return module;
    }

    @Override
    public @Nullable <T extends ItemModule> T attach(ItemModuleType<T> module, ItemStack attachItem, boolean simulate) {
        for (int i = 0; i < getSlots().size(); i++) {
            if (getModuleInSlot(i) == null) {
                return attach(module, attachItem, i, simulate);
            }
        }
        return null;
    }

    @Override
    public boolean detachModule(int slot) {
        ItemModule module = getModuleInSlot(slot);
        if (module == null) return true;
        return detachModule(module);
    }

    @Override
    public boolean detachModule(ItemModule module) {
        if (!module.canRemove()) return false;
        module.onRemove();
        module.setModularItemStack(null);
        module.setAppliedTo(null);
        modules.remove(module);
        saveModuleData();
        return true;
    }

    @Override
    public void clearModules() {
        stack.removeTagKey(MODULES_TAG);
    }

    @Override
    public @Nullable ItemModule getModuleInSlot(int slot) {
        if (modules.isEmpty() || slot >= modules.size()) return null;
        return modules.get(slot);
    }

    @Override
    public @NotNull List<ItemModule> getModules() {
        return Collections.unmodifiableList(modules);
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

    private record ModularItemData(List<ItemModule> modules) {

        // spotless:off
        public static final Codec<ModularItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemModule.CODEC.listOf().optionalFieldOf("modules", new ArrayList<>()).forGetter(ModularItemData::modules)
        ).apply(instance, ModularItemData::new));
        //spotless:on
    }
}

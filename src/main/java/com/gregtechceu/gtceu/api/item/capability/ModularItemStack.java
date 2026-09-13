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
        if (!stack.getOrCreateTagElement(MODULES_TAG).isEmpty()) {
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

    public void saveModuleData() {}

    @Override
    public @Nullable <T extends ItemModule> T attach(ItemModuleType<T> moduleType, ItemStack attachItem, int slot, boolean simulate) {
        ItemModule module = moduleType.defaultInstance().apply(attachItem);

        ItemModuleSlot moduleSlot = getSlots().get(slot);
        if (moduleSlot == null || !moduleSlot.acceptsModule(module) || !module.canApplyTo(stack)) return null;

        if (!simulate) {
            module.setModularItemStack(this);
            module.setAppliedTo(stack);
            module.onAttach();
            modules.add(module);
        }
        return null;
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
        return true;
    }

    @Override
    public void clearModules() {
        stack.removeTagKey(MODULES_TAG);
    }

    @Override
    public @Nullable ItemModule getModuleInSlot(int slot) {
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

    private record ModularItemData(List<ItemModule> modules, List<ItemStack> moduleItems) {

        // spotless:off
        public static final Codec<ModularItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemModule.CODEC.listOf().fieldOf("modules").forGetter(ModularItemData::modules),
                ItemStack.CODEC.listOf().fieldOf("module_items").forGetter(ModularItemData::moduleItems)
        ).apply(instance, ModularItemData::new));
        //spotless:on
    }
}

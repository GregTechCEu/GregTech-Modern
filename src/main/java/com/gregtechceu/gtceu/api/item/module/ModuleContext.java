package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.world.item.ItemStack;

import lombok.Getter;

public class ModuleContext {

    /**
     * The stack that this module is applied to.
     * If this module is not applied to anything, this field is {@code null}.
     */
    @Getter
    private final ItemStack appliedTo;

    /**
     * The {@link IModularItem} capability of the item this module is attached to.
     */
    @Getter
    private final IModularItem modularItemStack;

    /**
     * The persistent data for this module.<br>
     * The data object must be immutable.
     */
    private ModuleData data;

    public ModuleContext(ItemStack appliedTo, IModularItem modularItem, ModuleData data) {
        this.appliedTo = appliedTo;
        this.modularItemStack = modularItem;
        this.data = data;
    }

    public ItemModule getModule() {
        return getData().getModule();
    }

    public ModuleData getData() {
        return data;
    }

    public <T extends ModuleData> T getData(Class<T> dataClass) {
        return dataClass.cast(data);
    }

    public void setData(ModuleData data) {
        if (!data.getClass().equals(getModule().moduleDataClass())) {
            throw new ClassCastException("Cannot set module data: expected data class %s, got %s"
                    .formatted(getModule().moduleDataClass(), data.getClass()));
        }
        this.data = data;
        modularItemStack.setData(modularItemStack.getData().withModuleInSlot(getData().getSlot(), getData()));
    }
}

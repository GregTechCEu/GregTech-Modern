package com.gregtechceu.gtceu.api.item.module;

import lombok.Getter;
import net.minecraft.world.item.ItemStack;

public class ModuleContext {

    /**
     * The stack that this module is applied to.
     * If this module is not applied to anything, this field is {@code null}.
     */
    @Getter
    private ItemStack appliedTo;

    /**
     * The {@link IModularItem} capability of the item this module is attached to.
     */
    @Getter
    private IModularItem modularItemStack;

    /**
     * The persistent data for this module.<br>
     * The data object must be immutable
     */
    @Getter
    private ModuleData data;

    public ModuleContext(ItemStack appliedTo, IModularItem modularItem, ModuleData data) {
        this.appliedTo = appliedTo;
        this.modularItemStack = modularItem;
        this.data = data;
    }

    public ItemModule getModule() {
        return getData().getModule();
    }

    public ItemStack getModuleItem() {
        return getData().getModuleItem();
    }

    public void setData(ModuleData data) {
        this.data = data;
        modularItemStack.saveModuleData();
    }
}

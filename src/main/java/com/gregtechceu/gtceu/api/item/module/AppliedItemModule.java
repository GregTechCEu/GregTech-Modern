package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

public final class AppliedItemModule {

    private final CompoundTag moduleTag;
    @Getter
    private final int slot;

    @Getter
    private ItemModule module;

    @Getter
    private CompoundTag tag;

    @Getter
    private ItemStack moduleItem;

    /**
     * The stack that this module is applied to.
     * If this module is not applied to anything, this field is {@code null}.
     */
    @Getter
    @Setter
    private ItemStack appliedTo;

    public void setModuleItem(ItemStack stack) {
        if (module.forceModuleItemNBT())
            stack.getOrCreateTag();
        this.moduleItem = stack;
        this.moduleTag.put("item", stack.serializeNBT());
    }
}

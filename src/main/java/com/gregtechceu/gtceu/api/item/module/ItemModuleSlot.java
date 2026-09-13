package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.IDrawable;

public abstract class ItemModuleSlot {

    public abstract boolean acceptsModule(ItemModule module);

    public abstract Component getDisplayName();

    public IDrawable getSlotTexture() {
        return null;
    }
}

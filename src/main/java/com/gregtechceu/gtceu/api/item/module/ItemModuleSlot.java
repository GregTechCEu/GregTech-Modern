package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.IDrawable;
import org.jetbrains.annotations.Nullable;

public abstract class ItemModuleSlot {

    public abstract boolean acceptsModule(ItemModule module);

    public abstract Component getDisplayName();

    public @Nullable IDrawable getSlotTexture() {
        return null;
    }
}

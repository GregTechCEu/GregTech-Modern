package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.IDrawable;
import lombok.Getter;

public abstract class ItemModuleSlot {

    @Getter
    private final ResourceLocation id;

    protected ItemModuleSlot(ResourceLocation id) {
        this.id = id;
    }

    public abstract boolean acceptsModule(ItemModule module);

    public abstract Component getDisplayName();

    public IDrawable getSlotTexture() {
        return null;
    }
}

package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.common.mui.drawable.BorderDrawable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.IDrawable;

public class UniversalItemModuleSlot extends ItemModuleSlot {

    public UniversalItemModuleSlot(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean acceptsModule(ItemModule module) {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("metaarmor.tooltip.modifier_slot.universal");
    }

    @Override
    public IDrawable getSlotTexture() {
        return new BorderDrawable(0xFFFFFFFF, 1);
    }
}

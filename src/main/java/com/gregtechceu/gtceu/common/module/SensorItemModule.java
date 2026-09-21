package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.TieredItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SensorItemModule extends TieredItemModule {

    public SensorItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }
}

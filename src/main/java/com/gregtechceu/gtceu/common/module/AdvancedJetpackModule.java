package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.item.armor.AdvancedJetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AdvancedJetpackModule extends JetpackModule {

    private static final AdvancedJetpack JETPACK = new AdvancedJetpack(
            256,
            6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller - 4)),
            ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller);

    public AdvancedJetpackModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }

    @Override
    protected AdvancedJetpack getArmorLogic(ModuleContext moduleContext) {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.EV;
    }
}

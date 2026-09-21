package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.api.item.module.ArmorLogicItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.armor.Jetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JetpackModule extends ArmorLogicItemModule implements ITieredItemModule {

    private static final Jetpack JETPACK = new Jetpack(
            15,
            1_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierImpeller - 2)),
            ConfigHolder.INSTANCE.tools.voltageTierImpeller);

    public JetpackModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }

    @Override
    protected @Nullable IArmorLogic getArmorLogic(ModuleContext moduleContext) {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.HV;
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .bool(Text.lang("hud.gtceu.armor.hover_mode"),
                        () -> moduleContext.getAppliedTo().getOrDefault(GTDataComponents.ARMOR_DATA, GTArmor.EMPTY)
                                .hover(),
                        b -> moduleContext.getAppliedTo().update(GTDataComponents.ARMOR_DATA, GTArmor.EMPTY,
                                v -> v.setHover(b)));
    }
}

package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.ArmorLogicItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.common.item.armor.Jetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;

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
    protected Jetpack getArmorLogic(ModuleContext moduleContext) {
        return JETPACK;
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        return super.canApplyTo(stack) && modular.getAllModuleInstances().stream()
                .noneMatch(context -> context.getModule() instanceof JetpackModule ||
                        context.getModule() instanceof LiquidFuelJetpackModule);
    }

    @Override
    public void onArmorTick(ModuleContext context, LivingEntity entity) {
        if (isEnabled(context)) super.onArmorTick(context, entity);
    }

    @Override
    public int getTier() {
        return GTValues.MV;
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .bool(Text.lang("gui.gtceu.item_module.hover_mode"),
                        () -> moduleContext.getAppliedTo().getOrCreateTag().getBoolean("hover"),
                        b -> moduleContext.getAppliedTo().getOrCreateTag().putBoolean("hover", b));
    }
}

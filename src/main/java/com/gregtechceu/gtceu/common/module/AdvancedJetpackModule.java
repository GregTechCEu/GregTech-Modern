package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ArmorLogicItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.common.item.armor.AdvancedJetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdvancedJetpackModule extends ArmorLogicItemModule implements ITieredItemModule {

    private static final AdvancedJetpack JETPACK = new AdvancedJetpack(
            256,
            6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller - 4)),
            ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller);

    public AdvancedJetpackModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.advanced_jetpack");
    }

    @Override
    protected @Nullable IArmorLogic getArmorLogic(AppliedItemModule module) {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.EV;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.jetpack", module.getModuleItem().getHoverName()));
    }

    @Override
    public Settings getSettings(AppliedItemModule module, PanelSyncManager psm, int id) {
        return super.getSettings(module, psm, id)
                .bool(Text.lang("metaarmor.hud.hover_mode"),
                        () -> module.getAppliedTo().getOrCreateTag().getBoolean("hover"),
                        b -> module.getAppliedTo().getOrCreateTag().putBoolean("hover", b));
    }
}

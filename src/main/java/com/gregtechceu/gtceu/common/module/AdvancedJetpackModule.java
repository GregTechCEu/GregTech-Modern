package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.item.armor.AdvancedJetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdvancedJetpackModule extends ArmorLogicItemModule implements ITieredItemModule {

    // spotless:off
    public static final Codec<AdvancedJetpackModule> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(
            Codec.BOOL.fieldOf("hover").forGetter(AdvancedJetpackModule::isHover)
    ).apply(instance, AdvancedJetpackModule::new));
    // spotless:on

    private static final AdvancedJetpack JETPACK = new AdvancedJetpack(
            256,
            6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller - 4)),
            ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller);

    @Getter
    private boolean hover;

    public AdvancedJetpackModule(boolean isEnabled, ItemStack moduleItem, boolean hover) {
        super(isEnabled, moduleItem);
        this.hover = hover;
    }

    public AdvancedJetpackModule(ItemStack moduleItem) {
        super(moduleItem);
        this.hover = false;
    }

    @Override
    public ItemModuleType<AdvancedJetpackModule> type() {
        return GTItemModules.ADVANCED_JETPACK;
    }

    public void setHover(boolean hover) {
        this.hover = hover;
        getModularItemStack().saveModuleData();
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.advanced_jetpack");
    }

    @Override
    protected @Nullable IArmorLogic getArmorLogic() {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.EV;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.jetpack", getModuleItem().getHoverName()));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(PanelSyncManager psm, int id) {
        return super.getSettings(psm, id)
                .bool(Text.lang("metaarmor.hud.hover_mode"),
                        this::isHover, this::setHover);
    }
}

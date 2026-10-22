package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ModuleData;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.common.item.armor.AdvancedJetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Objects;

public class AdvancedJetpackModule extends JetpackModule {

    private static final AdvancedJetpack JETPACK = new AdvancedJetpack(
            48,
            6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller - 4)),
            ConfigHolder.INSTANCE.tools.voltageTierAdvImpeller);

    public AdvancedJetpackModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public MapCodec<? extends ModuleData> moduleDataCodec() {
        return ThrusterModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return ThrusterModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new ThrusterModuleData(slot, module, moduleStack, true, true);
    }

    public boolean isOmnidirectional(ModuleContext context) {
        return context.getData(ThrusterModuleData.class).isOmnidirectional();
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext context, PanelSyncManager psm, int id) {
        return super.getSettings(context, psm, id)
                .bool(Text.lang("gui.gtceu.item_module.omnidirectional_sprint"), () -> isOmnidirectional(context),
                        value -> context.setData(context.getData(ThrusterModuleData.class).withOmnidirectional(value)));
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), JETPACK.getEnergyPerUse(),
                Math.round(JETPACK.getEnergyPerUse() * JETPACK.getSprintEnergyModifier()));
    }

    @Override
    protected AdvancedJetpack getArmorLogic(ModuleContext moduleContext) {
        return JETPACK;
    }

    @Override
    public int getTier() {
        return GTValues.EV;
    }

    public static class ThrusterModuleData extends ModuleData {

        // spotless:off
        public static final MapCodec<ThrusterModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance).and(
                Codec.BOOL.optionalFieldOf("omnidirectional", true).forGetter(ThrusterModuleData::isOmnidirectional)
        ).apply(instance, ThrusterModuleData::new));
        // spotless:on

        @Getter
        private final boolean omnidirectional;

        public ThrusterModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                  boolean omnidirectional) {
            super(slot, module, moduleItem, enabled);
            this.omnidirectional = omnidirectional;
        }

        @Override
        public ModuleData copy() {
            return new ThrusterModuleData(slot, module, moduleItem.copy(), enabled, omnidirectional);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new ThrusterModuleData(slot, module, moduleItem, enabled, omnidirectional);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new ThrusterModuleData(slot, module, moduleItem, enabled, omnidirectional);
        }

        public ThrusterModuleData withOmnidirectional(boolean omnidirectional) {
            return new ThrusterModuleData(slot, module, moduleItem, enabled, omnidirectional);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ThrusterModuleData other && super.equals(obj) &&
                    omnidirectional == other.omnidirectional;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, omnidirectional);
        }
    }
}

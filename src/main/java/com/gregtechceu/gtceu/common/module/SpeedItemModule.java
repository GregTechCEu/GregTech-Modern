package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Objects;

public class SpeedItemModule extends TieredItemModule {

    private static final int ENERGY_USAGE = 819;

    public SpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public MapCodec<? extends ModuleData> moduleDataCodec() {
        return SpeedModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return SpeedModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new SpeedModuleData(slot, module, moduleStack, true, 1);
    }

    public float getStrength(ModuleContext context) {
        return context.getData(SpeedModuleData.class).getStrength();
    }

    public void setStrength(ModuleContext context, float strength) {
        if (!Float.isFinite(strength)) return;
        context.setData(context.getData(SpeedModuleData.class).withStrength(Mth.clamp(strength, 0, 1)));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext context, PanelSyncManager psm, int id) {
        return super.getSettings(context, psm, id)
                .num(Text.lang("gui.gtceu.item_module.power"), () -> getStrength(context),
                        value -> setStrength(context, (float) value), 0, 1,
                        value -> "+%.2f%%".formatted(value * getTier() * 25));
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), getTier() * 25);
    }

    @Override
    public void clientSideArmorTick(ModuleContext context, Player player) {
        updateMovementModifier(context, player);
    }

    @Override
    public void onArmorTick(ModuleContext context, LivingEntity entity) {
        if (entity instanceof Player player) updateMovementModifier(context, player);
        super.onArmorTick(context, entity);
    }

    private void updateMovementModifier(ModuleContext context, Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        var battery = GTCapabilityHelper.getElectricItem(context.getAppliedTo());
        double bonus = getTier() / 4d * getStrength(context);
        boolean active = isEnabled(context) && bonus > 0 && player.isSprinting() &&
                battery != null && battery.canUse(ENERGY_USAGE);
        if (!active) {
            attribute.removeModifier(getId());
            return;
        }
        var modifier = new AttributeModifier(getId(), bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if (!modifier.equals(attribute.getModifier(getId()))) attribute.addOrUpdateTransientModifier(modifier);
    }

    @Override
    public void onUnequip(ModuleContext context, LivingEntity entity) {
        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) attribute.removeModifier(getId());
    }

    @Override
    public long energyUsagePerTick(ModuleContext moduleContext, LivingEntity entity) {
        if (!isEnabled(moduleContext) || getStrength(moduleContext) <= 0) return 0;
        if (entity instanceof Player player) {
            return SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isSprinting() ? ENERGY_USAGE : 0;
        }
        return 0;
    }

    public static class SpeedModuleData extends ModuleData {

        // spotless:off
        public static final MapCodec<SpeedModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance).and(
                Codec.floatRange(0, 1).optionalFieldOf("strength", 1f).forGetter(SpeedModuleData::getStrength)
        ).apply(instance, SpeedModuleData::new));
        //spotless:on

        @Getter
        private final float strength;

        public SpeedModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled, float strength) {
            super(slot, module, moduleItem, enabled);
            this.strength = strength;
        }

        @Override
        public ModuleData copy() {
            return new SpeedModuleData(slot, module, moduleItem.copy(), enabled, strength);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength);
        }

        public SpeedModuleData withStrength(float strength) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SpeedModuleData other)) return false;
            return super.equals(obj) && strength == other.strength;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, strength);
        }
    }
}

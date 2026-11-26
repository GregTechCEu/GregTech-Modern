package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

public class SpeedItemModule extends TieredItemModule {

    public static final UUID SPEED_UUID = UUID.fromString("2dc2a66a-1e2c-453c-9ce2-cba71bb2e779");

    public SpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Codec<? extends ModuleData> moduleDataCodec() {
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

    public boolean isOmnidirectional(ModuleContext context) {
        return context.getData(SpeedModuleData.class).isOmnidirectional();
    }

    public double getEnergyCost(ModuleContext context) {
        return Math.max(0.25, ArmorUtils.getModuleEnergyCost(getTier()) * getStrength(context));
    }

    public void setStrength(ModuleContext context, float strength) {
        if (!Float.isFinite(strength)) return;
        context.setData(context.getData(SpeedModuleData.class).withStrength(Mth.clamp(strength, 0, 1)));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext context, PanelSyncManager psm, int id) {
        return super.getSettings(context, psm, id)
                .bool(Text.lang("gui.gtceu.item_module.omnidirectional_sprint"), () -> isOmnidirectional(context),
                        value -> context.setData(context.getData(SpeedModuleData.class).withOmnidirectional(value)))
                .num(Text.lang("gui.gtceu.item_module.power"), () -> getStrength(context),
                        value -> setStrength(context, (float) value), 0, 1,
                        value -> "+%.2f%%".formatted(value * getTier() * 25));
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), getTier() * 25,
                ArmorUtils.getModuleEnergyCost(getTier()));
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
        double bonus = getTier() / 4d * getStrength(context);
        boolean active = isEnabled(context) && bonus > 0 && player.isSprinting() &&
                ArmorUtils.isMovingOnGround(player) && ArmorUtils.useModuleEnergy(context.getAppliedTo(),
                        ArmorUtils.toModuleEnergyCredits(getEnergyCost(context)),
                        player.level().isClientSide);
        if (!active) {
            attribute.removeModifier(SPEED_UUID);
            return;
        }
        var modifier = new AttributeModifier(SPEED_UUID, "Speed Bost", bonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (!modifier.equals(attribute.getModifier(SPEED_UUID))) attribute.addTransientModifier(modifier);
    }

    @Override
    public void onUnequip(ModuleContext context, LivingEntity entity) {
        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) attribute.removeModifier(SPEED_UUID);
    }

    public static class SpeedModuleData extends ModuleData {

        // spotless:off
        public static final Codec<SpeedModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(instance.group(
                Codec.floatRange(0, 1).optionalFieldOf("strength", 1f).forGetter(SpeedModuleData::getStrength),
                Codec.BOOL.optionalFieldOf("omnidirectional", true).forGetter(SpeedModuleData::isOmnidirectional)
        )).apply(instance, SpeedModuleData::new));
        // spotless:on

        @Getter
        private final float strength;

        @Getter
        private final boolean omnidirectional;

        public SpeedModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled, float strength) {
            this(slot, module, moduleItem, enabled, strength, true);
        }

        public SpeedModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled, float strength,
                               boolean omnidirectional) {
            super(slot, module, moduleItem, enabled);
            this.strength = strength;
            this.omnidirectional = omnidirectional;
        }

        @Override
        public ModuleData copy() {
            return new SpeedModuleData(slot, module, moduleItem.copy(), enabled, strength, omnidirectional);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength, omnidirectional);
        }

        public SpeedModuleData withStrength(float strength) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength, omnidirectional);
        }

        public SpeedModuleData withOmnidirectional(boolean omnidirectional) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength, omnidirectional);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength, omnidirectional);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SpeedModuleData other)) return false;
            return super.equals(obj) && strength == other.strength && omnidirectional == other.omnidirectional;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, strength, omnidirectional);
        }
    }
}

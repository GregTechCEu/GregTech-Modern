package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Objects;

public class SpeedItemModule extends TieredItemModule {

    private static final double SPEED_ACCEL = 0.085D;
    private static final int ENERGY_USAGE = 819;

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

    public void setStrength(ModuleContext context, float strength) {
        if (!Float.isFinite(strength)) return;
        context.setData(context.getData(SpeedModuleData.class).withStrength(Mth.clamp(strength, 0, 1)));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext context, PanelSyncManager psm, int id) {
        return super.getSettings(context, psm, id)
                .num(Text.lang("gui.gtceu.item_module.power"), () -> getStrength(context),
                        value -> setStrength(context, (float) value), 0, 1,
                        value -> "%.0f%%".formatted(value * 100));
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), getTier() * 25);
    }

    @Override
    public void clientSideArmorTick(ModuleContext moduleContext, Player player) {
        if (!isEnabled(moduleContext) || getStrength(moduleContext) <= 0) return;

        var battery = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (battery == null || !battery.canUse(ENERGY_USAGE)) return;

        float strength = getStrength(moduleContext);
        float mul = (getTier() / 4f + 1) * strength;
        boolean sprinting = SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isSprinting();
        boolean jumping = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
        boolean sneaking = SyncedKeyMappings.VANILLA_SNEAK.isKeyDown(player);

        if ((player.onGround() || player.isInWater()) && sprinting) {
            float speed = 0.25F * mul;
            if (player.isInWater()) {
                speed = 0.1F * mul;
                if (jumping) {
                    player.push(0.0, 0.1 * strength, 0.0);
                }
            }
            player.moveRelative(speed, new Vec3(0, 0, 1));
        } else if (player.isInWater() && (sneaking || jumping)) {
            if (sneaking)
                player.push(0.0, -SPEED_ACCEL * mul, 0.0);
            if (jumping)
                player.push(0.0, SPEED_ACCEL * mul, 0.0);
        }
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
        public static final Codec<SpeedModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(
                Codec.floatRange(0, 1).optionalFieldOf("strength", 1f).forGetter(SpeedModuleData::getStrength)
        ).apply(instance, SpeedModuleData::new));
        // spotless:on

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

        public SpeedModuleData withStrength(float strength) {
            return new SpeedModuleData(slot, module, moduleItem, enabled, strength);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
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

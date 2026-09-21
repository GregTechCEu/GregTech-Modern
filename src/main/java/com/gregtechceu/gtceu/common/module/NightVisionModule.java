package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ModuleData;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public class NightVisionModule extends ItemModule {

    public NightVisionModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Codec<? extends ModuleData> moduleDataCodec() {
        return NightVisionModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return NightVisionModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new NightVisionModuleData(slot, module, moduleStack);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.nightvision", 2);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips, TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, level, tooltips, isAdvanced);
        tooltips.add(Component.translatable("metaarmor.message.nightvision.enabled"));
    }

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        super.onArmorTick(moduleContext, entity);
        if (!(entity instanceof Player player)) return;
        IElectricItem item = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (item == null) {
            return;
        }
        NightVisionModuleData data = moduleContext.getData(NightVisionModuleData.class);

        byte toggleTimer = data.getToggleTimer();
        int nightVisionTimer = data.getNightVisionTimer();
        boolean nightVision = data.isNightVision();

        if (toggleTimer == 0 && SyncedKeyMappings.ARMOR_MODE_SWITCH.isKeyDown(player)) {
            nightVision = !nightVision;
            toggleTimer = 5;
            if (item.getCharge() < ArmorUtils.MIN_NIGHTVISION_CHARGE) {
                nightVision = false;
                player.displayClientMessage(Component.translatable("metaarmor.nms.nightvision.error"), true);
            } else {
                player.displayClientMessage(Component
                        .translatable("metaarmor.nms.nightvision." + (nightVision ? "enabled" : "disabled")), true);
            }
        }

        if (nightVision) {
            player.removeEffect(MobEffects.BLINDNESS);
            if (nightVisionTimer <= ArmorUtils.NIGHT_VISION_RESET) {
                nightVisionTimer = ArmorUtils.NIGHTVISION_DURATION;
                player.addEffect(
                        new MobEffectInstance(MobEffects.NIGHT_VISION, ArmorUtils.NIGHTVISION_DURATION, 0, true,
                                false));
                item.discharge(2, item.getTier(), true, false, false);
            }
        } else {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }

        if (nightVisionTimer > 0) nightVisionTimer--;
        if (toggleTimer > 0) toggleTimer--;

        var newData = new NightVisionModuleData(data.getSlot(), data.getModule(), data.getModuleItem(),
                data.isEnabled(), nightVision, toggleTimer, nightVisionTimer);
        moduleContext.setData(newData);
    }

    public static class NightVisionModuleData extends ModuleData {

        // spotless:off
        public static final Codec<NightVisionModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(instance.group(
                Codec.BOOL.fieldOf("night_vision").forGetter(NightVisionModuleData::isNightVision),
                Codec.BYTE.fieldOf("toggle_timer").forGetter(NightVisionModuleData::getToggleTimer),
                Codec.INT.fieldOf("night_vision_timer").forGetter(NightVisionModuleData::getNightVisionTimer))
        ).apply(instance, NightVisionModuleData::new));
        //spotless:on

        @Getter
        private boolean nightVision = false;
        @Getter
        private byte toggleTimer = 0;
        @Getter
        private int nightVisionTimer = ArmorUtils.NIGHTVISION_DURATION;

        public NightVisionModuleData(int slot, ItemModule module, ItemStack moduleItem) {
            super(slot, module, moduleItem, true);
        }

        public NightVisionModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                     boolean nightVision, byte toggleTimer, int nightVisionTimer) {
            super(slot, module, moduleItem, enabled);
            this.nightVision = nightVision;
            this.toggleTimer = toggleTimer;
            this.nightVisionTimer = nightVisionTimer;
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new NightVisionModuleData(slot, module, moduleItem, enabled, nightVision, toggleTimer,
                    nightVisionTimer);
        }

        @Override
        public ModuleData copy() {
            return new NightVisionModuleData(slot, module, moduleItem.copy(), enabled, nightVision, toggleTimer,
                    nightVisionTimer);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NightVisionModuleData other)) return false;
            return super.equals(obj) && nightVision == other.nightVision && toggleTimer == other.toggleTimer &&
                    nightVisionTimer == other.nightVisionTimer;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, nightVision, toggleTimer, nightVisionTimer);
        }
    }
}

package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
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

public class NightVisionModule extends ItemModule {

    // spotless:off
    public static final Codec<NightVisionModule> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(instance.group(
            Codec.BOOL.fieldOf("night_vision").forGetter(NightVisionModule::isNightVision),
            Codec.BYTE.fieldOf("toggle_timer").forGetter(NightVisionModule::getToggleTimer),
            Codec.INT.fieldOf("night_vision_timer").forGetter(NightVisionModule::getNightVisionTimer))
    ).apply(instance, NightVisionModule::new));
    //spotless:on

    @Getter
    private boolean nightVision = false;
    @Getter
    private byte toggleTimer = 0;
    @Getter
    private int nightVisionTimer = ArmorUtils.NIGHTVISION_DURATION;

    public NightVisionModule(boolean isEnabled, ItemStack moduleItem, boolean nightVisionEnabled, byte toggleTimer,
                             int nightVisionTimer) {
        super(isEnabled, moduleItem);
        this.nightVision = nightVisionEnabled;
        this.toggleTimer = toggleTimer;
        this.nightVisionTimer = nightVisionTimer;
    }

    public NightVisionModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public NightVisionModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    @Override
    public ItemModuleType<NightVisionModule> type() {
        return GTItemModules.NIGHT_VISION;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.nightvision", 2);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.message.nightvision.enabled"));
    }

    @Override
    public void onArmorTick(LivingEntity entity) {
        super.onArmorTick(entity);
        if (!(entity instanceof Player player)) return;
        IElectricItem item = GTCapabilityHelper.getElectricItem(getAppliedTo());
        if (item == null) {
            return;
        }

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

        getModularItemStack().saveModuleData();
    }
}

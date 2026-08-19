package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class NightVisionModule extends ItemModule {

    public NightVisionModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.nightvision", 2);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.message.nightvision.enabled"));
    }

    @Override
    public void onArmorTick(LivingEntity entity, AppliedItemModule module) {
        super.onArmorTick(entity, module);
        if (!(entity instanceof Player player)) return;
        IElectricItem item = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        if (item == null) {
            return;
        }
        CompoundTag data = module.getTag();
        byte toggleTimer = data.contains("toggleTimer") ? data.getByte("toggleTimer") : 0;
        int nightVisionTimer = data.contains("nightVisionTimer") ? data.getInt("nightVisionTimer") :
                ArmorUtils.NIGHTVISION_DURATION;
        boolean nightVision = data.contains("nightVision") && data.getBoolean("nightVision");
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
        data.putBoolean("nightVision", nightVision);

        if (nightVisionTimer > 0) nightVisionTimer--;
        if (toggleTimer > 0) toggleTimer--;

        data.putInt("nightVisionTimer", nightVisionTimer);
        data.putByte("toggleTimer", toggleTimer);
    }
}

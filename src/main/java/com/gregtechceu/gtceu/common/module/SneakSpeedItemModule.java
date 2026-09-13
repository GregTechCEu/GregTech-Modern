package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.Codec;

import java.util.List;

public class SneakSpeedItemModule extends TieredItemModule {

    // spotless:off
    public static final Codec<SneakSpeedItemModule> CODEC = tieredSimpleCodec(SneakSpeedItemModule::new);
    //spotless:on

    public SneakSpeedItemModule(boolean isEnabled, ItemStack moduleItem, int tier) {
        super(isEnabled, moduleItem, tier);
    }

    public SneakSpeedItemModule(ItemStack attachItem, int tier) {
        super(attachItem, tier);
    }

    @Override
    public ItemModuleType<SneakSpeedItemModule> type() {
        return GTItemModules.SNEAK_SPEED[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.sneak_speed", getTier() * 100 / 8f);
    }

    @Override
    public void onArmorTick(LivingEntity entity) {
        super.onArmorTick(entity);
        if (entity instanceof Player player) {
            float mul = getTier() / 8f + 1;
            boolean jumping = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
            boolean sneaking = SyncedKeyMappings.VANILLA_SNEAK.isKeyDown(player);

            if (player.onGround() && sneaking) {
                float speed = 0.25F * mul;
                if (player.isInWater()) {
                    speed = 0.1F * mul;
                    if (jumping) {
                        player.push(0.0, 0.1, 0.0);
                    }
                }
                player.moveRelative(speed, new Vec3(0, 0, 1));
            }
        }
    }

    @Override
    public boolean useEnergyInInventory(LivingEntity entity) {
        return false;
    }

    @Override
    public long energyUsagePerTick(LivingEntity entity) {
        if (entity instanceof Player player) {
            return SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isShiftKeyDown() ? 819 : 0;
        }
        return 0;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.sneak_speed",
                GTValues.VNF[getTier()]));
    }
}

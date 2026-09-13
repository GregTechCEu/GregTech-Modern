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

public class SpeedItemModule extends TieredItemModule {

    // spotless:off
    public static final Codec<SpeedItemModule> CODEC = tieredSimpleCodec(SpeedItemModule::new);
    //spotless:on

    public SpeedItemModule(boolean isEnabled, ItemStack moduleItem, int tier) {
        super(isEnabled, moduleItem, tier);
    }

    public SpeedItemModule(ItemStack attachItem, int tier) {
        super(attachItem, tier);
    }

    @Override
    public ItemModuleType<SpeedItemModule> type() {
        return GTItemModules.SPEED[getTier()];
    }

    private static final double SPEED_ACCEL = 0.085D;

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.speed", getTier() * 25);
    }

    @Override
    public void onArmorTick(LivingEntity entity) {
        super.onArmorTick(entity);
        if (entity instanceof Player player) {
            float mul = getTier() / 4f + 1;
            boolean sprinting = SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isSprinting();
            boolean jumping = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
            boolean sneaking = SyncedKeyMappings.VANILLA_SNEAK.isKeyDown(player);

            if ((player.onGround() || player.isInWater()) && sprinting) {
                float speed = 0.25F * mul;
                if (player.isInWater()) {
                    speed = 0.1F * mul;
                    if (jumping) {
                        player.push(0.0, 0.1, 0.0);
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
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.speed", GTValues.VNF[getTier()]));
    }

    @Override
    public long energyUsagePerTick(LivingEntity entity) {
        if (entity instanceof Player player) {
            return SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isSprinting() ? 819 : 0;
        }
        return 0;
    }

    @Override
    public boolean useEnergyInInventory(LivingEntity entity) {
        return false;
    }
}

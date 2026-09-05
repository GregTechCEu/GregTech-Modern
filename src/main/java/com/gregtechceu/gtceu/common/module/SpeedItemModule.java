package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SpeedItemModule extends TieredItemModule {

    private static final double SPEED_ACCEL = 0.085D;

    public SpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.speed", getTier() * 25);
    }

    @Override
    public void onArmorTick(LivingEntity entity, AppliedItemModule modifier) {
        super.onArmorTick(entity, modifier);
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
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.speed", GTValues.VNF[getTier()]));
    }

    @Override
    public long energyUsagePerTick(LivingEntity entity, AppliedItemModule module) {
        if (entity instanceof Player player) {
            return SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isSprinting() ? 819 : 0;
        }
        return 0;
    }

    @Override
    public boolean useEnergyInInventory(LivingEntity entity, AppliedItemModule module) {
        return false;
    }
}

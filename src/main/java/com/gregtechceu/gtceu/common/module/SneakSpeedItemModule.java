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

public class SneakSpeedItemModule extends TieredItemModule {

    public SneakSpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.sneak_speed", getTier() * 100 / 8f);
    }

    @Override
    public void onArmorTick(LivingEntity entity, AppliedItemModule module) {
        super.onArmorTick(entity, module);
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
    public boolean useEnergyInInventory(LivingEntity entity, AppliedItemModule module) {
        return false;
    }

    @Override
    public long energyUsagePerTick(LivingEntity entity, AppliedItemModule module) {
        if (entity instanceof Player player) {
            return SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isShiftKeyDown() ? 819 : 0;
        }
        return 0;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.sneak_speed",
                GTValues.VNF[getTier()]));
    }
}

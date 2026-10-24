package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SneakSpeedItemModule extends TieredItemModule {

    private final double sneakSpeedBonus;
    private static final int ENERGY_USAGE = 819;

    public SneakSpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
        sneakSpeedBonus = tier >= GTValues.EV ? 0.7 : tier * 0.15;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.sneak_speed.description", Math.round((0.3 + sneakSpeedBonus) * 100),
                Math.max(0.25, ArmorUtils.getModuleEnergyCost(getTier()) / 2));
    }

    @Override
    public void clientSideArmorTick(ModuleContext moduleContext, Player player) {
        if (!isEnabled(moduleContext)) return;
        float mul = getTier() / 8f + 1;
        boolean jumping = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
        boolean sneaking = SyncedKeyMappings.VANILLA_SNEAK.isKeyDown(player);
        boolean movingForward = SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player);

        var battery = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (battery == null || !battery.canUse(ENERGY_USAGE)) return;

        if (player.onGround() && sneaking && movingForward) {
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

    @Override
    public long energyUsagePerTick(ModuleContext moduleContext, LivingEntity entity) {
        if (entity instanceof Player player) {
            return SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isShiftKeyDown() ? ENERGY_USAGE : 0;
        }
        return 0;
    }
}

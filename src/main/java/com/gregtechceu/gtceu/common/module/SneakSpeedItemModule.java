package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class SneakSpeedItemModule extends TieredItemModule {

    private static final int ENERGY_USAGE = 819;
    private final double sneakSpeedBonus;

    public SneakSpeedItemModule(ResourceLocation id, int tier) {
        super(id, tier);
        sneakSpeedBonus = tier >= GTValues.EV ? 0.7 : tier * 0.15;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.sneak_speed.description", Math.round((0.3 + sneakSpeedBonus) * 100));
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
        var attribute = player.getAttribute(Attributes.SNEAKING_SPEED);
        if (attribute == null) return;
        var battery = GTCapabilityHelper.getElectricItem(context.getAppliedTo());
        boolean active = isEnabled(context) &&
                battery != null && battery.canUse(ENERGY_USAGE);
        if (!active) {
            attribute.removeModifier(getId());
            return;
        }
        var modifier = new AttributeModifier(getId(), sneakSpeedBonus, AttributeModifier.Operation.ADD_VALUE);
        if (!modifier.equals(attribute.getModifier(getId()))) attribute.addOrUpdateTransientModifier(modifier);
    }

    @Override
    public void onUnequip(ModuleContext context, LivingEntity entity) {
        var attribute = entity.getAttribute(Attributes.SNEAKING_SPEED);
        if (attribute != null) attribute.removeModifier(getId());
    }

    @Override
    public long energyUsagePerTick(ModuleContext moduleContext, LivingEntity entity) {
        if (entity instanceof Player player) {
            boolean moving = SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) ||
                    SyncedKeyMappings.VANILLA_BACKWARD.isKeyDown(player) ||
                    SyncedKeyMappings.VANILLA_LEFT.isKeyDown(player) ||
                    SyncedKeyMappings.VANILLA_RIGHT.isKeyDown(player);
            return moving && (player.isCrouching() || player.isVisuallyCrawling()) ? ENERGY_USAGE : 0;
        }
        return 0;
    }
}

package com.lowdragmc.gtceu.api.data.damagesource;


import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DamageSourceTool extends EntityDamageSource {

    private final String deathMessage;

    public DamageSourceTool(String type, LivingEntity entity, String deathMessage) {
        super(type, entity);
        this.deathMessage = deathMessage;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        if (deathMessage == null || !LocalizationUtils.exist(deathMessage)) return super.getLocalizedDeathMessage(livingEntity);
        return Component.translatable(deathMessage, livingEntity.getDisplayName(), livingEntity.getDisplayName());
    }

}

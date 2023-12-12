package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Add to tools to have them deal bonus damage to specific mobs.
 * Pass null for the mobType parameter to ignore the tooltip.
 */
public class EntityDamageBehavior implements IToolBehavior {

    private final List<Function<LivingEntity, Float>> shouldDoBonusList = new ArrayList<>();
    private final String mobType;

    public EntityDamageBehavior(float bonus, Class<?>... entities) {
        this(null, bonus, entities);
    }

    public EntityDamageBehavior(Map<Class<?>, Float> entities) {
        this(null, entities);
    }

    public EntityDamageBehavior(String mobType, float bonus, Class<?>... entities) {
        this.mobType = mobType;
        for (Class<?> entity : entities) {
            shouldDoBonusList.add(e -> entity.isAssignableFrom(e.getClass()) ? bonus : 0);
        }
    }

    public EntityDamageBehavior(String mobType, Map<Class<?>, Float> entities) {
        this.mobType = mobType;
        for (Map.Entry<Class<?>, Float> entry : entities.entrySet()) {
            Class<?> entity = entry.getKey();
            float bonus = entry.getValue();
            shouldDoBonusList.add(e -> entity.isAssignableFrom(e.getClass()) ? bonus : 0);
        }
    }

    @Override
    public void hitEntity(@NotNull ItemStack stack, @NotNull LivingEntity target,
                          @NotNull LivingEntity attacker) {
        float damageBonus = shouldDoBonusList.stream().map(func -> func.apply(target)).filter(f -> f > 0).findFirst()
                .orElse(0f);
        if (damageBonus != 0f) {
            DamageSource source = attacker instanceof Player player ?
                    attacker.damageSources().playerAttack(player) : attacker.damageSources().mobAttack(attacker);
            target.hurt(source, damageBonus);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        if (mobType != null && !mobType.isEmpty()) {
            tooltip.add(Component.translatable("item.gt.tool.behavior.damage_boost",
                    Component.translatable("item.gt.tool.behavior.damage_boost_" + mobType)));
        }
    }
}
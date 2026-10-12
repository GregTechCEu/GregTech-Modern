package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.api.item.module.AttributeItemModule;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
public class CreativeFlightModule extends AttributeItemModule implements ITieredItemModule {

    public static final int ENERGY_PER_TICK = 2048;
    private static final String FLIGHT_BOOST = "gtceu:thruster_flight_boost";

    public CreativeFlightModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), ENERGY_PER_TICK);
    }

    private boolean isFlying(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().flying;
        } else return false;
    }

    @Override
    public Holder<Attribute> getAttribute(ModuleContext moduleContext) {
        return NeoForgeMod.CREATIVE_FLIGHT;
    }

    @Override
    public double getModifier(ModuleContext moduleContext) {
        return 1;
    }

    @Override
    protected double getNeutralModifier(ModuleContext moduleContext) {
        return 1;
    }

    @Override
    public AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_VALUE;
    }

    @Override
    public double getMaxAttributeAmount() {
        return 1;
    }

    @Override
    public Component getDisplayName(ModuleContext moduleContext) {
        return Component.translatable(getLanguageKey()).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        return super.canApplyTo(stack) && modular.getAllModuleInstances().stream()
                .noneMatch(context -> context.getModule() instanceof LiquidFuelJetpackModule);
    }

    @Override
    public boolean keepAttributeActive(ModuleContext moduleContext, LivingEntity entity) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (!isEnabled(moduleContext) || electricItem == null || !electricItem.canUse(ENERGY_PER_TICK)) return false;
        if (!isFlying(entity)) return true;
        if (entity.level().isClientSide) return true;

        long cost = ENERGY_PER_TICK;
        var player = (Player) entity;
        var modular = GTCapabilityHelper.getModularItem(moduleContext.getAppliedTo());
        if (player.isSprinting() && !player.isSpectator() && modular != null &&
                moduleContext.getAppliedTo().getOrDefault(GTDataComponents.ARMOR_DATA, GTArmor.EMPTY).enabled()) {
            for (var context : modular.getAllModuleInstances()) {
                if (context.getModule() instanceof JetpackModule thruster && thruster.isEnabled(context)) {
                    var logic = thruster.getArmorLogic(context);
                    long combinedCost = cost + Math.round(logic.getEnergyPerUse() * logic.getSprintEnergyModifier());
                    if (electricItem.canUse(combinedCost)) {
                        cost = combinedCost;
                        applyFlightBoost(player, thruster instanceof AdvancedJetpackModule ?
                                (float) logic.getSprintSpeedModifier() : 1.3f);
                    }
                    break;
                }
            }
        }
        electricItem.discharge(cost, electricItem.getTier(), true, false, false);
        return true;
    }

    private static void applyFlightBoost(Player player, float multiplier) {
        var data = player.getPersistentData();
        var boost = data.getCompound(FLIGHT_BOOST);
        float current = player.getAbilities().getFlyingSpeed();
        if (!boost.contains("base") || Float.compare(current, boost.getFloat("applied")) != 0) {
            boost.putFloat("base", current);
        }
        float speed = boost.getFloat("base") * multiplier;
        boost.putFloat("applied", speed);
        boost.putLong("tick", player.level().getGameTime());
        data.put(FLIGHT_BOOST, boost);
        if (Float.compare(current, speed) != 0) {
            player.getAbilities().setFlyingSpeed(speed);
            player.onUpdateAbilities();
        }
    }

    private static void restoreFlightSpeed(Player player) {
        var data = player.getPersistentData();
        if (!data.contains(FLIGHT_BOOST)) return;
        CompoundTag boost = data.getCompound(FLIGHT_BOOST);
        // Keep speed changes made by other mods after our last update.
        if (Float.compare(player.getAbilities().getFlyingSpeed(), boost.getFloat("applied")) == 0) {
            player.getAbilities().setFlyingSpeed(boost.getFloat("base"));
            player.onUpdateAbilities();
        }
        data.remove(FLIGHT_BOOST);
    }

    @SubscribeEvent
    public static void afterPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.level().isClientSide && player.getPersistentData().contains(FLIGHT_BOOST) &&
                player.getPersistentData().getCompound(FLIGHT_BOOST).getLong("tick") != player.level().getGameTime()) {
            restoreFlightSpeed(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        restoreFlightSpeed(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        restoreFlightSpeed(event.getEntity());
    }

    @Override
    public int getTier() {
        return GTValues.LuV;
    }
}

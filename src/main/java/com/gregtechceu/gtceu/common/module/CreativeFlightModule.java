package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID)
public class CreativeFlightModule extends ItemModule implements ITieredItemModule {

    public static final int ENERGY_PER_TICK = 2048;
    private static final String FLIGHT_BOOST = "gtceu:thruster_flight_boost";

    public CreativeFlightModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey(), ENERGY_PER_TICK);
    }

    private void setMayFly(LivingEntity entity, boolean mayFly) {
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return;
            player.getAbilities().mayfly = mayFly;
            if (!mayFly) player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    private boolean isFlying(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().flying;
        } else return false;
    }

    @Override
    public void onEquip(ModuleContext moduleContext, LivingEntity entity) {
        super.onEquip(moduleContext, entity);
        setMayFly(entity, true);
    }

    @Override
    public void onUnequip(ModuleContext moduleContext, LivingEntity entity) {
        super.onUnequip(moduleContext, entity);
        setMayFly(entity, false);
    }

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (!isEnabled(moduleContext) || electricItem == null || !electricItem.canUse(ENERGY_PER_TICK)) {
            setMayFly(entity, false);
            return;
        }
        if (!isFlying(entity)) {
            setMayFly(entity, true);
            return;
        }
        if (entity.level().isClientSide) return;

        long cost = ENERGY_PER_TICK;
        var player = (Player) entity;
        var modular = GTCapabilityHelper.getModularItem(moduleContext.getAppliedTo());
        if (player.isSprinting() && !player.isSpectator() && modular != null &&
                moduleContext.getAppliedTo().getTag() != null &&
                moduleContext.getAppliedTo().getTag().getBoolean("enabled")) {
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
        setMayFly(entity, true);
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
    public static void afterPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.player.level().isClientSide && event.player.getPersistentData().contains(FLIGHT_BOOST) &&
                event.player.getPersistentData().getCompound(FLIGHT_BOOST).getLong("tick") !=
                        event.player.level().getGameTime()) {
            restoreFlightSpeed(event.player);
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

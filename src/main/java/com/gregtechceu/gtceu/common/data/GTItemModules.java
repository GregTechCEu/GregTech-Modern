package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.module.*;

import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GTItemModules {

    // spotless:off
    public static final ItemModuleSlot UNIVERSAL_SLOT = new UniversalItemModuleSlot();
    public static final TieredItemModuleSlot[] TIERED_SLOTS = TieredItemModuleSlot.create(TieredItemModuleSlot::new);

    public static final RegistryEntry<ItemModule, SpeedItemModule>[] SPEED = registerTiered(GTCEu.id("speed"), SpeedItemModule::new);
    public static final RegistryEntry<ItemModule, EnergyShieldItemModule>[] DAMAGE_BLOCK = registerTiered(GTCEu.id("damage_block"), EnergyShieldItemModule::new);
    public static final RegistryEntry<ItemModule, AttackSpeedItemModule>[] ATTACK_SPEED = registerTiered(GTCEu.id("attack_speed"), AttackSpeedItemModule::new);
    public static final RegistryEntry<ItemModule, AttackDamageItemModule>[] ATTACK_DAMAGE = registerTiered(GTCEu.id("attack_damage"), AttackDamageItemModule::new);
    public static final RegistryEntry<ItemModule, BlockReachItemModule>[] BLOCK_REACH = registerTiered(GTCEu.id("block_reach"), BlockReachItemModule::new);
    public static final RegistryEntry<ItemModule, MovementSpeedItemModule>[] MOVEMENT_SPEED_ATTR = registerTiered(GTCEu.id("movement_speed"), MovementSpeedItemModule::new);
    public static final RegistryEntry<ItemModule, SneakSpeedItemModule>[] SNEAK_SPEED = registerTiered(GTCEu.id("sneak_speed"), SneakSpeedItemModule::new);
    public static final RegistryEntry<ItemModule, SwimSpeedModule>[] SWIM_SPEED = registerTiered(GTCEu.id("swim_speed"), SwimSpeedModule::new);
    public static final RegistryEntry<ItemModule, StepHeightModule>[] STEP_HEIGHT = registerTiered(GTCEu.id("step_height"), StepHeightModule::new);
    public static final RegistryEntry<ItemModule, JumpBoostItemModule>[] JUMP_BOOST = registerTiered(GTCEu.id("jump_boost"), JumpBoostItemModule::new);
    public static final RegistryEntry<ItemModule, SensorItemModule>[] SENSOR = registerTiered(GTCEu.id("sensor"), SensorItemModule::new);
    public static final RegistryEntry<ItemModule, AutoChargeItemModule>[] WIRELESS_CHARGER = registerTiered(GTCEu.id("wireless_charger"), AutoChargeItemModule::new);
    public static final RegistryEntry<ItemModule, AutoEatModule> AUTO_EAT = register(GTCEu.id("auto_eat"), AutoEatModule::new);
    public static final RegistryEntry<ItemModule, AirSupplierModule> AIR_SUPPLIER = register(GTCEu.id("air_supplier"), AirSupplierModule::new);
    public static final RegistryEntry<ItemModule, BatteryItemModule> BATTERY = register(GTCEu.id("battery"), BatteryItemModule::new);
    public static final RegistryEntry<ItemModule, NightVisionModule> NIGHT_VISION = register(GTCEu.id("night_vision"), NightVisionModule::new);
    public static final RegistryEntry<ItemModule, PPEModule> PPE = register(GTCEu.id("ppe"), PPEModule::new);
    public static final RegistryEntry<ItemModule, LiquidFuelJetpackModule> LIQUID_FUEL_JETPACK = register(GTCEu.id("liquid_fuel_jetpack"), LiquidFuelJetpackModule::new);
    public static final RegistryEntry<ItemModule, JetpackModule> JETPACK = register(GTCEu.id("jetpack"), JetpackModule::new);
    public static final RegistryEntry<ItemModule, AdvancedJetpackModule> ADVANCED_JETPACK = register(GTCEu.id("advanced_jetpack"), AdvancedJetpackModule::new);
    public static final RegistryEntry<ItemModule, CreativeFlightModule> CREATIVE_FLIGHT = register(GTCEu.id("creative_flight"), CreativeFlightModule::new);
    public static final RegistryEntry<ItemModule, FluidStorageModule> FLUID_STORAGE = register(GTCEu.id("fluid_storage"), FluidStorageModule::new);
    // spotless:on

    private static <T extends ItemModule> RegistryEntry<ItemModule, T> register(ResourceLocation id, Function<ResourceLocation, T> factory) {
        return GTRegistration.REGISTRATE.simple(id.getPath(), GTRegistries.Keys.ITEM_MODULE, () -> factory.apply(id));
    }

    @SuppressWarnings("unchecked")
    public static <T extends TieredItemModule> RegistryEntry<ItemModule, T>[] registerTiered(GTRegistrate registrate, ResourceLocation id,
                                                    BiFunction<ResourceLocation, Integer, T> constructor,
                                                    int... tiers) {
        RegistryEntry<ItemModule, T>[] result = new RegistryEntry[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            ResourceLocation resourceLocation = id.withPrefix(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_");
            result[tier] = registrate.simple(resourceLocation.getPath(), GTRegistries.Keys.ITEM_MODULE, () -> {
                var module = constructor.apply(resourceLocation, tier);
                module.setOtherTierModules(result);
                return module;
            });
        }
        return result;
    }

    public static <T extends TieredItemModule> RegistryEntry<ItemModule, T>[] registerTiered(GTRegistrate registrate, ResourceLocation id,
                                                    BiFunction<ResourceLocation, Integer, T> constructor) {
        return registerTiered(registrate, id, constructor, GTValues.tiersBetween(GTValues.LV, GTValues.OpV));
    }

    private static <T extends TieredItemModule> RegistryEntry<ItemModule, T>[] registerTiered(ResourceLocation id,
                                                    BiFunction<ResourceLocation, Integer, T> constructor) {
        return registerTiered(GTRegistration.REGISTRATE, id, constructor, GTValues.tiersBetween(GTValues.LV, GTValues.OpV));
    }

    public static void init() {}
}

package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.module.*;

import com.mojang.datafixers.types.Func;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GTItemModules {

    static {
        GTRegistries.ITEM_MODULES.unfreeze();
    }

    //spotless:on
    public static final ItemModuleSlot UNIVERSAL_SLOT = new UniversalItemModuleSlot();
    public static final TieredItemModuleSlot[] TIERED_SLOTS = TieredItemModuleSlot.create(TieredItemModuleSlot::new);

    public static final ItemModule[] SPEED = registerTiered(GTCEu.id("speed"), SpeedItemModule::new);
    public static final ItemModule[] DAMAGE_BLOCK = registerTiered(GTCEu.id("damage_block"), EnergyShieldItemModule::new);
    public static final ItemModule[] ATTACK_SPEED = registerTiered(GTCEu.id("attack_speed"), AttackSpeedItemModule::new);
    public static final ItemModule[] ATTACK_DAMAGE = registerTiered(GTCEu.id("attack_damage"), AttackDamageItemModule::new);
    public static final ItemModule[] BLOCK_REACH = registerTiered(GTCEu.id("block_reach"), BlockReachItemModule::new);
    public static final ItemModule[] MOVEMENT_SPEED_ATTR = registerTiered(GTCEu.id("movement_speed"), MovementSpeedItemModule::new);
    public static final ItemModule[] SNEAK_SPEED = registerTiered(GTCEu.id("sneak_speed"), SneakSpeedItemModule::new);
    public static final ItemModule[] SWIM_SPEED = registerTiered(GTCEu.id("swim_speed"), SwimSpeedModule::new);
    public static final ItemModule[] STEP_HEIGHT = registerTiered(GTCEu.id("step_height"), StepHeightModule::new);
    public static final ItemModule[] JUMP_BOOST = registerTiered(GTCEu.id("jump_boost"), JumpBoostItemModule::new);
    public static final ItemModule[] SENSOR = registerTiered(GTCEu.id("sensor"), SensorItemModule::new);
    public static final ItemModule[] WIRELESS_CHARGER = registerTiered(GTCEu.id("wireless_charger"), AutoChargeItemModule::new);
    public static final ItemModule AUTO_EAT = register(GTCEu.id("auto_eat"), AutoEatModule::new);
    public static final ItemModule AIR_SUPPLIER = register(GTCEu.id("air_supplier"), AirSupplierModule::new);
    public static final ItemModule BATTERY = register(GTCEu.id("battery"), BatteryItemModule::new);
    public static final ItemModule NIGHT_VISION = register(GTCEu.id("night_vision"), NightVisionModule::new);
    public static final ItemModule PPE = register(GTCEu.id("ppe"), PPEModule::new);
    public static final ItemModule LIQUID_FUEL_JETPACK = register(GTCEu.id("liquid_fuel_jetpack"), LiquidFuelJetpackModule::new);
    public static final ItemModule JETPACK = register(GTCEu.id("jetpack"), JetpackModule::new);
    public static final ItemModule ADVANCED_JETPACK = register(GTCEu.id("advanced_jetpack"), AdvancedJetpackModule::new);
    public static final ItemModule CREATIVE_FLIGHT = register(GTCEu.id("creative_flight"), CreativeFlightModule::new);
    public static final ItemModule FLUID_STORAGE = register(GTCEu.id("fluid_storage"), FluidStorageModule::new);
    //spotless:off
    public static <T extends ItemModule> T register(ResourceLocation id, Function<ResourceLocation, T> factory) {
        T module = factory.apply(id);
        GTRegistries.ITEM_MODULES.register(id, module);
        return module;
    }

    public static TieredItemModule[] registerTiered(ResourceLocation id,
                                                    BiFunction<ResourceLocation, Integer, TieredItemModule> constructor,
                                                    int... tiers) {
        TieredItemModule[] result = new TieredItemModule[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            ResourceLocation resourceLocation = id.withPrefix(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_");
            result[tier] = constructor.apply(resourceLocation, tier);
            result[tier].setOtherTierModules(result);
            GTRegistries.ITEM_MODULES.register(resourceLocation, result[tier]);
        }
        return result;
    }

    public static TieredItemModule[] registerTiered(ResourceLocation id,
                                                    BiFunction<ResourceLocation, Integer, TieredItemModule> constructor) {
        return registerTiered(id, constructor, GTValues.tiersBetween(GTValues.LV, GTValues.OpV));
    }

    public static void init() {}
}

package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.module.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;

import java.util.function.BiFunction;
import java.util.function.Function;

public class GTItemModules {

    static {
        GTRegistries.ITEM_MODULES.unfreeze();
    }

    // spotless:off
    public static final ItemModuleSlot UNIVERSAL_SLOT = new UniversalItemModuleSlot(GTCEu.id("universal"));
    public static final TieredItemModuleSlot[] TIERED_SLOTS = TieredItemModuleSlot.create(GTCEu.id("tiered"),
            TieredItemModuleSlot::new);

    public static final ItemModuleType<SpeedItemModule>[] SPEED = registerTiered(GTCEu.id("speed"), SpeedItemModule.CODEC, SpeedItemModule::new);
    public static final ItemModuleType<EnergyShieldItemModule>[] DAMAGE_BLOCK = registerTiered(GTCEu.id("damage_block"), EnergyShieldItemModule.CODEC, EnergyShieldItemModule::new);
    public static final ItemModuleType<AttackSpeedItemModule>[] ATTACK_SPEED = registerTiered(GTCEu.id("attack_speed"), AttackSpeedItemModule.CODEC, AttackSpeedItemModule::new);
    public static final ItemModuleType<AttackDamageItemModule>[] ATTACK_DAMAGE = registerTiered(GTCEu.id("attack_damage"), AttackDamageItemModule.CODEC, AttackDamageItemModule::new);
    public static final ItemModuleType<BlockReachItemModule>[] BLOCK_REACH = registerTiered(GTCEu.id("block_reach"), BlockReachItemModule.CODEC, BlockReachItemModule::new);
    public static final ItemModuleType<MovementSpeedItemModule>[] MOVEMENT_SPEED_ATTR = registerTiered(GTCEu.id("movement_speed"), MovementSpeedItemModule.CODEC, MovementSpeedItemModule::new);
    public static final ItemModuleType<SneakSpeedItemModule>[] SNEAK_SPEED = registerTiered(GTCEu.id("sneak_speed"), SneakSpeedItemModule.CODEC, SneakSpeedItemModule::new);
    public static final ItemModuleType<SwimSpeedModule>[] SWIM_SPEED = registerTiered(GTCEu.id("swim_speed"), SwimSpeedModule.CODEC, SwimSpeedModule::new);
    public static final ItemModuleType<StepHeightModule>[] STEP_HEIGHT = registerTiered(GTCEu.id("step_height"), StepHeightModule.CODEC, StepHeightModule::new);
    public static final ItemModuleType<JumpBoostItemModule>[] JUMP_BOOST = registerTiered(GTCEu.id("jump_boost"), JumpBoostItemModule.CODEC, JumpBoostItemModule::new);
    public static final ItemModuleType<SensorItemModule>[] SENSOR = registerTiered(GTCEu.id("sensor"), SensorItemModule.CODEC, SensorItemModule::new);
    public static final ItemModuleType<AutoChargeItemModule>[] WIRELESS_CHARGER = registerTiered(GTCEu.id("wireless_charger"), AutoChargeItemModule.CODEC, AutoChargeItemModule::new);
    public static final ItemModuleType<AutoEatModule> AUTO_EAT = register(GTCEu.id("auto_eat"), AutoEatModule.CODEC, AutoEatModule::new);
    public static final ItemModuleType<AirSupplierModule> AIR_SUPPLIER = register(GTCEu.id("air_supplier"), AirSupplierModule.CODEC, AirSupplierModule::new);
    public static final ItemModuleType<BatteryItemModule> BATTERY = register(GTCEu.id("battery"), BatteryItemModule.CODEC, BatteryItemModule::new);
    public static final ItemModuleType<NightVisionModule> NIGHT_VISION = register(GTCEu.id("night_vision"), NightVisionModule.CODEC, NightVisionModule::new);
    public static final ItemModuleType<PPEModule> PPE = register(GTCEu.id("ppe"), PPEModule.CODEC, PPEModule::new);
    public static final ItemModuleType<LiquidFuelJetpackModule> LIQUID_FUEL_JETPACK = register(GTCEu.id("liquid_fuel_jetpack"), LiquidFuelJetpackModule.CODEC, LiquidFuelJetpackModule::new);
    public static final ItemModuleType<JetpackModule> JETPACK = register(GTCEu.id("jetpack"), JetpackModule.CODEC, JetpackModule::new);
    public static final ItemModuleType<AdvancedJetpackModule> ADVANCED_JETPACK = register(GTCEu.id("advanced_jetpack"), AdvancedJetpackModule.CODEC, AdvancedJetpackModule::new);
    public static final ItemModuleType<CreativeFlightModule> CREATIVE_FLIGHT = register(GTCEu.id("creative_flight"), CreativeFlightModule.CODEC, CreativeFlightModule::new);
    public static final ItemModuleType<FluidStorageModule> FLUID_STORAGE = register(GTCEu.id("fluid_storage"), FluidStorageModule.CODEC, FluidStorageModule::new);
    //spotless:on

    public static <T extends ItemModule> ItemModuleType<T> register(ResourceLocation id, Codec<T> codec,
                                                                    Function<ItemStack, T> defaultInstance) {
        ItemModuleType<T> type = new ItemModuleType<>(codec, defaultInstance);
        GTRegistries.ITEM_MODULES.register(id, type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <
            T extends TieredItemModule> ItemModuleType<T>[] registerTiered(ResourceLocation id, int minTier,
                                                                           int maxTier, Codec<T> codec,
                                                                           BiFunction<ItemStack, Integer, T> constructor) {
        ItemModuleType<T>[] result = new ItemModuleType[maxTier - minTier + 1];
        for (int i = 0; i <= maxTier - minTier; i++) {
            int finalI = i;

            ResourceLocation resourceLocation = id.withSuffix("_" + (i + minTier));
            result[i] = new ItemModuleType<>(codec, s -> constructor.apply(s, finalI));
            GTRegistries.ITEM_MODULES.register(resourceLocation, result[i]);
        }
        return result;
    }

    public static <
            T extends TieredItemModule> ItemModuleType<T>[] registerTiered(ResourceLocation id, Codec<T> codec,
                                                                           BiFunction<ItemStack, Integer, T> constructor) {
        return registerTiered(id, GTValues.ULV, GTValues.MAX, codec, constructor);
    }

    public static void init() {}
}

package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.common.module.*;

public class GTItemModules {

    public static final ItemModuleSlot UNIVERSAL_SLOT = new UniversalItemModuleSlot(GTCEu.id("universal"));
    public static final TieredItemModuleSlot[] TIERED_SLOTS = TieredItemModuleSlot.create(GTCEu.id("tiered"),
            TieredItemModuleSlot::new);

    public static final ItemModule[] SPEED = TieredItemModule.create(GTCEu.id("speed"), SpeedItemModule::new);
    public static final ItemModule[] DAMAGE_BLOCK = TieredItemModule.create(GTCEu.id("damage_block"),
            EnergyShieldItemModule::new);
    public static final ItemModule[] ATTACK_SPEED = TieredItemModule.create(GTCEu.id("attack_speed"),
            AttackSpeedItemModule::new);
    public static final ItemModule[] ATTACK_DAMAGE = TieredItemModule.create(GTCEu.id("attack_damage"),
            AttackDamageItemModule::new);
    public static final ItemModule[] BLOCK_REACH = TieredItemModule.create(GTCEu.id("block_reach"),
            BlockReachItemModule::new);
    public static final ItemModule[] MOVEMENT_SPEED_ATTR = TieredItemModule.create(GTCEu.id("movement_speed"),
            MovementSpeedItemModule::new);
    public static final ItemModule[] SNEAK_SPEED = TieredItemModule.create(GTCEu.id("sneak_speed"),
            SneakSpeedItemModule::new);
    public static final ItemModule[] SWIM_SPEED = TieredItemModule.create(GTCEu.id("swim_speed"), SwimSpeedModule::new);
    public static final ItemModule[] STEP_HEIGHT = TieredItemModule.create(GTCEu.id("step_height"),
            StepHeightModule::new);
    public static final ItemModule[] JUMP_BOOST = TieredItemModule.create(GTCEu.id("jump_boost"),
            JumpBoostItemModule::new);
    public static final ItemModule[] SENSOR = TieredItemModule.create(GTCEu.id("sensor"), SensorItemModule::new);
    public static final ItemModule[] WIRELESS_CHARGER = TieredItemModule.create(GTCEu.id("wireless_charger"),
            AutoChargeItemModule::new);
    public static final ItemModule AUTO_EAT = new AutoEatModule(GTCEu.id("auto_eat"));
    public static final ItemModule AIR_SUPPLIER = new AirSupplierModule(GTCEu.id("air_supplier"));
    public static final ItemModule BATTERY = new BatteryItemModule(GTCEu.id("battery"));
    public static final ItemModule NIGHT_VISION = new NightVisionModule(GTCEu.id("night_vision"));
    public static final ItemModule PPE = new PPEModule(GTCEu.id("ppe"));
    public static final ItemModule LIQUID_FUEL_JETPACK = new LiquidFuelJetpackModule(GTCEu.id("liquid_fuel_jetpack"));
    public static final ItemModule JETPACK = new JetpackModule(GTCEu.id("jetpack"));
    public static final ItemModule ADVANCED_JETPACK = new AdvancedJetpackModule(GTCEu.id("advanced_jetpack"));
    public static final ItemModule CREATIVE_FLIGHT = new CreativeFlightModule(GTCEu.id("creative_flight"));
    public static final ItemModule FLUID_STORAGE = new FluidStorageModule(GTCEu.id("fluid_storage"));

    public static void init() {}
}

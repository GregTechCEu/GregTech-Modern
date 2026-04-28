package com.gregtechceu.gtceu.client.model.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.item.behavior.NanoSaberBehavior;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

import com.mojang.serialization.MapCodec;

public final class GTItemModelProperties {

    public static final Identifier BATTERY = GTCEu.id("battery");
    public static final Identifier ELECTRIC_JETPACK = GTCEu.id("electric_jetpack");
    public static final Identifier CIRCUIT = GTCEu.id("circuit");
    public static final Identifier LIGHTER_OPEN = GTCEu.id("lighter_open");
    public static final Identifier NANO_SABER_ACTIVE = NanoSaberBehavior.OVERRIDE_KEY_LOCATION;

    private GTItemModelProperties() {}

    public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(BATTERY, BatteryCharge.MAP_CODEC);
        event.register(ELECTRIC_JETPACK, ElectricJetpackCharge.MAP_CODEC);
        event.register(CIRCUIT, CircuitConfiguration.MAP_CODEC);
    }

    public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(LIGHTER_OPEN, LighterOpen.MAP_CODEC);
        event.register(NANO_SABER_ACTIVE, NanoSaberActive.MAP_CODEC);
    }

    public record BatteryCharge() implements RangeSelectItemModelProperty {

        public static final BatteryCharge INSTANCE = new BatteryCharge();
        public static final MapCodec<BatteryCharge> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
            return ElectricStats.getStoredPredicate(stack);
        }

        @Override
        public MapCodec<BatteryCharge> type() {
            return MAP_CODEC;
        }
    }

    public record ElectricJetpackCharge() implements RangeSelectItemModelProperty {

        public static final ElectricJetpackCharge INSTANCE = new ElectricJetpackCharge();
        public static final MapCodec<ElectricJetpackCharge> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
            return ElectricStats.getStoredPredicate(stack);
        }

        @Override
        public MapCodec<ElectricJetpackCharge> type() {
            return MAP_CODEC;
        }
    }

    public record CircuitConfiguration() implements RangeSelectItemModelProperty {

        public static final CircuitConfiguration INSTANCE = new CircuitConfiguration();
        public static final MapCodec<CircuitConfiguration> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
            return IntCircuitBehaviour.getCircuitConfiguration(stack) / 100f;
        }

        @Override
        public MapCodec<CircuitConfiguration> type() {
            return MAP_CODEC;
        }
    }

    public record LighterOpen() implements ConditionalItemModelProperty {

        public static final LighterOpen INSTANCE = new LighterOpen();
        public static final MapCodec<LighterOpen> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed,
                           ItemDisplayContext displayContext) {
            return stack.getOrDefault(GTDataComponents.LIGHTER_OPEN, false);
        }

        @Override
        public MapCodec<LighterOpen> type() {
            return MAP_CODEC;
        }
    }

    public record NanoSaberActive() implements ConditionalItemModelProperty {

        public static final NanoSaberActive INSTANCE = new NanoSaberActive();
        public static final MapCodec<NanoSaberActive> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed,
                           ItemDisplayContext displayContext) {
            return NanoSaberBehavior.isItemActive(stack);
        }

        @Override
        public MapCodec<NanoSaberActive> type() {
            return MAP_CODEC;
        }
    }
}

package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.graphnet.pipenet.NodeLossResult;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.MultLossOperator;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

@FunctionalInterface
public interface ContainmentFailure {

    ContainmentFailure FALLBACK = stack -> NodeLossResult.IDENTITY;

    EnumMap<FluidState, ContainmentFailure> STATE_FAILURES = new EnumMap<>(FluidState.class);

    Map<ResourceLocation, ContainmentFailure> ATTRIBUTE_FAILURES = new Object2ObjectOpenHashMap<>();

    static void registerFailure(FluidState state, ContainmentFailure failure) {
        STATE_FAILURES.put(state, failure);
    }

    static void registerFailure(FluidAttribute attribute, ContainmentFailure failure) {
        ATTRIBUTE_FAILURES.put(attribute.getResourceLocation(), failure);
    }

    static @NotNull ContainmentFailure getFailure(FluidState state) {
        return STATE_FAILURES.getOrDefault(state, FALLBACK);
    }

    static @NotNull ContainmentFailure getFailure(FluidAttribute attribute) {
        return ATTRIBUTE_FAILURES.getOrDefault(attribute.getResourceLocation(), FALLBACK);
    }

    static @NotNull ContainmentFailure getFailure(ResourceLocation attribute) {
        return ATTRIBUTE_FAILURES.getOrDefault(attribute, FALLBACK);
    }

    @NotNull
    NodeLossResult computeLossResult(FluidStack fluid);

    static void init() {
        registerFailure(FluidState.GAS, stack -> {
            if (GTValues.RNG.nextInt(8) == 0) {
                return new NodeLossResult(node -> {
                    IWorldPipeNetTile tile = node.getBlockEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.spawnParticles(Direction.UP, ParticleTypes.SMOKE, 7 + GTValues.RNG.nextInt(2));
                        tile.dealAreaDamage(2, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                FluidHelper.getTemperature(stack), 1, 10));
                    }
                }, MultLossOperator.TENTHS[9]);
            } else {
                return new NodeLossResult(node -> {
                    node.getNet().getLevel().removeBlock(node.getEquivalencyData(), false);
                    IWorldPipeNetTile tile = node.getBlockEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.visuallyExplode();
                        tile.spawnParticles(Direction.UP, ParticleTypes.LARGE_SMOKE, 9 + GTValues.RNG.nextInt(3));
                        tile.dealAreaDamage(2, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                FluidHelper.getTemperature(stack), 1.5f, 15));
                    }
                }, MultLossOperator.TENTHS[2]);
            }
        });
        registerFailure(FluidState.LIQUID, stack -> new NodeLossResult(node -> {
            IWorldPipeNetTile tile = node.getBlockEntityNoLoading();
            if (tile != null) {
                tile.playLossSound();
                for (Direction facing : Direction.Plane.HORIZONTAL) {
                    int particles = GTValues.RNG.nextInt(5);
                    if (particles != 0) {
                        tile.spawnParticles(facing, ParticleTypes.DRIPPING_WATER, particles);
                    }
                }
                tile.dealAreaDamage(1, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                        FluidHelper.getTemperature(stack), 2f, 20));
            }
        }, MultLossOperator.TENTHS[6]));
        registerFailure(FluidState.PLASMA, stack -> {
            if (GTValues.RNG.nextInt(4) == 0) {
                return new NodeLossResult(node -> {
                    IWorldPipeNetTile tile = node.getBlockEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.spawnParticles(Direction.UP, ParticleTypes.SMOKE, 1 + GTValues.RNG.nextInt(2));
                        tile.dealAreaDamage(3, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                FluidHelper.getTemperature(stack), 1, 25));
                    }
                }, MultLossOperator.TENTHS[8]);
            } else {
                return new NodeLossResult(node -> {
                    node.getNet().getLevel().removeBlock(node.getEquivalencyData(), false);
                    IWorldPipeNetTile tile = node.getBlockEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.visuallyExplode();
                        tile.spawnParticles(Direction.UP, ParticleTypes.LARGE_SMOKE, 3 + GTValues.RNG.nextInt(3));
                        tile.dealAreaDamage(3, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                FluidHelper.getTemperature(stack), 1.5f, 30));
                    }
                }, MultLossOperator.TENTHS[2]);
            }
        });
        registerFailure(FluidAttributes.ACID, stack -> {
            if (GTValues.RNG.nextInt(10) == 0) {
                return new NodeLossResult(node -> {
                    IWorldPipeNetTile tile = node.getBlockEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        boolean gaseous = stack.getFluid().getFluidType().isLighterThanAir();
                        tile.spawnParticles(gaseous ? Direction.UP : Direction.DOWN, ParticleTypes.CRIT,
                                3 + GTValues.RNG.nextInt(2));
                        tile.dealAreaDamage(gaseous ? 2 : 1,
                                entity -> EntityDamageUtil.applyChemicalDamage(entity, gaseous ? 2 : 3));
                    }
                }, MultLossOperator.TENTHS[9]);
            } else {
                return new NodeLossResult(node -> {
                    node.getNet().getLevel().removeBlock(node.getEquivalencyData(), false);
                    IWorldPipeNetTile tile = node.getBlockEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        boolean gaseous = stack.getFluid().getFluidType().isLighterThanAir();
                        for (Direction facing : Direction.Plane.HORIZONTAL) {
                            tile.spawnParticles(facing, ParticleTypes.CRIT, 3 + GTValues.RNG.nextInt(2));
                        }
                        tile.spawnParticles(gaseous ? Direction.UP : Direction.DOWN, ParticleTypes.CRIT,
                                6 + GTValues.RNG.nextInt(4));
                        tile.dealAreaDamage(gaseous ? 2 : 1,
                                entity -> EntityDamageUtil.applyChemicalDamage(entity, gaseous ? 3 : 4));
                    }
                }, MultLossOperator.EIGHTHS[6]);
            }
        });
    }
}

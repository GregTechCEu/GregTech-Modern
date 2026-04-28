package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class SteamLiquidBoilerMachine extends SteamBoilerMachine {

    public static final Object2BooleanMap<Fluid> FUEL_CACHE = new Object2BooleanOpenHashMap<>();

    @SaveField
    public final NotifiableFluidTank fuelTank;

    public SteamLiquidBoilerMachine(BlockEntityCreationInfo info, boolean isHighPressure) {
        super(info, isHighPressure);
        this.fuelTank = createFuelTank().setFilter(fluid -> FUEL_CACHE.computeIfAbsent(fluid.getFluid(), f -> {
            if (isRemote()) return true;
            return recipeLogic.getRecipeManager().recipeMap().byType(getRecipeType()).stream().anyMatch(recipe -> {
                var list = recipe.value().inputs.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
                if (!list.isEmpty()) {
                    return Arrays
                            .stream(SizedIngredientExtensions
                                    .getFluids(FluidRecipeCapability.CAP.of(list.getFirst().content)))
                            .anyMatch(stack -> stack.getFluid() == f);
                }
                return false;
            });
        }));
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected NotifiableFluidTank createFuelTank() {
        return new NotifiableFluidTank(this, 1, 16 * FluidType.BUCKET_VOLUME, IO.IN);
    }

    @Override
    protected long getBaseSteamOutput() {
        return isHighPressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput :
                ConfigHolder.INSTANCE.machines.smallBoilers.liquidBoilerBaseOutput;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return SteamLiquidBoilerMachineUI.create(this, entityPlayer);
    }

    @Override
    protected void randomDisplayTick(RandomSource random, float x, float y, float z) {
        super.randomDisplayTick(random, x, y, z);
        if (random.nextFloat() < 0.3F) {
            Objects.requireNonNull(getLevel()).addParticle(ParticleTypes.LAVA, x + random.nextFloat(), y,
                    z + random.nextFloat(), 0.0F, 0.0F,
                    0.0F);
        }
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (!isRemote()) {
            if (FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), fuelTank)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.onUseWithItem(context);
    }
}

package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote SteamLiquidBoilerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamLiquidBoilerMachine extends SteamBoilerMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SteamLiquidBoilerMachine.class, SteamBoilerMachine.MANAGED_FIELD_HOLDER);
    public static final Object2BooleanMap<Fluid> FUEL_CACHE = new Object2BooleanOpenHashMap<>();

    @Persisted
    public final NotifiableFluidTank fuelTank;

    public SteamLiquidBoilerMachine(IMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder, isHighPressure, args);
        this.fuelTank = createFuelTank(args).setFilter(fluid -> FUEL_CACHE.computeIfAbsent(fluid.getFluid(), f -> {
            if (isRemote())  return true;
            return recipeLogic.getRecipeManager().getAllRecipesFor(getRecipeType()).stream().anyMatch(recipe -> {
                var list = recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
                if (!list.isEmpty()) {
                    return Arrays.stream(FluidRecipeCapability.CAP.of(list.get(0).content).getStacks()).anyMatch(stack -> stack.getFluid() == f);
                }
                return false;
            });
        }));
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createFuelTank(Object... args) {
        return new NotifiableFluidTank(this, 1, 16 * FluidHelper.getBucket(), IO.IN);
    }

    @Override
    protected long getBaseSteamOutput() {
        return (isHighPressure ? 600 : 240);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return super.createUI(entityPlayer)
                .widget(new TankWidget(fuelTank.storages[0], 119, 26, 10, 54, true, true)
                        .setShowAmount(false)
                        .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
                        .setBackground(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)));
    }

    @Override
    protected void randomDisplayTick(RandomSource random, float x, float y, float z) {
        super.randomDisplayTick(random, x, y, z);
        if (random.nextFloat() < 0.3F) {
            getLevel().addParticle(ParticleTypes.LAVA, x + random.nextFloat(), y, z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
        }
    }
}

package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.FluidSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamLiquidBoilerMachine extends SteamBoilerMachine {

    public static final Object2BooleanMap<Fluid> FUEL_CACHE = new Object2BooleanOpenHashMap<>();

    @SaveField
    public final NotifiableFluidTank fuelTank;

    public SteamLiquidBoilerMachine(IMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder, isHighPressure, args);
        this.fuelTank = createFuelTank(args).setFilter(fluid -> FUEL_CACHE.computeIfAbsent(fluid.getFluid(), f -> {
            if (isRemote()) return true;
            return recipeLogic.getRecipeManager().getAllRecipesFor(getRecipeType()).stream().anyMatch(recipe -> {
                var list = recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
                if (!list.isEmpty()) {
                    return Arrays.stream(FluidRecipeCapability.CAP.of(list.get(0).content).getStacks())
                            .anyMatch(stack -> stack.getFluid() == f);
                }
                return false;
            });
        }));
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected NotifiableFluidTank createFuelTank(Object... args) {
        return new NotifiableFluidTank(this, 1, 16 * FluidType.BUCKET_VOLUME, IO.IN);
    }

    @Override
    protected long getBaseSteamOutput() {
        return isHighPressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput :
                ConfigHolder.INSTANCE.machines.smallBoilers.liquidBoilerBaseOutput;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        UITexture progressTexture = isHighPressure() ? GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_STEEL :
                GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_BRONZE;

        return super.buildUI(data, syncManager, settings)
                .child(new Row()
                        .coverChildren()
                        .right(12).top(12)
                        .childPadding(4)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .child(new ProgressWidget()
                                .size(18)
                                .texture(progressTexture, 18)
                                .progress(recipeLogic::getProgressPercent)
                                .direction(ProgressWidget.Direction.UP))
                        .child(new FluidSlot()
                                .syncHandler(new FluidSlotSyncHandler(fuelTank.getStorages()[0])
                                        .canFillSlot(true).canDrainSlot(true))
                                .size(14, 54)
                                .displayAmount(false)));
    }

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * return super.createUI(entityPlayer)
     * .widget(new TankWidget(fuelTank.getStorages()[0], 119, 26, 10, 54, true, true)
     * .setShowAmount(false)
     * .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
     * .setBackground(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)));
     * }
     */

    @Override
    protected void randomDisplayTick(RandomSource random, float x, float y, float z) {
        super.randomDisplayTick(random, x, y, z);
        if (random.nextFloat() < 0.3F) {
            getLevel().addParticle(ParticleTypes.LAVA, x + random.nextFloat(), y, z + random.nextFloat(), 0.0F, 0.0F,
                    0.0F);
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isRemote()) {
            if (super.onUse(state, world, pos, player, hand, hit) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
            if (FluidUtil.interactWithFluidHandler(player, hand, fuelTank)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}

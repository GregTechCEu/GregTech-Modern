package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMechanic;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote LargeBoilerMachine
 */
public class LargeBoilerMachine extends WorkableMultiblockMachine implements IExplosionMachine, IDisplayUIMachine, IMufflerMechanic {
    private static final long STEAM_PER_WATER = 160;

    @Getter
    public final int maxTemperature, heatSpeed;
    @Persisted @Getter
    private int currentTemperature, throttle;
    @Getter
    private boolean hasNoWater;
    @Nullable
    protected TickableSubscription temperatureSubs;
    public LargeBoilerMachine(IMachineBlockEntity holder, int maxTemperature, int heatSpeed, Object... args) {
        super(holder, args);
        this.maxTemperature = maxTemperature;
        this.heatSpeed = heatSpeed;
        this.throttle = 100;
    }

    //////////////////////////////////////
    //******     Recipe Logic     ******//
    //////////////////////////////////////

    protected void updateSteamSubscription() {
        if (currentTemperature > 0) {
            temperatureSubs = subscribeServerTick(temperatureSubs, this::updateCurrentTemperature);
        } else if (temperatureSubs != null) {
            temperatureSubs.unsubscribe();
            temperatureSubs = null;
        }
    }

    protected void updateCurrentTemperature() {
        if (recipeLogic.isWorking()) {
            if (getOffsetTimer() % 10 == 0) {
                if (currentTemperature < getMaxTemperature()) {
                    currentTemperature = Mth.clamp(currentTemperature + heatSpeed * 10, 0, getMaxTemperature());
                }
            }
        } else if (currentTemperature > 0) {
            currentTemperature -= getCoolDownRate();
        };

        if (currentTemperature >= 100 && getOffsetTimer() % 5 == 0) {
            // drain water
            var maxDrain = currentTemperature * throttle * 5 * FluidHelper.getBucket() / (STEAM_PER_WATER * 100000);
            var drainWater = List.of(FluidStack.create(Fluids.WATER, maxDrain));
            List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
            if (getCapabilitiesProxy().contains(IO.IN, FluidRecipeCapability.CAP)) {
                inputTanks.addAll(Objects.requireNonNull(getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP)));
            }
            if (getCapabilitiesProxy().contains(IO.BOTH, FluidRecipeCapability.CAP)) {
                inputTanks.addAll(Objects.requireNonNull(getCapabilitiesProxy().get(IO.BOTH, FluidRecipeCapability.CAP)));
            }
            for (IRecipeHandler<?> tank : inputTanks) {
                drainWater = (List<FluidStack>) tank.handleRecipe(IO.IN, null, drainWater, null);
                if (drainWater == null) break;
            }
            var drained = (drainWater == null || drainWater.isEmpty()) ? maxDrain : maxDrain - drainWater.get(0).getAmount();

            boolean hasDrainedWater = drained > 0;

            if (hasDrainedWater) {
                // fill steam
                var fillSteam = List.of(GTMaterials.Steam.getFluid((drained * STEAM_PER_WATER)));
                List<IRecipeHandler<?>> outputTanks = new ArrayList<>();
                if (getCapabilitiesProxy().contains(IO.OUT, FluidRecipeCapability.CAP)) {
                    outputTanks.addAll(Objects.requireNonNull(getCapabilitiesProxy().get(IO.OUT, FluidRecipeCapability.CAP)));
                }
                if (getCapabilitiesProxy().contains(IO.BOTH, FluidRecipeCapability.CAP)) {
                    outputTanks.addAll(Objects.requireNonNull(getCapabilitiesProxy().get(IO.BOTH, FluidRecipeCapability.CAP)));
                }
                for (IRecipeHandler<?> tank : outputTanks) {
                    fillSteam = (List<FluidStack>) tank.handleRecipe(IO.OUT, null, fillSteam, null);
                    if (fillSteam == null) break;
                }
            }

            // check explosion
            if (this.hasNoWater && hasDrainedWater) {
                doExplosion(2f);
                var center = getPos().below().relative(getFrontFacing().getOpposite());
                if (GTValues.RNG.nextInt(100) > 80) {
                    doExplosion(center, 2f);
                }
                for (Direction x : Direction.Plane.HORIZONTAL) {
                    for (Direction y : Direction.Plane.HORIZONTAL) {
                        if (GTValues.RNG.nextInt(100) > 80) {
                            doExplosion(center.relative(x).relative(y), 2f);
                        }
                    }
                }
            } else {
                this.hasNoWater = !hasDrainedWater;
            }
        } else this.hasNoWater = false;
        updateSteamSubscription();
    }

    protected int getCoolDownRate() {
        return 1;
    }

    @Override
    public void onWorking() {
        if (currentTemperature < getMaxTemperature()) {
            currentTemperature = Math.max(1, currentTemperature);
            updateSteamSubscription();
        }
    }

    @Nullable
    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (throttle < 100) {
            var copied = recipe.copy();
            copied.duration = recipe.duration * 100 / throttle;
            return copied;
        }
        return super.modifyRecipe(recipe);
    }

    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()){
            textList.add(Component.translatable("gtceu.multiblock.large_boiler.temperature", (int) (currentTemperature * 274.15), (int) (maxTemperature * 274.15)));
            textList.add(Component.translatable("gtceu.multiblock.large_boiler.steam_output", currentTemperature));

            var throttleText = Component.translatable("gtceu.multiblock.large_boiler.throttle",
                            ChatFormatting.AQUA.toString() + getThrottle() + "%")
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("gtceu.multiblock.large_boiler.throttle.tooltip"))));
            textList.add(throttleText);

            var buttonText = Component.translatable("gtceu.multiblock.large_boiler.throttle_modify");
            buttonText.append(" ");
            buttonText.append(ComponentPanelWidget.withButton(Component.literal("[-]"), "sub"));
            buttonText.append(" ");
            buttonText.append(ComponentPanelWidget.withButton(Component.literal("[+]"), "add"));
            textList.add(buttonText);
        }
    }

    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            int result = componentData.equals("add") ? 5 : -5;
            this.throttle = Mth.clamp(throttle + result, 25, 100);
        }
    }

    @Override
    public IGuiTexture getScreenTexture() {
        return GuiTextures.DISPLAY_STEAM.get(maxTemperature > 800);
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }
}

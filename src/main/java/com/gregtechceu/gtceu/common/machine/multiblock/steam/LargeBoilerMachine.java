package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluids;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeBoilerMachine extends WorkableMultiblockMachine implements IExplosionMachine, IDisplayUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LargeBoilerMachine.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    public static final int TICKS_PER_STEAM_GENERATION = 5;

    @Getter
    public final int maxTemperature, heatSpeed;
    @Persisted
    @Getter
    private int currentTemperature, throttle;
    @Nullable
    protected TickableSubscription temperatureSubs;
    private int steamGenerated;

    public LargeBoilerMachine(IMachineBlockEntity holder, int maxTemperature, int heatSpeed, Object... args) {
        super(holder, args);
        this.maxTemperature = maxTemperature;
        this.heatSpeed = heatSpeed;
        this.throttle = 100;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ****** Recipe Logic ******//
    //////////////////////////////////////

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSteamSubscription));
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSteamSubscription));
        }
    }

    @Override
    public void onUnload() {
        if (temperatureSubs != null) {
            temperatureSubs.unsubscribe();
            temperatureSubs = null;
        }
        super.onUnload();
    }

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
        }

        if (isFormed() && getOffsetTimer() % TICKS_PER_STEAM_GENERATION == 0) {
            var maxDrain = currentTemperature * throttle * TICKS_PER_STEAM_GENERATION /
                    (ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater * 100);
            if (currentTemperature < 100) {
                steamGenerated = 0;
            } else if (maxDrain > 0) { // if maxDrain is 0 because throttle is too low, skip trying to make steam
                // drain water
                var drainWater = List.of(FluidIngredient.of(Fluids.WATER, maxDrain));
                List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
                inputTanks.addAll(getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP));
                inputTanks.addAll(getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP));
                for (IRecipeHandler<?> tank : inputTanks) {
                    drainWater = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, drainWater, false);
                    if (drainWater == null || drainWater.isEmpty()) {
                        break;
                    }
                }
                var drained = (drainWater == null || drainWater.isEmpty()) ? maxDrain :
                        maxDrain - drainWater.get(0).getAmount();

                steamGenerated = drained * ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater;

                if (drained > 0) {
                    // fill steam
                    var fillSteam = List.of(FluidIngredient.of(GTMaterials.Steam.getFluid(steamGenerated)));
                    List<IRecipeHandler<?>> outputTanks = new ArrayList<>();
                    outputTanks.addAll(getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP));
                    outputTanks.addAll(getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP));
                    for (IRecipeHandler<?> tank : outputTanks) {
                        fillSteam = (List<FluidIngredient>) tank.handleRecipe(IO.OUT, null, fillSteam, false);
                        if (fillSteam == null) break;
                    }
                }

                // check explosion
                if (drained < maxDrain) {
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
                }
            }
        }
        updateSteamSubscription();
    }

    protected int getCoolDownRate() {
        return 1;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        if (currentTemperature < getMaxTemperature()) {
            currentTemperature = Math.max(1, currentTemperature);
            updateSteamSubscription();
        }
        return value;
    }

    /**
     * Recipe Modifier for <b>Large Boiler Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Duration is multiplied by {@code 100 / throttle} if throttle is less than 100
     * </p>
     * 
     * @param machine a {@link LargeBoilerMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Large Boiler and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof LargeBoilerMachine largeBoilerMachine)) {
            return RecipeModifier.nullWrongType(LargeBoilerMachine.class, machine);
        }
        if (largeBoilerMachine.throttle == 100) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .durationMultiplier(100.0 / largeBoilerMachine.throttle)
                .build();
    }

    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.large_boiler.temperature",
                    currentTemperature + 274, maxTemperature + 274));
            textList.add(Component.translatable("gtceu.multiblock.large_boiler.steam_output",
                    steamGenerated / TICKS_PER_STEAM_GENERATION));

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
}

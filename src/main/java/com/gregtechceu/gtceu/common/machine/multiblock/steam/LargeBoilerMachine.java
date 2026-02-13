package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.StringSyncValue;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluids;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeBoilerMachine extends WorkableMultiblockMachine implements IExplosionMachine, IMuiMachine {

    public static final int TICKS_PER_STEAM_GENERATION = 5;

    @Getter
    public final int maxTemperature, heatSpeed;
    @SaveField
    @Getter
    private int currentTemperature, throttle;
    @Nullable
    protected TickableSubscription temperatureSubs;
    private int steamGenerated;

    public LargeBoilerMachine(BlockEntityCreationInfo info, int maxTemperature, int heatSpeed) {
        super(info, LargeBoilerRecipeLogic::new);
        this.maxTemperature = maxTemperature;
        this.heatSpeed = heatSpeed;
        this.throttle = 100;
    }

    //////////////////////////////////////
    // ****** Recipe Logic ******//
    //////////////////////////////////////

    @Override
    public LargeBoilerMachine.LargeBoilerRecipeLogic getRecipeLogic() {
        return (LargeBoilerMachine.LargeBoilerRecipeLogic) super.getRecipeLogic();
    }

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
                    var center = getBlockPos().below().relative(getFrontFacing().getOpposite());
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
     * Does not modify recipe. Real recipe duration is determined by
     * {@link LargeBoilerRecipeLogic#modifyFuelBurnTime(int)}
     * </p>
     * 
     * @param machine a {@link LargeBoilerMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Large Boiler and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        return ModifierFunction.IDENTITY;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 176);

        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(0.95f)
                        .heightRel(.45f)
                        .margin(4, 0)
                        .left(3).top(5)
                        .child(Flow.row()
                                .child(getMainTextPanel(syncManager, 170, 84))))
                .child(Flow.column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 4, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .excludeAreaInXei()
                        .child(GTMuiWidgets.createPowerButton(this, syncManager)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
        return panel;
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager, int width, int height) {
        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>();
        listWidget
                .width(width - 6)
                .height(height - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .alignX(Alignment.CenterLeft)
                .left(3)
                .top(3);
        parentWidget.size(width, height)
                .background(GTGuiTextures.DISPLAY);

        // Machine generic sync handlers
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));

        // Large Boiler specific sync handlers
        // These will not be called anywhere else, so we can create them directly instead of using
        // getOrCreateSyncHandler

        IntSyncValue currentTemperature = new IntSyncValue(this::getCurrentTemperature);
        syncManager.syncValue("currentTemperature", currentTemperature);

        IntSyncValue maxTemperature = new IntSyncValue(this::getMaxTemperature);
        syncManager.syncValue("maxTemperature", maxTemperature);

        IntSyncValue steamGenerated = new IntSyncValue(() -> this.steamGenerated);
        syncManager.syncValue("steamGenerated", steamGenerated);

        IntSyncValue throttle = new IntSyncValue(() -> this.throttle, newValue -> this.throttle = newValue);
        syncManager.syncValue("throttle", throttle);

        listWidget
                .child(IKey.dynamic(() -> {
                    if (!isFormed.getBoolValue()) return Component.empty();
                    return Component.translatable("gtceu.multiblock.large_boiler.temperature",
                            currentTemperature.getIntValue() + 274, maxTemperature.getIntValue() + 274)
                            .withStyle(ChatFormatting.WHITE);
                })
                        .asWidget())
                .child(IKey.dynamic(() -> {
                    if (!isFormed.getBoolValue()) return Component.empty();
                    return Component.translatable("gtceu.multiblock.large_boiler.steam_output",
                            steamGenerated.getIntValue() / TICKS_PER_STEAM_GENERATION).withStyle(ChatFormatting.WHITE);
                })
                        .asWidget())
                .child(IKey.lang(Component.translatable("gtceu.multiblock.large_boiler.throttle_modify")
                        .withStyle(ChatFormatting.WHITE))
                        .asWidget())
                .child(createIntInputWithButtons(throttle))

                .setEnabledIf((widget) -> isFormed.getBoolValue());
        parentWidget.child(listWidget);
        return parentWidget;
    }

    public static ParentWidget<?> createIntInputWithButtons(IntSyncValue syncValue) {
        StringSyncValue formattedValue = new StringSyncValue(syncValue::getStringValue,
                syncValue::setStringValue);

        return Flow.row()
                .coverChildrenHeight()
                .marginBottom(2)
                .widthRel(1.0f)
                .child(new ButtonWidget<>()
                        .left(0).width(18)
                        .onMousePressed((x, y, button) -> {
                            int val = syncValue.getIntValue() - getIncrementValue(MouseData.create(button));
                            val = Mth.clamp(val, 25, 100);
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(false))))
                .child(new TextFieldWidget()
                        .left(18).right(18)
                        .setTextAlignment(Alignment.Center)
                        .setTextColor(Color.WHITE.darker(1))
                        .setNumbers(25, 100)
                        .onMouseScrolled((mouseX, mouseY, delta) -> {
                            int inc = (int) delta;
                            int val = Mth.clamp(syncValue.getIntValue() + inc, 25, 100);
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .value(formattedValue))
                .child(new ButtonWidget<>()
                        .right(0).width(18)
                        .onMousePressed((x, y, button) -> {
                            int val = syncValue.getIntValue() + getIncrementValue(MouseData.create(button));
                            val = Mth.clamp(val, 25, 100);
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(true))));
    }

    private static IKey createAdjustOverlay(boolean increment) {
        final StringBuilder builder = new StringBuilder();
        builder.append(increment ? '+' : '-');
        builder.append(getIncrementValue(MouseData.create(-1)));

        float scale = 1f;
        if (builder.length() == 3) {
            scale = 0.8f;
        } else if (builder.length() == 4) {
            scale = 0.6f;
        } else if (builder.length() > 4) {
            scale = 0.5f;
        }
        return IKey.str(builder.toString())
                .color(Color.WHITE.main)
                .scale(scale);
    }

    private static int getIncrementValue(MouseData data) {
        return data.shift() ? 5 : 1;
    }

    public static class LargeBoilerRecipeLogic extends RecipeLogic {

        @SaveField
        @SyncToClient
        @Getter
        int currentThrottle;

        public LargeBoilerRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
            currentThrottle = 100;
        }

        public void setCurrentThrottle(int currentThrottle) {
            this.currentThrottle = currentThrottle;
            syncDataHolder.markClientSyncFieldDirty("currentThrottle");
        }

        @Override
        public void setupRecipe(GTRecipe recipe) {
            super.setupRecipe(recipe);
            if (lastRecipe != null) {
                setCurrentThrottle(((LargeBoilerMachine) machine).getThrottle());
                duration = (int) Math.round(lastRecipe.duration / (currentThrottle / 100.0));
            }
        }

        public void modifyFuelBurnTime(int newThrottle) {
            if (lastRecipe != null) {
                double newThrottleMultiplier = (double) currentThrottle / newThrottle;
                duration = (int) Math.round(lastRecipe.duration / (newThrottle / 100.0));
                progress = (int) Math.round(newThrottleMultiplier * progress);
            }
            setCurrentThrottle(newThrottle);
        }
    }
}

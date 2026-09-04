package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The base class for steam boilers
 */
public abstract class SteamBoilerMachine extends SteamWorkableMachine
                                         implements IMuiMachine, IDataInfoProvider {

    @SaveField
    public final NotifiableFluidTank waterTank;
    @SaveField
    @SyncToClient
    @Getter
    private int currentTemperature;
    @SaveField
    @Getter
    private int timeBeforeCoolingDown;
    @Getter
    private boolean hasNoWater;
    @Nullable
    protected TickableSubscription temperatureSubs, autoOutputSubs;
    @Nullable
    protected ISubscription steamTankSubs;
    @SaveField
    @Getter
    private int heatingTimeDebt; // amount of time the fuel should be burned for to account for the boiler cooling down

    public SteamBoilerMachine(BlockEntityCreationInfo info, boolean isHighPressure) {
        super(info, isHighPressure, new SteamBoilerRecipeLogic(),
                new NotifiableFluidTank(1, 16 * FluidType.BUCKET_VOLUME, IO.OUT));
        this.waterTank = attachTrait(createWaterTank());
        this.waterTank.setFilter(fluid -> fluid.getFluid().is(GTMaterials.Water.getFluidTag()));
        recipeLogic.setRegressWhenWaiting(false);
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected NotifiableFluidTank createWaterTank() {
        return new NotifiableFluidTank(1, 16 * FluidType.BUCKET_VOLUME, IO.IN);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        scheduleForNextServerTick(this::updateAutoOutputSubscription);
        scheduleForNextServerTick(this::updateSteamSubscription);
        steamTankSubs = steamTank.addChangedListener(this::updateAutoOutputSubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (steamTankSubs != null) {
            steamTankSubs.unsubscribe();
            steamTankSubs = null;
        }
    }

    @Override
    public boolean hasOutputFacing() {
        return false;
    }

    //////////////////////////////////////
    // ******* Auto Output *******//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    protected void updateAutoOutputSubscription() {
        if (Direction.stream().filter(direction -> direction != getFrontFacing() && direction != Direction.DOWN)
                .anyMatch(direction -> GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getBlockPos(), direction))) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void autoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            steamTank.exportToNearby(Direction.stream()
                    .filter(direction -> direction != getFrontFacing() && direction != Direction.DOWN)
                    .filter(direction -> GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getBlockPos(), direction))
                    .toArray(Direction[]::new));
            updateAutoOutputSubscription();
        }
    }

    //////////////////////////////////////
    // ****** Recipe Logic ******//
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
        if (shouldDiscountFuelConsumptionAtMaxTemperature() &&
                currentTemperature >= getMaxTemperature() &&
                (recipeLogic.isWorking() || recipeLogic.isWaiting())) {
            // We are fully heated up and are running a recipe. We want to simulate the cooling logic here,
            // but instead of actually lowering the temperature, we instead manipulate the heating time debt
            // to simulate the fuel burn cycle without actually affecting the temperature.
            if (timeBeforeCoolingDown == 0) {
                // Heating interval times cool down rate is the same amount of fuel recipe progress it would take
                // to get the lowered temperature back up to the maximum temperature.
                heatingTimeDebt += getHeatingTimeDebtAtMaxTemperature();
                timeBeforeCoolingDown = getCooldownInterval();
            } else {
                --timeBeforeCoolingDown;
            }
        } else if (recipeLogic.isWorking()) {
            this.timeBeforeCoolingDown = getCooldownInterval();
            if (getOffsetTimer() % getHeatingInterval() == 0) {
                if (currentTemperature < getMaxTemperature()) {
                    currentTemperature++;
                }
            }
        } else if (timeBeforeCoolingDown == 0) {
            if (currentTemperature > 0) {
                currentTemperature -= getCoolDownRate();
                heatingTimeDebt = 0;
                timeBeforeCoolingDown = getCooldownInterval();
            }
        } else {
            --timeBeforeCoolingDown;
        }

        if (getOffsetTimer() % 10 == 0) {
            if (currentTemperature >= 100) {
                int fillAmount = (int) getTotalSteamOutput();
                boolean hasDrainedWater = !waterTank.drainInternal(1, FluidAction.EXECUTE).isEmpty();
                var filledSteam = 0L;
                if (hasDrainedWater) {
                    filledSteam = steamTank.fillInternal(
                            GTMaterials.Steam.getFluid(fillAmount),
                            FluidAction.EXECUTE);
                }
                if (this.hasNoWater && hasDrainedWater) {
                    GTUtil.doExplosion(getLevel(), getBlockPos(), 2.0f);
                } else {
                    this.hasNoWater = !hasDrainedWater;
                }
                if (filledSteam == 0 && hasDrainedWater && getLevel() instanceof ServerLevel serverLevel) {
                    final float x = getBlockPos().getX() + 0.5F;
                    final float y = getBlockPos().getY() + 0.5F;
                    final float z = getBlockPos().getZ() + 0.5F;

                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x + getFrontFacing().getStepX() * 0.6,
                            y + getFrontFacing().getStepY() * 0.6,
                            z + getFrontFacing().getStepZ() * 0.6,
                            7 + GTValues.RNG.nextInt(3),
                            getFrontFacing().getStepX() / 2.0,
                            getFrontFacing().getStepY() / 2.0,
                            getFrontFacing().getStepZ() / 2.0, 0.1);

                    if (ConfigHolder.INSTANCE.machines.machineSounds) {
                        getLevel().playSound(null, x, y, z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f,
                                1.0f);
                    }

                    // bypass capability check for special case behavior
                    steamTank.drainInternal(FluidType.BUCKET_VOLUME * 4, FluidAction.EXECUTE);
                }
            } else {
                this.hasNoWater = false;
            }
        }
        updateSteamSubscription();
        syncDataHolder.markClientSyncFieldDirty("currentTemperature");
    }

    protected int getCooldownInterval() {
        return isHighPressure ? 40 : 45;
    }

    @SuppressWarnings("MethodMayBeStatic")
    protected int getCoolDownRate() {
        return 1;
    }

    protected int getHeatingInterval() {
        return isHighPressure ? 12 : 24;
    }

    public int getMaxTemperature() {
        return isHighPressure ? 1000 : 500;
    }

    private double getTemperaturePercent() {
        return currentTemperature / (getMaxTemperature() * 1.0);
    }

    protected abstract long getBaseSteamOutput();

    /** Returns the current total steam output every 10 ticks. */
    public long getTotalSteamOutput() {
        if (currentTemperature < 100) return 0;
        return (long) (getBaseSteamOutput() * ((float) currentTemperature / getMaxTemperature()) / 2);
    }

    /**
     * Recipe Modifier for <b>Steam Boiler Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Duration is multiplied by {@code 0.5} if the machine is high pressure
     * 
     * @param machine a {@link SteamBoilerMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Steam Boiler
     */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof SteamBoilerMachine boilerMachine)) {
            return RecipeModifier.nullWrongType(SteamBoilerMachine.class, machine);
        }
        if (!boilerMachine.isHighPressure) return ModifierFunction.IDENTITY;

        return ModifierFunction.builder()
                .durationMultiplier(0.5)
                .build();
    }

    @Override
    public boolean onWorking() {
        if (heatingTimeDebt > 0) {
            --this.heatingTimeDebt;
        }
        boolean value = super.onWorking();
        if (currentTemperature < getMaxTemperature()) {
            currentTemperature = Math.max(1, currentTemperature);
            updateSteamSubscription();
        }
        return value;
    }

    /** Returns true if fuel consumption should be discounted at max temperature */
    protected boolean shouldDiscountFuelConsumptionAtMaxTemperature() {
        return true;
    }

    /** Returns true if we should suspend the currently running burning recipe due to the boiler being fully heated */
    protected boolean shouldSuspendRecipeDueToBeingFullyHeated() {
        return shouldDiscountFuelConsumptionAtMaxTemperature() &&
                currentTemperature >= getMaxTemperature() &&
                heatingTimeDebt <= 0;
    }

    protected int getHeatingTimeDebtAtMaxTemperature() {
        return Math.min(getHeatingInterval() * getCoolDownRate(), getCooldownInterval());
    }

    /** Returns the effective fuel consumption rate, normalized */
    public float getEffectiveFuelConsumptionRate() {
        if (shouldDiscountFuelConsumptionAtMaxTemperature() && currentTemperature >= getMaxTemperature()) {
            return getHeatingTimeDebtAtMaxTemperature() * 1.0f / getCooldownInterval();
        }
        return 1.0f;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    @Override
    protected InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            context.getPlayer().sendSystemMessage(Component.translatable("behaviour.soft_hammer.ignored"));
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (FluidUtil.getFluidHandler(context.getItemInHand()).isPresent()) {
            if (isRemote()) {
                return InteractionResult.SUCCESS;
            }
            if (FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), waterTank)) {
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return super.onUseWithItem(context);
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.defaultSteamMachinePanelBuilder(this);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        UITexture progressTexture = isHighPressure() ? GTGuiTextures.PROGRESS_BAR_BOILER_EMPTY_STEEL :
                GTGuiTextures.PROGRESS_BAR_BOILER_EMPTY_BRONZE;

        DoubleSyncValue tempPercentage = syncManager.getOrCreateSyncHandler("tempPercentage", DoubleSyncValue.class,
                () -> new DoubleSyncValue(this::getTemperaturePercent));

        mainWidget.child(Flow.row()
                .top(12)
                .left(50)
                .coverChildren()
                .childPadding(10)
                .child(new FluidSlot()
                        .syncHandler(new FluidSlotSyncHandler(waterTank.getStorages()[0]))
                        .size(14, 54)
                        .alwaysShowFull(true))
                .child(new FluidSlot()
                        .syncHandler(new FluidSlotSyncHandler(steamTank.getStorages()[0])
                                .canFillSlot(false).canDrainSlot(true))
                        .alwaysShowFull(true)
                        .size(14, 54))
                .child(new ProgressWidget()
                        .texture(progressTexture,
                                GTGuiTextures.PROGRESS_BAR_BOILER_HEAT, ProgressDrawable.Direction.UP)
                        .size(14, 54)
                        .value(tempPercentage)
                        .tooltipAutoUpdate(true)
                        .tooltipBuilder((r) -> r.addLine(Text
                                .lang("gtceu.fluid.temperature", getCurrentTemperature())))));
    }

    //////////////////////////////////////
    // ********* Client *********//
    //////////////////////////////////////

    @Override
    public void animateTick(RandomSource random) {
        if (isActive()) {
            final BlockPos pos = getBlockPos();
            float x = pos.getX() + 0.5F;
            float z = pos.getZ() + 0.5F;

            final var facing = getFrontFacing();
            final float horizontalOffset = random.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + random.nextFloat() * 0.375F;

            if (facing.getAxis() == Direction.Axis.X) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == Direction.Axis.Z) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            randomDisplayTick(random, x, y, z);
        }
    }

    protected void randomDisplayTick(RandomSource random, float x, float y, float z) {
        getLevel().addParticle(isHighPressure ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        getLevel().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            return Collections.singletonList(Component.translatable("gtceu.machine.steam_boiler.heat_amount",
                    FormattingUtil.formatNumbers((int) (getTemperaturePercent() * 100))));
        }
        return new ArrayList<>();
    }

    private static class SteamBoilerRecipeLogic extends RecipeLogic {

        @Override
        public ActionResult handleTickRecipe(GTRecipe recipe) {
            SteamBoilerMachine boilerMachine = (SteamBoilerMachine) getMachine();
            if (boilerMachine.shouldSuspendRecipeDueToBeingFullyHeated()) {
                return ActionResult.FAIL_NO_REASON;
            }
            return super.handleTickRecipe(recipe);
        }
    }
}

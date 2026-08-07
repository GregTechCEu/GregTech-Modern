package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.trait.miner.LargeMinerLogic;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.GTMaterials.DrillingFluid;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeMinerMachine extends WorkableElectricMultiblockMachine
                               implements IMiner, IControllable, IDataInfoProvider {

    public static final int CHUNK_LENGTH = 16;
    @Getter
    private final int tier;
    @Nullable
    protected EnergyContainerList energyContainer;
    @Nullable
    protected FluidHandlerList inputFluidInventory;
    private final int drillingFluidConsumePerTick;

    public LargeMinerMachine(BlockEntityCreationInfo info, int tier, int speed, int maximumChunkDiameter, int fortune,
                             int drillingFluidConsumePerTick) {
        super(info, new LargeMinerLogic(fortune, speed, maximumChunkDiameter * CHUNK_LENGTH / 2));
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
    }

    @Override
    public LargeMinerLogic getRecipeLogic() {
        return (LargeMinerLogic) super.getRecipeLogic();
    }

    public static Material getMaterial(int tier) {
        if (tier == GTValues.EV) return GTMaterials.Steel;
        if (tier == GTValues.IV) return GTMaterials.Titanium;
        if (tier == GTValues.LuV) return GTMaterials.TungstenSteel;
        return GTMaterials.Steel;
    }

    public static Block getCasingState(int tier) {
        return GTBlocks.MATERIALS_TO_CASINGS.get(getMaterial(tier)).get();
    }

    public long getMaxVoltage() {
        return GTValues.V[getEnergyTier()];
    }

    //////////////////////////////////////
    // ******* Logic *********//
    //////////////////////////////////////
    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        Direction opposite = this.getUpwardsFacing().getOpposite();
        getRecipeLogic().setDir(opposite == Direction.NORTH ? Direction.UP : Direction.DOWN);
        initializeAbilities();
    }

    @Override
    public PatternState checkStructurePattern(String name) {
        var patternState = super.checkStructurePattern(name);
        if (this.getUpwardsFacing() != Direction.UP && this.getUpwardsFacing() != Direction.DOWN) {
            patternState.setError(Predicates.PLACEHOLDER);
        }
        return patternState;
    }

    private void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IFluidHandler> fluidTanks = new ArrayList<>();
        // Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
        // Long2ObjectMaps::emptyMap);
        for (MultiblockPartMachine part : getParts()) {
            // IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            // if (io == IO.NONE) continue;

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                // if (!handlerList.isValid(io)) continue;
                handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .forEach(energyContainers::add);
                handlerList.getCapability(FluidRecipeCapability.CAP).stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .forEach(fluidTanks::add);
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
        this.inputFluidInventory = new FluidHandlerList(fluidTanks);

        getRecipeLogic().setVoltageTier(GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()));
        getRecipeLogic().setOverclockAmount(
                Math.max(1, GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier));
        getRecipeLogic().initPos(getBlockPos(), getRecipeLogic().getCurrentRadius());
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1,
                Math.max(this.tier, GTUtil.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public boolean drainInput(boolean simulate) {
        // drain energy
        if (energyContainer != null && energyContainer.getEnergyStored() > 0) {
            long energyToDrain = GTValues.VA[getEnergyTier()];
            long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                if (!simulate) {
                    energyContainer.changeEnergy(-energyToDrain);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        // drain fluid
        if (inputFluidInventory != null && inputFluidInventory.handlers.length > 0) {
            FluidStack drillingFluid = DrillingFluid
                    .getFluid(this.drillingFluidConsumePerTick * getRecipeLogic().getOverclockAmount());
            FluidStack fluidStack = inputFluidInventory.getFluidInTank(0);
            if (fluidStack != FluidStack.EMPTY && fluidStack.is(DrillingFluid.getFluid()) &&
                    fluidStack.getAmount() >= drillingFluid.getAmount()) {
                if (!simulate) {
                    GTTransferUtils.drainFluidAccountNotifiableList(inputFluidInventory, drillingFluid,
                            IFluidHandler.FluidAction.EXECUTE);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panelBuilder = getPanelBuilder(data, syncManager, settings);
        panelBuilder.mainContents(parent -> buildMainUI(parent, data, syncManager, settings));
        var machinePanel = panelBuilder.build(syncManager, settings);

        BooleanSyncValue silk = syncManager.getOrCreateSyncHandler("silkTouch", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> getRecipeLogic().isSilkTouchMode(),
                        (v) -> getRecipeLogic().setSilkTouchMode(v)).allowC2S());
        BooleanSyncValue chunk = syncManager.getOrCreateSyncHandler("chunk", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> getRecipeLogic().isChunkMode(),
                        (v) -> getRecipeLogic().setChunkMode(v)).allowC2S());

        machinePanel.getRightConfiguratorPanel()
                .child(new ToggleButton()
                        .value(silk)
                        .overlay(new ItemDrawable(Items.FEATHER))
                        .tooltipDynamic(r -> r.addLine(Component.translatable("gtceu.universal.tooltip.silk_touch")
                                .append(Component.translatable(
                                        "cover.voiding.label." + (silk.getBoolValue() ? "enabled" : "disabled")))))
                        .tooltipAutoUpdate(true))
                .child(new ToggleButton()
                        .value(chunk)
                        .overlay(false, GTGuiTextures.BUTTON_CHUNK_ALIGN[0])
                        .overlay(true, GTGuiTextures.BUTTON_CHUNK_ALIGN[1])
                        .tooltipDynamic(r -> r.addLine(Component.translatable("gtceu.universal.tooltip.chunk_mode")
                                .append(Component.translatable(
                                        "cover.voiding.label." + (chunk.getBoolValue() ? "enabled" : "disabled")))))
                        .tooltipAutoUpdate(true));

        return machinePanel;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        BooleanSyncValue done = syncManager.getOrCreateSyncHandler("done", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> getRecipeLogic().isDone()));
        IntSyncValue workingArea = syncManager.getOrCreateSyncHandler("workingArea", IntSyncValue.class,
                () -> new IntSyncValue(() -> getRecipeLogic().getCurrentRadius()));
        LongSyncValue x = syncManager.getOrCreateSyncHandler("x", LongSyncValue.class, () -> new LongSyncValue(() -> {
            if (getRecipeLogic().getX() == Integer.MAX_VALUE) return 0;
            return getRecipeLogic().getX();
        }));
        LongSyncValue y = syncManager.getOrCreateSyncHandler("y", LongSyncValue.class, () -> new LongSyncValue(() -> {
            if (getRecipeLogic().getY() == Integer.MAX_VALUE) return 0;
            return getRecipeLogic().getY();
        }));
        LongSyncValue z = syncManager.getOrCreateSyncHandler("z", LongSyncValue.class, () -> new LongSyncValue(() -> {
            if (getRecipeLogic().getZ() == Integer.MAX_VALUE) return 0;
            return getRecipeLogic().getZ();
        }));
        LongSyncValue startX = syncManager.getOrCreateSyncHandler("startx", LongSyncValue.class,
                () -> new LongSyncValue(() -> {
                    if (getRecipeLogic().getStartX() == Integer.MAX_VALUE) return 0;
                    return getRecipeLogic().getStartX();
                }));
        LongSyncValue startY = syncManager.getOrCreateSyncHandler("starty", LongSyncValue.class,
                () -> new LongSyncValue(() -> {
                    if (getRecipeLogic().getStartY() == Integer.MAX_VALUE) return 0;
                    return getRecipeLogic().getStartY();
                }));
        LongSyncValue startZ = syncManager.getOrCreateSyncHandler("startz", LongSyncValue.class,
                () -> new LongSyncValue(() -> {
                    if (getRecipeLogic().getStartZ() == Integer.MAX_VALUE) return 0;
                    return getRecipeLogic().getStartZ();
                }));
        BooleanSyncValue chunk = syncManager.getOrCreateSyncHandler("chunk", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> getRecipeLogic().isChunkMode(),
                        (v) -> getRecipeLogic().setChunkMode(v)).allowC2S());
        IntSyncValue mineProgress = syncManager.getOrCreateSyncHandler("mineProgress", IntSyncValue.class,
                () -> new IntSyncValue(() -> getRecipeLogic().getProgress()));
        IntSyncValue totalMine = syncManager.getOrCreateSyncHandler("totalMine", IntSyncValue.class,
                () -> new IntSyncValue(() -> getRecipeLogic().getMaxProgress()));

        widgets.add(Text
                .dynamic(() -> Component.translatable("gtceu.machine.miner.x", x.getLongValue(), startX.getLongValue()))
                .asWidget());
        widgets.add(Text
                .dynamic(() -> Component.translatable("gtceu.machine.miner.y", y.getLongValue(), startY.getLongValue()))
                .asWidget());
        widgets.add(Text
                .dynamic(() -> Component.translatable("gtceu.machine.miner.z", z.getLongValue(), startZ.getLongValue()))
                .asWidget());
        widgets.add(Text.dynamic(() -> {
            if (chunk.getBoolValue()) {
                return Component.translatable("gtceu.universal.tooltip.working_area_chunks",
                        workingArea.getIntValue() * 2 / CHUNK_LENGTH, workingArea.getIntValue() * 2 / CHUNK_LENGTH);
            }
            return Component.translatable("gtceu.universal.tooltip.working_area", workingArea.getIntValue(),
                    workingArea.getIntValue());
        }).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_miner.done")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)))
                .asWidget()
                .setEnabledIf((w) -> done.getBoolValue()));
        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.machine.miner.progress",
                mineProgress.getIntValue(), totalMine.getIntValue())).asWidget());

        return widgets;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    @Override
    public InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (isRemote() || !this.isFormed())
            return InteractionResult.SUCCESS;

        if (!this.isActive()) {
            int currentRadius = getRecipeLogic().getCurrentRadius();
            if (getRecipeLogic().isChunkMode()) {
                if (currentRadius - CHUNK_LENGTH <= 0) {
                    getRecipeLogic().setCurrentRadius(getRecipeLogic().getMaximumRadius());
                } else {
                    getRecipeLogic().setCurrentRadius(currentRadius - CHUNK_LENGTH);
                }
                int workingAreaChunks = getRecipeLogic().getCurrentRadius() * 2 / CHUNK_LENGTH;
                context.getPlayer()
                        .sendSystemMessage(Component.translatable("gtceu.universal.tooltip.working_area_chunks",
                                workingAreaChunks, workingAreaChunks));
            } else {
                if (currentRadius - CHUNK_LENGTH / 2 <= 0) {
                    getRecipeLogic().setCurrentRadius(getRecipeLogic().getMaximumRadius());
                } else {
                    getRecipeLogic().setCurrentRadius(currentRadius - CHUNK_LENGTH / 2);
                }
                int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
                context.getPlayer().sendSystemMessage(
                        Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
            }
            getRecipeLogic().resetArea(true);
        } else {
            context.getPlayer().sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            return Collections.singletonList(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        }
        return new ArrayList<>();
    }
}

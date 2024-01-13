package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.miner.LargeMinerLogic;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.common.data.GTMaterials.DrillingFluid;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeMinerMachine extends WorkableElectricMultiblockMachine implements IMiner, IControllable {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LargeMinerMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    public static final int CHUNK_LENGTH = 16;
    @Getter
    private final int tier;
    @Nullable
    protected EnergyContainerList energyContainer;
    @Nullable
    protected FluidTransferList inputFluidInventory;
    private final int drillingFluidConsumePerTick;

    public LargeMinerMachine(IMachineBlockEntity holder, int tier, int speed, int maximumChunkDiameter, int fortune, int drillingFluidConsumePerTick) {
        super(holder, fortune, speed, maximumChunkDiameter);
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        if ( args[args.length - 3] instanceof Integer fortune && args[args.length - 2] instanceof Integer speed && args[args.length - 1] instanceof Integer maxRadius) {
            return new LargeMinerLogic(this, fortune, speed, maxRadius * CHUNK_LENGTH / 2);
        }
        throw new IllegalArgumentException("MinerMachine need args [inventorySize, fortune, speed, maximumRadius] for initialization");
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
    //*******       Logic      *********//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        initializeAbilities();
    }

    private void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IFluidTransfer> fluidTanks = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                var handlerIO = io == IO.BOTH ? handler.getHandlerIO() : io;
                if (handlerIO == IO.IN && handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                } else if (handlerIO == IO.IN && handler.getCapability() == FluidRecipeCapability.CAP && handler instanceof IFluidTransfer fluidTransfer) {
                    fluidTanks.add(fluidTransfer);
                }
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
        this.inputFluidInventory = new FluidTransferList(fluidTanks);

        getRecipeLogic().setVoltageTier(GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()));
        getRecipeLogic().setOverclockAmount(Math.max(1, GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier));
        getRecipeLogic().initPos(getPos(), getRecipeLogic().getCurrentRadius());
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1, Math.max(this.tier, GTUtil.getFloorTierByVoltage(energyContainer.getInputVoltage())));
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
        if (inputFluidInventory != null && inputFluidInventory.transfers.length > 0) {
            FluidStack drillingFluid = DrillingFluid.getFluid((long) this.drillingFluidConsumePerTick * getRecipeLogic().getOverclockAmount());
            FluidStack fluidStack = inputFluidInventory.getFluidInTank(0);
            if (fluidStack != FluidStack.empty() && fluidStack.isFluidEqual(DrillingFluid.getFluid(1)) && fluidStack.getAmount() >= drillingFluid.getAmount()) {
                if (!simulate) {
                    GTTransferUtils.drainFluidAccountNotifiableList(inputFluidInventory, drillingFluid, false);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////
    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (this.isFormed()) {
            int workingAreaChunks = getRecipeLogic().getCurrentRadius() * 2 / CHUNK_LENGTH;
            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            textList.add(Component.translatable("gtceu.machine.miner.startx", getRecipeLogic().getX() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getX()));
            textList.add(Component.translatable("gtceu.machine.miner.starty", getRecipeLogic().getY() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getY()));
            textList.add(Component.translatable("gtceu.machine.miner.startz", getRecipeLogic().getZ() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getZ()));
            textList.add(Component.translatable("gtceu.universal.tooltip.silk_touch")
                    .append(ComponentPanelWidget.withButton(Component.literal("[")
                            .append(getRecipeLogic().isSilkTouchMode() ?
                                    Component.translatable("gtceu.creative.activity.on") :
                                    Component.translatable("gtceu.creative.activity.off"))
                            .append(Component.literal("]")), "silk_touch")));
            textList.add(Component.translatable("gtceu.universal.tooltip.chunk_mode")
                    .append(ComponentPanelWidget.withButton(Component.literal("[")
                            .append(getRecipeLogic().isChunkMode() ?
                                    Component.translatable("gtceu.creative.activity.on") :
                                    Component.translatable("gtceu.creative.activity.off"))
                            .append(Component.literal("]")), "chunk_mode")));
            if (getRecipeLogic().isChunkMode()) {
                textList.add(Component.translatable("gtceu.universal.tooltip.working_area_chunks", workingAreaChunks, workingAreaChunks));
            } else {
                textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
            }
            if (getRecipeLogic().isDone()) {
                textList.add(Component.translatable("gtceu.multiblock.large_miner.done").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            }
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("chunk_mode")) {
                getRecipeLogic().setChunkMode(!getRecipeLogic().isChunkMode());
            }
            if (componentData.equals("silk_touch")) {
                getRecipeLogic().setSilkTouchMode(!getRecipeLogic().isSilkTouchMode());
            }
        }
    }

    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////
    @Override
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, BlockHitResult hitResult) {
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
                playerIn.sendSystemMessage(Component.translatable("gtceu.universal.tooltip.working_area_chunks", workingAreaChunks, workingAreaChunks));
            } else {
                if (currentRadius - CHUNK_LENGTH / 2 <= 0) {
                    getRecipeLogic().setCurrentRadius(getRecipeLogic().getMaximumRadius());
                } else {
                    getRecipeLogic().setCurrentRadius(currentRadius - CHUNK_LENGTH / 2);
                }
                int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
                playerIn.sendSystemMessage(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
            }
            getRecipeLogic().resetArea();
        } else {
            playerIn.sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

}

package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.MinerRenderer;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.trait.miner.LargeMinerLogic;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.CycleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.common.data.GTMaterials.DrillingFluid;

public class LargeMinerMachine extends WorkableElectricMultiblockMachine implements IMiner, IControllable {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LargeMinerMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final int CHUNK_LENGTH = 16;
    public static final ImmutableMap<Integer, Material> MINER_MATERIALS = ImmutableMap.of(
            GTValues.EV, GTMaterials.Steel,
            GTValues.IV, GTMaterials.Titanium,
            GTValues.LuV, GTMaterials.TungstenSteel);

    @Getter @Setter
    private GTRecipeType recipeType;

    @Getter
    private final Material material;
    private final int tier;

    @Nullable
    protected EnergyContainerList energyContainer;
    @Nullable
    protected FluidTransferList inputFluidInventory;
    @Nullable
    protected ItemTransferList outputInventory;

    @Getter @Setter
    private boolean isInventoryFull = false;

    private final int drillingFluidConsumePerTick;

    public LargeMinerMachine(IMachineBlockEntity holder, int tier, int speed, int maximumChunkDiameter, int fortune, Material material, int drillingFluidConsumePerTick) {
        super(holder, material, fortune, speed, maximumChunkDiameter);
        this.recipeType = getDefinition().getRecipeType();
        this.material = material;
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        if (args.length > 3 && args[args.length - 4] instanceof Material pipeMat && args[args.length - 3] instanceof Integer fortune && args[args.length - 2] instanceof Integer speed && args[args.length - 1] instanceof Integer maxRadius) {
            return new LargeMinerLogic(this, fortune, speed, maxRadius * CHUNK_LENGTH / 2, getBaseTexture(pipeMat), GTRecipeTypes.MACERATOR_RECIPES);
        }
        throw new IllegalArgumentException("MinerMachine need args [inventorySize, fortune, speed, maximumRadius] for initialization");
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
    }

    @Override
    public LargeMinerLogic getRecipeLogic() {
        return (LargeMinerLogic) super.getRecipeLogic();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        initializeAbilities();
    }

    private void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IFluidTransfer> fluidTanks = new ArrayList<>();
        List<IItemTransfer> itemHandlers = new ArrayList<>();
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
                } else if (handlerIO == IO.OUT && handler.getCapability() == ItemRecipeCapability.CAP && handler instanceof IItemTransfer itemHandler) {
                    itemHandlers.add(itemHandler);
                }
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
        this.inputFluidInventory = new FluidTransferList(fluidTanks);
        this.outputInventory = new ItemTransferList(itemHandlers);

        getRecipeLogic().setVoltageTier(GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()));
        getRecipeLogic().setOverclockAmount(Math.max(1, GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier));
        getRecipeLogic().initPos(getPos(), getRecipeLogic().getCurrentRadius());
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1, Math.max(this.tier, GTUtil.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            long energyToDrain = GTValues.VA[getEnergyTier()];
            long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                if (!simulate)
                    energyContainer.changeEnergy(-energyToDrain);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean drainFluid(boolean simulate) {
        if (inputFluidInventory != null && inputFluidInventory.transfers.length > 0) {

            FluidStack drillingFluid = DrillingFluid.getFluid((long) this.drillingFluidConsumePerTick * getRecipeLogic().getOverclockAmount());
            FluidStack fluidStack = inputFluidInventory.getFluidInTank(0);
            if (fluidStack != FluidStack.empty() && fluidStack.isFluidEqual(DrillingFluid.getFluid(1)) && fluidStack.getAmount() >= drillingFluid.getAmount()) {
                if (!simulate)
                    GTTransferUtils.drainFluidAccountNotifiableList(inputFluidInventory, drillingFluid, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = (WidgetGroup) super.createUIWidget();
        ComponentPanelWidget widget2 = new ComponentPanelWidget(63, 36, this::addDisplayText2)
                .setMaxWidthLimit(68).clickHandler(this::handleDisplayClick);
        group.addWidget(widget2);
        group.addWidget(new CycleButtonWidget(151, 110, 18, 18, 4, this::getCurrentModeTexture, this::setCurrentMode));
        return group;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (this.isFormed()) {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                int energyContainer = getEnergyTier();
                long maxVoltage = GTValues.V[energyContainer];
                String voltageName = GTValues.VNF[energyContainer];
                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            int workingAreaChunks = getRecipeLogic().getCurrentRadius() * 2 / CHUNK_LENGTH;
            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            textList.add(Component.translatable("gtceu.machine.miner.startx", getRecipeLogic().getX() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getX()));
            textList.add(Component.translatable("gtceu.machine.miner.starty", getRecipeLogic().getY() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getY()));
            textList.add(Component.translatable("gtceu.machine.miner.startz", getRecipeLogic().getZ() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getZ()));
            if (getRecipeLogic().isChunkMode()) {
                textList.add(Component.translatable("gtceu.universal.tooltip.working_area_chunks", workingAreaChunks, workingAreaChunks));
            } else {
                textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
            }
            if (getRecipeLogic().isDone())
                textList.add(Component.translatable("gtceu.multiblock.large_miner.done").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            else if (getRecipeLogic().isWorking())
                textList.add(Component.translatable("gtceu.multiblock.large_miner.working").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
            else if (!this.isWorkingEnabled())
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        }
    }

    private void addDisplayText2(List<Component> textList) {
        if (this.isFormed()) {
            textList.add(Component.translatable("gtceu.machine.miner.minex", getRecipeLogic().getMineX()));
            textList.add(Component.translatable("gtceu.machine.miner.miney", getRecipeLogic().getMineY()));
            textList.add(Component.translatable("gtceu.machine.miner.minez", getRecipeLogic().getMineZ()));
        }
    }

    protected void addWarningText(List<Component> textList) {
        //super.addWarningText(textList);
        if (isFormed()) {
            if (this.isInventoryFull) {
                textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
            if (!drainFluid(true)) {
                textList.add(Component.translatable("gtceu.multiblock.large_miner.needsfluid").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
            if (!drainEnergy(true)) {
                textList.add(Component.translatable("gtceu.multiblock.large_miner.needspower").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        }
    }

    public IModelRenderer getBaseTexture(Material material) {
        if (material.equals(GTMaterials.Titanium))
            return new TextureOverrideRenderer(MinerRenderer.PIPE_MODEL, Map.of("all", GTCEu.id("block/casings/solid/machine_casing_stable_titanium")));
        else if (material.equals(GTMaterials.TungstenSteel))
            return new TextureOverrideRenderer(MinerRenderer.PIPE_MODEL, Map.of("all", GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel")));
        return new TextureOverrideRenderer(MinerRenderer.PIPE_MODEL, Map.of("all", GTCEu.id("block/casings/solid/machine_casing_solid_steel")));
    }

    public long getMaxVoltage() {
        return GTValues.V[getEnergyTier()];
    }

    // used for UI
    private IGuiTexture getCurrentModeTexture(int mode) {
        return switch (mode) {
            case 0 -> GuiTextures.BUTTON_MINER_MODES.getSubTexture(0, 0, 1, 0.25f);
            case 1 -> GuiTextures.BUTTON_MINER_MODES.getSubTexture(0, 0.25f, 1, 0.25f);
            case 2 -> GuiTextures.BUTTON_MINER_MODES.getSubTexture(0, 0.5f, 1, 0.25f);
            case 3 -> GuiTextures.BUTTON_MINER_MODES.getSubTexture(0, 0.75f, 1, 0.25f);
            default -> new GuiTextureGroup(GuiTextures.BUTTON_MINER_MODES.getSubTexture(0, 0, 1, 0.25f), new ItemStackTexture(Items.BARRIER)); // "broken" color
        };
    }

    // used for UI
    private void setCurrentMode(int mode) {
        switch (mode) {
            case 0 -> {
                getRecipeLogic().setChunkMode(false);
                getRecipeLogic().setSilkTouchMode(false);
            }
            case 1 -> {
                getRecipeLogic().setChunkMode(true);
                getRecipeLogic().setSilkTouchMode(false);
            }
            case 2 -> {
                getRecipeLogic().setChunkMode(false);
                getRecipeLogic().setSilkTouchMode(true);
            }
            case 3 -> {
                getRecipeLogic().setChunkMode(true);
                getRecipeLogic().setSilkTouchMode(true);
            }
        }
    }

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

    public int getTier() {
        return this.tier;
    }

    public int getMaxChunkRadius() {
        return getRecipeLogic().getMaximumRadius() / CHUNK_LENGTH;
    }

    public IItemTransfer getExportItems() {
        return this.outputInventory;
    }

    //    @Nonnull
//    @Override
//    public List<Component> getDataInfo() {
//        int workingArea = getWorkingArea(getRecipeLogic().getCurrentRadius());
//        return Collections.singletonList(Component.translatable("gtceu.machine.miner.working_area", workingArea, workingArea));
//    }
}

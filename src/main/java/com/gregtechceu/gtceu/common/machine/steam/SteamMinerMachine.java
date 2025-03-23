package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedImageWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.steam.SteamWorkableMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.trait.miner.SteamMinerLogic;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamMinerMachine extends SteamWorkableMachine implements IMiner, IControllable, IExhaustVentMachine,
                               IUIMachine, IMachineLife, IDataInfoProvider {

    @Getter
    @Setter
    @Persisted
    @DescSynced
    private boolean needsVenting;
    @Persisted
    public final NotifiableItemStackHandler importItems;
    @Persisted
    public final NotifiableItemStackHandler exportItems;
    private final int inventorySize;
    private final int energyPerTick;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportItemSubs;

    public SteamMinerMachine(IMachineBlockEntity holder, boolean isHighPressure, int speed, int maximumRadius,
                             int fortune, int energyPerTick) {
        super(holder, isHighPressure, fortune, speed, maximumRadius);
        this.inventorySize = 4;
        this.energyPerTick = energyPerTick;
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        if (args.length > 2 && args[args.length - 3] instanceof Integer fortune &&
                args[args.length - 2] instanceof Integer speed && args[args.length - 1] instanceof Integer maxRadius) {
            return new SteamMinerLogic(this, fortune, speed, maxRadius);
        }
        throw new IllegalArgumentException(
                "MinerMachine need args [fortune, speed, maximumRadius] for initialization");
    }

    @Override
    public SteamMinerLogic getRecipeLogic() {
        return (SteamMinerLogic) super.getRecipeLogic();
    }

    @Override
    protected NotifiableFluidTank createSteamTank(Object... args) {
        return new NotifiableFluidTank(this, 1, 16 * FluidType.BUCKET_VOLUME, IO.IN);
    }

    protected NotifiableItemStackHandler createImportItemHandler(@SuppressWarnings("unused") Object... args) {
        return new NotifiableItemStackHandler(this, 0, IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler(@SuppressWarnings("unused") Object... args) {
        return new NotifiableItemStackHandler(this, inventorySize, IO.OUT);
    }

    @Override
    public void onMachineRemoved() {
        getRecipeLogic().onRemove();
        clearInventory(exportItems.storage);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
        getRecipeLogic().updateTickSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
            }
            exportItemSubs = exportItems.addChangedListener(this::updateAutoOutputSubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }
    }

    //////////////////////////////////////
    // ********** LOGIC **********//
    //////////////////////////////////////
    protected void updateAutoOutputSubscription() {
        var outputFacingItems = getFrontFacing();
        if (!exportItems.isEmpty() && GTTransferUtils.hasAdjacentItemHandler(getLevel(), getPos(), outputFacingItems)) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void autoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            exportItems.exportToNearby(getFrontFacing());
        }
        updateAutoOutputSubscription();
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI builder = new ModularUI(175, 176, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isHighPressure()));
        builder.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                GuiTextures.SLOT_STEAM.get(isHighPressure()), 7,
                94, true));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(isHighPressure())));
            }
        }

        builder.widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()));
        builder.widget(new PredicatedImageWidget(79, 42, 18, 18, GuiTextures.INDICATOR_NO_STEAM.get(isHighPressure()))
                .setPredicate(() -> !drainInput(true)));
        builder.widget(new ImageWidget(7, 16, 105, 75, GuiTextures.DISPLAY_STEAM.get(isHighPressure())));
        builder.widget(new ComponentPanelWidget(10, 19, this::addDisplayText)
                .setMaxWidthLimit(84));
        builder.widget(new ComponentPanelWidget(70, 19, this::addDisplayText2)
                .setMaxWidthLimit(84));

        return builder;
    }

    void addDisplayText(List<Component> textList) {
        int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
        textList.add(Component.translatable("gtceu.machine.miner.startx", this.getRecipeLogic().getX()));
        textList.add(Component.translatable("gtceu.machine.miner.starty", this.getRecipeLogic().getY()));
        textList.add(Component.translatable("gtceu.machine.miner.startz", this.getRecipeLogic().getZ()));
        textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        if (this.getRecipeLogic().isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done")
                    .withStyle(ChatFormatting.GREEN));
        else if (this.getRecipeLogic().isWorking())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.working")
                    .withStyle(ChatFormatting.GOLD));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        if (getRecipeLogic().isInventoryFull())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull")
                    .withStyle(ChatFormatting.RED));
        if (isVentingBlocked())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.vent")
                    .withStyle(ChatFormatting.RED));
        else if (!drainInput(true))
            textList.add(Component.translatable("gtceu.multiblock.large_miner.steam")
                    .withStyle(ChatFormatting.RED));
    }

    void addDisplayText2(List<Component> textList) {
        textList.add(Component.translatable("gtceu.machine.miner.minex", this.getRecipeLogic().getMineX()));
        textList.add(Component.translatable("gtceu.machine.miner.miney", this.getRecipeLogic().getMineY()));
        textList.add(Component.translatable("gtceu.machine.miner.minez", this.getRecipeLogic().getMineZ()));
    }

    public boolean drainInput(boolean simulate) {
        long resultSteam = steamTank.getFluidInTank(0).getAmount() - energyPerTick;
        if (!this.isVentingBlocked() && resultSteam >= 0L && resultSteam <= steamTank.getTankCapacity(0)) {
            if (!simulate)
                steamTank.drainInternal(energyPerTick, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Direction getVentingDirection() {
        return Direction.UP;
    }

    @Override
    public void markVentingComplete() {
        this.needsVenting = false;
    }

    @Override
    public float getVentingDamage() {
        return isHighPressure() ? 12F : 6F;
    }

    @NotNull
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

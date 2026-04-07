package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemHandlerHelper;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeTankMachine extends QuantumTankMachine {

    @Getter
    @SaveField
    private int mBPerCycle = 1000;
    @Getter
    @SaveField
    private int ticksPerCycle = 1;

    public CreativeTankMachine(BlockEntityCreationInfo info) {
        super(info, GTValues.MAX, 1);
    }

    protected FluidCache createCacheFluidHandler() {
        return new InfiniteCache(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) autoOutput.setTicksPerCycle(ticksPerCycle);
    }

    @Override
    public long getStoredAmount() {
        return (long) Math.ceil(1d * mBPerCycle / ticksPerCycle);
    }

    private InteractionResult updateStored(FluidStack fluid) {
        stored = new FluidStack(fluid, 1000);
        onFluidChanged();
        return InteractionResult.SUCCESS;
    }

    private void setTicksPerCycle(int value) {
        ticksPerCycle = value;
        autoOutput.setTicksPerCycle(value);
        onFluidChanged();
    }

    private void setmBPerCycle(int value) {
        mBPerCycle = value;
        onFluidChanged();
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.putInt("mBPerCycle", mBPerCycle);
        tag.putInt("ticksPerCycle", ticksPerCycle);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        mBPerCycle = tag.getInt("mBPerCycle");
        ticksPerCycle = tag.getInt("ticksPerCycle");
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        var heldItem = context.getItemInHand();
        var player = context.getPlayer();
        if (context.getClickedFace() == getFrontFacing() && !isRemote()) {
            // If no fluid set and held-item has fluid, set fluid
            if (stored.isEmpty()) {
                return FluidUtil.getFluidContained(heldItem)
                        .map(this::updateStored)
                        .orElse(InteractionResult.PASS);
            }

            // Need to make a fake source to fully fill held-item since our cache only allows mbPerTick extraction
            CustomFluidTank source = new CustomFluidTank(new FluidStack(stored, Integer.MAX_VALUE));
            ItemStack result = FluidUtil.tryFillContainer(heldItem, source, Integer.MAX_VALUE, player, true)
                    .getResult();
            if (!result.isEmpty() && heldItem.getCount() > 1) {
                ItemHandlerHelper.giveItemToPlayer(player, result);
                result = heldItem.copy();
                result.shrink(1);
            }

            if (!result.isEmpty()) {
                player.setItemInHand(context.getHand(), result);
                return InteractionResult.SUCCESS;
            } else {
                return FluidUtil.getFluidContained(heldItem)
                        .map(this::updateStored)
                        .orElse(InteractionResult.PASS);
            }
        }
        return super.onUseWithItem(context);
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        if (context.getClickedFace() == getFrontFacing() && !isRemote()) {
            // Clear fluid if empty + shift-rclick
            if (context.getPlayer().isCrouching() && !stored.isEmpty()) {
                return updateStored(FluidStack.EMPTY);
            }
            return InteractionResult.PASS;
        }
        return super.onUse(context);
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.defaultPanelBuilder(this).addDefaultConfigurators(false)
                .addTraitConfigurators(false).rightConfigurators(f -> f.child(GTMuiWidgets.createPowerButton(this)));
    }

    // TODO
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        syncManager.syncValue("fluid",
                new FluidSlotSyncHandler(new FluidCacheTankWrapper(cache)).controlsAmount(false).phantom(true));

        IntSyncValue mbPerCycle = new IntSyncValue(this::getMBPerCycle, this::setmBPerCycle);
        syncManager.syncValue("mbPerCycle", mbPerCycle);
        IntSyncValue ticksPerCycle = new IntSyncValue(this::getTicksPerCycle, this::setTicksPerCycle);
        syncManager.syncValue("ticksPerCycle", ticksPerCycle);

        mainWidget
                .child(Flow.col()
                        .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 86)
                        .name("main")
                        .padding(7)
                        .mainAxisAlignment(Alignment.MainAxis.START)
                        .child(Flow.row().coverChildrenHeight()
                                .child(IKey.lang("gtceu.creative.tank.fluid").asWidget()
                                        .marginRight(4)
                                        .verticalCenter())
                                .child(new FluidSlot().syncHandler("locked_fluid_slot", 0)
                                        .background(GTGuiTextures.FLUID_SLOT)))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .height(18)
                                .child(IKey.lang("gtceu.creative.tank.mbpc").asWidget()
                                        .marginRight(4)
                                        .width(80)
                                        .verticalCenter())
                                .child(new TextFieldWidget()
                                        .setTextAlignment(Alignment.CENTER)
                                        .setNumbers(1, Integer.MAX_VALUE)
                                        .value(mbPerCycle)))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .height(18)
                                .child(IKey.lang("gtceu.creative.tank.tpc").asWidget()
                                        .marginRight(4)
                                        .width(80)
                                        .verticalCenter())
                                .child(new TextFieldWidget()
                                        .setTextAlignment(Alignment.CENTER)
                                        .setNumbers(1, Integer.MAX_VALUE)
                                        .value(ticksPerCycle))));
    }

    private class InfiniteCache extends FluidCache {

        public InfiniteCache(MetaMachine holder) {
            super(holder);
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return stored;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!stored.isEmpty() && stored.isFluidEqual(resource)) return resource.getAmount();
            return 0;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (!stored.isEmpty()) return new FluidStack(stored, mBPerCycle);
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!stored.isEmpty() && stored.isFluidEqual(resource)) return new FluidStack(resource, mBPerCycle);
            return FluidStack.EMPTY;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 1000;
        }
    }
}

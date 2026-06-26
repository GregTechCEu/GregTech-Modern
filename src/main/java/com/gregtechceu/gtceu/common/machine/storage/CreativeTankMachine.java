package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.item.datacomponents.CreativeMachineInfo;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

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
        return new InfiniteCache();
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
        stored = fluid.copyWithAmount(FluidType.BUCKET_VOLUME);
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
            CustomFluidTank source = new CustomFluidTank(stored.copyWithAmount(Integer.MAX_VALUE));
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
        return MachineUIPanelBuilder.panelBuilder(this).addDefaultConfigurators(false)
                .addTraitConfigurators(false).rightConfigurators(f -> f.child(GTMuiWidgets.createPowerButton(this)));
    }

    // TODO
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue mbPerCycle = new IntSyncValue(this::getMBPerCycle, this::setmBPerCycle).allowC2S();
        syncManager.syncValue("mbPerCycle", mbPerCycle);
        IntSyncValue ticksPerCycle = new IntSyncValue(this::getTicksPerCycle, this::setTicksPerCycle).allowC2S();
        syncManager.syncValue("ticksPerCycle", ticksPerCycle);

        mainWidget
                .child(Flow.col()
                        .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 86)
                        .name("main")
                        .padding(7)
                        .mainAxisAlignment(Alignment.MainAxis.START)
                        .child(Flow.row().coverChildrenHeight()
                                .child(Text.lang("gtceu.creative.tank.fluid").asWidget()
                                        .marginRight(4)
                                        .verticalCenter())
                                .child(createPhantomLockedFluidSlot(syncManager)))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .height(18)
                                .child(Text.lang("gtceu.creative.tank.mbpc").asWidget()
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
                                .child(Text.lang("gtceu.creative.tank.tpc").asWidget()
                                        .marginRight(4)
                                        .width(80)
                                        .verticalCenter())
                                .child(new TextFieldWidget()
                                        .setTextAlignment(Alignment.CENTER)
                                        .setNumbers(1, Integer.MAX_VALUE)
                                        .value(ticksPerCycle))));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        CreativeMachineInfo info = componentInput.get(GTDataComponents.CREATIVE_MACHINE_INFO);
        if (info != null) {
            mBPerCycle = info.outputPerCycle();
            ticksPerCycle = info.ticksPerCycle();
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(GTDataComponents.CREATIVE_MACHINE_INFO, new CreativeMachineInfo(mBPerCycle, ticksPerCycle));
    }

    private class InfiniteCache extends FluidCache {

        public InfiniteCache() {
            super();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return stored;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!stored.isEmpty() && FluidStack.isSameFluidSameComponents(stored, resource))
                return resource.getAmount();
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!stored.isEmpty()) return stored.copyWithAmount(mBPerCycle);
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!stored.isEmpty() && FluidStack.isSameFluidSameComponents(stored, resource))
                return resource.copyWithAmount(mBPerCycle);
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

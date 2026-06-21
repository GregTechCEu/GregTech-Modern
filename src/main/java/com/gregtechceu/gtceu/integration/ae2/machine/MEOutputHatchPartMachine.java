package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.AEKeyStorageSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEStackDisplayWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.ScrollPreservingGrid;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widgets.DynamicSyncedWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEOutputHatchPartMachine extends MEHatchPartMachine {

    @SaveField
    private KeyStorage internalBuffer; // Do not use KeyCounter, use our simple implementation

    public MEOutputHatchPartMachine(BlockEntityCreationInfo info) {
        super(info, IO.OUT);
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        this.internalBuffer = new KeyStorage();
        return new InaccessibleInfiniteTank(this);
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        var grid = getMainNode().getGrid();
        if (grid != null && !internalBuffer.isEmpty()) {
            for (var entry : internalBuffer) {
                grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(),
                        Actionable.MODULATE, actionSource);
            }
        }
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    protected boolean shouldSubscribe() {
        return super.shouldSubscribe() && !internalBuffer.storage.isEmpty();
    }

    @Override
    protected void autoIO() {
        if (!this.shouldSyncME()) return;
        if (this.updateMEStatus()) {
            var grid = getMainNode().getGrid();
            if (grid != null && !internalBuffer.isEmpty()) {
                internalBuffer.insertInventory(grid.getStorageService().getInventory(), actionSource);
            }
            this.updateTankSubscription();
        }
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        var flow = Flow.col().coverChildren();

        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        var storageSyncHandler = new AEKeyStorageSyncHandler(internalBuffer);
        syncManager.syncValue("ae_output_display", storageSyncHandler);

        int[] savedScroll = { 0 };
        var dynamicHandler = new DynamicLinkedSyncHandler<>(storageSyncHandler)
                .widgetProvider((sm, value) -> {
                    var col = Flow.col().leftRel(0.5f).coverChildrenHeight();
                    var list = value.getValue();
                    if (list.isEmpty()) return col.child(new TextWidget<>(Text.lang("gtceu.gui.waiting_list_empty")));
                    col.child(new TextWidget<>(Text.lang("gtceu.gui.waiting_list")).margin(0, 2));
                    col.child(new ScrollPreservingGrid(savedScroll)
                            .size(167, 80)
                            .scrollable(new VerticalScrollData())
                            .gridOfSizeWidth(9, 1, (x, y, index) -> new AEStackDisplayWidget(list, index)));
                    return col;
                });

        flow.child(new DynamicSyncedWidget<>()
                .syncHandler(dynamicHandler)
                .size(167, 80));

        mainWidget.child(flow);
    }

    private class InaccessibleInfiniteTank extends NotifiableFluidTank {

        FluidStorageDelegate storage;

        public InaccessibleInfiniteTank(MetaMachine holder) {
            super(List.of(new FluidStorageDelegate()), IO.OUT, IO.NONE);
            internalBuffer.setOnContentsChanged(this::onContentsChanged);
            storage = (FluidStorageDelegate) getStorages()[0];
            allowSameFluids = true;
        }

        @Override
        public int getTanks() {
            return 128;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public void setFluidInTank(int tank, FluidStack fluidStack) {}

        @Override
        public int getTankCapacity(int tank) {
            return storage.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public @NotNull List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                                boolean simulate) {
            if (io != IO.OUT) return left;
            FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
            for (var it = left.iterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }

                FluidStack[] fluids;
                if (ingredient instanceof IntProviderFluidIngredient provider) {
                    provider.setFluidStacks(null);
                    provider.setSampledCount(-1);

                    if (simulate) {
                        fluids = new FluidStack[] { provider.getMaxSizeStack() };
                    } else {
                        fluids = provider.getStacks();
                    }
                } else {
                    fluids = ingredient.getStacks();
                }
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }

                FluidStack output = fluids[0];
                int filled = storage.fill(output, action);
                ingredient.shrink(filled);
                if (filled <= 0) it.remove();
            }
            return left;
        }
    }

    private class FluidStorageDelegate extends CustomFluidTank {

        public FluidStorageDelegate() {
            super(0);
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setFluid(FluidStack fluid) {
            // NO-OP
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            var key = AEFluidKey.of(resource.getFluid(), resource.getTag());
            int amount = resource.getAmount();
            int oldValue = GTMath.saturatedCast(internalBuffer.storage.getOrDefault(key, 0));
            int changeValue = Math.min(Integer.MAX_VALUE - oldValue, amount);
            if (changeValue > 0 && action.execute()) {
                internalBuffer.storage.put(key, oldValue + changeValue);
                internalBuffer.onChanged();
            }
            return changeValue;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }
    }
}

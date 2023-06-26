package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IPassthroughHatch;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class FluidPassthroughHatchPartMachine extends TieredIOPartMachine implements IPassthroughHatch, IUIMachine {

    public static final int TANK_SIZE = 16_000;

    private final NotifiableFluidTank inventory;

    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;


    public FluidPassthroughHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
        this.inventory = createInventory();
    }

    protected NotifiableFluidTank createInventory(Object... args) {
        FluidStorage[] fluidHandlers = new FluidStorage[getTier() + 1];
        for (int i = 0; i < fluidHandlers.length; i++) {
            fluidHandlers[i] = new FluidStorage(TANK_SIZE);
        }
        return new NotifiableFluidTank(this, Arrays.asList(fluidHandlers), IO.BOTH);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateInventorySubscription));
        }
        inventorySubs = inventory.addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    protected void updateInventorySubscription() {
        if (isWorkingEnabled() && ((io == IO.OUT && !inventory.isEmpty()) || io == IO.IN)
                && ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(getFrontFacing()), getFrontFacing().getOpposite()) != null) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    inventory.exportToNearby(getFrontFacing());
                } else if (io == IO.IN){
                    inventory.importFromNearby(getFrontFacing());
                }
            }
            updateInventorySubscription();
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(getTier() + 1);
        return createUITemplate(entityPlayer, rowSize);
    }

    private ModularUI createUITemplate(Player player, int rowSize) {
        ModularUI builder = new ModularUI(176, 18 + 18 * rowSize + 94, this, player)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new ToggleButtonWidget(2, 18 + 18 * rowSize + 12 - 20, 18, 18,
                        GuiTextures.BUTTON_ITEM_OUTPUT, this::isWorkingEnabled, this::setWorkingEnabled)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.item_auto_input.tooltip"))
                .widget(UITemplate.bindPlayerInventory(player.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12, true));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new TankWidget(inventory.storages[index],
                        (88 - rowSize * 9 + x * 18), 18 + y * 18, true, true)
                        .setBackground(GuiTextures.SLOT));
            }
        }
        return builder;
    }

    @Nonnull
    @Override
    public Class<IFluidStorage> getPassthroughType() {
        return IFluidStorage.class;
    }
}

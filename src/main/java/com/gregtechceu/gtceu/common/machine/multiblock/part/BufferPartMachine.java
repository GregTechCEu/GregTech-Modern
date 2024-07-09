package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyRecipeTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BufferPartMachine extends TieredIOPartMachine
                               implements IDistinctPart, IMachineModifyDrops {

    public static final long INITIAL_TANK_CAPACITY = 4 * FluidHelper.getBucket();
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BufferPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableFluidTank tank;

    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;

    @Getter
    protected final ItemHandlerProxyRecipeTrait combinedInventory;

    @Getter
    @Persisted
    private final NotifiableItemStackHandler inventory;

    @Nullable
    protected TickableSubscription autoIOSubs;

    @Nullable
    protected ISubscription tankSubs;

    @Nullable
    protected ISubscription inventorySubs;

    private boolean hasFluidTransfer;
    private boolean hasItemTransfer;

    public BufferPartMachine(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier, io);
        this.inventory = createInventory(args);
        this.circuitInventory = createCircuitItemHandler(io);
        this.combinedInventory = createCombinedItemHandler(io);
        this.tank = createTank(INITIAL_TANK_CAPACITY, (int) Math.sqrt(getInventorySize()), args);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    public static long getTankCapacity(long initialCapacity, int tier) {
        return initialCapacity * (1L << Math.min(9, tier));
    }

    protected int getInventorySize() {
        int sizeRoot = 1 + Math.min(9, getTier());
        return sizeRoot * sizeRoot;
    }

    protected NotifiableItemStackHandler createInventory(Object... args) {
        return new NotifiableItemStackHandler(this, getInventorySize(), io);
    }

    protected NotifiableItemStackHandler createCircuitItemHandler(Object... args) {
        if (args.length > 0 && args[0] instanceof IO io && io == IO.IN) {
            return new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                    .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        } else {
            return new NotifiableItemStackHandler(this, 0, IO.NONE);
        }
    }

    protected ItemHandlerProxyRecipeTrait createCombinedItemHandler(Object... args) {
        if (args.length > 0 && args[0] instanceof IO io && io == IO.IN) {
            return new ItemHandlerProxyRecipeTrait(
                    this, Set.of(getInventory(), circuitInventory), IO.IN, IO.NONE);
        } else {
            return new ItemHandlerProxyRecipeTrait(
                    this, Set.of(getInventory(), circuitInventory), IO.NONE, IO.NONE);
        }
    }

    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        return new NotifiableFluidTank(this, slots, getTankCapacity(initialCapacity, getTier()), io);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateBufferSubscription));
        }
        inventorySubs = getInventory().addChangedListener(this::updateBufferSubscription);
        tankSubs = tank.addChangedListener(this::updateBufferSubscription);

        combinedInventory.recomputeEnabledState();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }

        if (tankSubs != null) {
            tankSubs.unsubscribe();
            tankSubs = null;
        }
    }

    //////////////////////////////////////
    // ******** Auto IO *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateBufferSubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateBufferSubscription();
    }

    protected void updateBufferSubscription() {
        boolean canOutput = io == IO.OUT && (!tank.isEmpty() || !inventory.isEmpty());
        this.hasItemTransfer = ItemTransferHelper.getItemTransfer(
                getLevel(), getPos().relative(getFrontFacing()), getFrontFacing().getOpposite()) != null;
        this.hasFluidTransfer = FluidTransferHelper.getFluidTransfer(
                getLevel(), getPos().relative(getFrontFacing()), getFrontFacing().getOpposite()) != null;
        if (isWorkingEnabled() && (canOutput || io == IO.IN) && (hasItemTransfer || hasFluidTransfer)) {
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
                    if (hasItemTransfer) {
                        getInventory().exportToNearby(getFrontFacing());
                    }
                    if (hasFluidTransfer) {
                        tank.exportToNearby(getFrontFacing());
                    }
                } else if (io == IO.IN) {
                    if (hasItemTransfer) {
                        getInventory().importFromNearby(getFrontFacing());
                    }
                    if (hasFluidTransfer) {
                        tank.importFromNearby(getFrontFacing());
                    }
                }
            }
            updateBufferSubscription();
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateBufferSubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IDistinctPart.super.attachConfigurators(configuratorPanel);
        if (this.io == IO.IN) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        }
    }

    @Override
    public Widget createUIWidget() {
        int slots = getInventorySize();
        int tanks = (int) Math.sqrt(slots);
        var group = new WidgetGroup(0, 0, 18 * (tanks + 1) + 16, 18 * tanks + 16);
        var container = new WidgetGroup(4, 4, 18 * (tanks + 1) + 8, 18 * tanks + 8);

        int index = 0;
        for (int y = 0; y < tanks; y++) {
            for (int x = 0; x < tanks; x++) {
                container.addWidget(new SlotWidget(
                        getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, io.support(IO.IN))
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT));
            }
        }

        index = 0;
        for (int y = 0; y < tanks; y++) {
            container.addWidget(new TankWidget(
                    tank.getStorages()[index++], 4 + tanks * 18, 4 + y * 18, true, io.support(IO.IN))
                    .setBackground(GuiTextures.FLUID_SLOT));
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }

    @Override
    public boolean isDistinct() {
        return getInventory().isDistinct() && circuitInventory.isDistinct();
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        getInventory().setDistinct(isDistinct);
        circuitInventory.setDistinct(isDistinct);
        combinedInventory.setDistinct(isDistinct);
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, getInventory().storage);

        if (!ConfigHolder.INSTANCE.machines.ghostCircuit) {
            clearInventory(drops, circuitInventory.storage);
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

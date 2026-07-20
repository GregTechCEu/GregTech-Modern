package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.widget.LargeStackSlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyInvConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IAllowSameUIProvider;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.CatalystItemHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.LargeStackItemHandler;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemBusPartMachine extends TieredIOPartMachine
                                implements IDistinctPart, IMachineLife, IHasCircuitSlot, IAllowSameUIProvider {

    public static final int[] INVENTORY_SIZE = { 1, 4, 6, 8, 10, 18, 21, 24, 36, 64 };
    public static final int[] LINE_NUM = { 1, 2, 2, 2, 2, 3, 3, 4, 6, 8 };

    @Getter
    @Persisted
    private final NotifiableItemStackHandler inventory;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;
    @Getter
    private boolean circuitSlotEnabled = true;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler shareInventory;
    @Getter
    @Persisted
    @DescSynced
    private boolean isDistinct = false;

    public ItemBusPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        this(holder, tier, io, false);
    }

    public ItemBusPartMachine(IMachineBlockEntity holder, int tier, IO io, boolean enableShareInventory) {
        super(holder, tier, io);
        this.inventory = createInventory();
        this.circuitInventory = createCircuitItemHandler(io).shouldSearchContent(false);
        this.shareInventory = new CatalystItemHandler(this,
                enableShareInventory && io == IO.IN ? getShareInventorySlots(getTier()) : 0,
                IO.IN, IO.NONE)
                .shouldSearchContent(false);
        shareInventory.setCapabilityValidator(dir -> false);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    protected int getInventorySize() {
        return INVENTORY_SIZE[getTier()];
    }

    public static int getShareInventorySlots(int tier) {
        if (tier <= GTValues.HV) {
            return 0;
        } else if (tier <= GTValues.IV) {
            return 4;
        } else if (tier <= GTValues.ZPM) {
            return 9;
        } else {
            return 16;
        }
    }

    public static int getSlotMultiplier(int tier) {
        return 1 << (2 * tier);
    }

    protected boolean matchesFilter(ItemStack stack) {
        if (io == IO.IN) return true;
        var cover = getCoverContainer().getCoverAtSide(getFrontFacing().getOpposite());
        if (cover instanceof ItemFilterCover itemFilterCover) {
            return itemFilterCover.getItemFilter().test(stack);
        }
        return true;
    }

    protected NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(this, getInventorySize(), io, io,
                i -> new LargeStackItemHandler(i, getSlotMultiplier(getTier())))
                .setFilter(this::matchesFilter);
    }

    protected NotifiableItemStackHandler createCircuitItemHandler(IO io) {
        if (io == IO.IN) {
            return new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                    .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        } else {
            circuitSlotEnabled = false;
            return new NotifiableItemStackHandler(this, 0, IO.NONE);
        }
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(getInventory().storage);
        clearInventory(shareInventory.storage);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateInventorySubscription));
        }
        inventorySubs = getInventory().addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getControllers().forEach(controller -> {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().resetLastGroup();
            }
        });
    }

    @Override
    public void setDistinct(boolean distinct) {
        isDistinct = (io != IO.OUT && distinct);
        getControllers().forEach(controller -> {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().resetLastGroup();
            }
        });
    }

    @Override
    public int tintColor(int index) {
        if (index == 9) return getRealColor();
        return -1;
    }

    //////////////////////////////////////
    // ******** Auto IO *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateInventorySubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateInventorySubscription(newFacing);
    }

    protected void updateInventorySubscription() {
        updateInventorySubscription(getFrontFacing());
    }

    protected void updateInventorySubscription(Direction newFacing) {
        if (isWorkingEnabled() && ((io.support(IO.OUT) && !getInventory().isEmpty()) || io.support(IO.IN)) &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getPos(), newFacing)) {
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
                    getInventory().exportToNearby(getFrontFacing());
                } else if (io == IO.IN) {
                    getInventory().importFromNearby(getFrontFacing());
                } else if (io == IO.BOTH) {
                    getInventory().importFromNearby(getFrontFacing());
                    getInventory().exportToNearby(getFrontFacing().getOpposite());
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
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        InteractionResult superResult = super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
        if (superResult != InteractionResult.PASS) return superResult;
        if (io == IO.BOTH) return InteractionResult.PASS;
        if (playerIn.isShiftKeyDown()) {
            if (swapIO()) {
                return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public boolean swapIO() {
        BlockPos blockPos = getHolder().pos();
        MachineDefinition newDefinition = null;
        if (io == IO.IN) {
            newDefinition = GTMachines.ITEM_EXPORT_BUS[this.getTier()];
        } else if (io == IO.OUT) {
            newDefinition = GTMachines.ITEM_IMPORT_BUS[this.getTier()];
        }

        if (newDefinition == null) return false;
        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos).getBlockPos() instanceof IMachineBlockEntity newHolder) {
            if (newHolder.getMetaMachine() instanceof ItemBusPartMachine newMachine) {
                // We don't set the circuit or distinct busses, since
                // that doesn't make sense on an output bus.
                // Furthermore, existing inventory items
                // and conveyors will drop to the floor on block override.
                newMachine.setFrontFacing(this.getFrontFacing());
                newMachine.setUpwardsFacing(this.getUpwardsFacing());
                newMachine.setPaintingColor(this.getPaintingColor());
            }
        }
        return true;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    protected void refundAll(ClickData clickData) {
        if (!clickData.isRemote) {
            this.setWorkingEnabled(false);
            getInventory().exportToNearby(getFrontFacing());
        }
    }

    public void attachConfigurators(ConfiguratorPanel left, ConfiguratorPanel right) {
        attachAllowSameConfigurators(right);
        if (this.io == IO.IN) {
            left.attachConfigurators(
                    new ButtonConfigurator(new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("\ud83d\udd19")),
                            this::refundAll)
                            .setTooltips(List.of(Component.translatable("gtceu.gui.refund_all_item"))));
            if (isCircuitSlotEnabled()) {
                left.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
            }
            if (shareInventory.getSlots() != 0) {
                right.attachConfigurators(new FancyInvConfigurator(
                        shareInventory.storage, Component.translatable("gui.gtceu.share_inventory.title"))
                        .setTooltips(List.of(
                                Component.translatable("gui.gtceu.share_inventory.desc.1"))));
            }
            IDistinctPart.super.attachConfigurators(left, right);

        } else {
            super.attachConfigurators(left, right);
        }
    }

    @Override
    public Widget createUIWidget() {
        int colSize = LINE_NUM[getTier()];
        int rowSize = INVENTORY_SIZE[getTier()] / colSize;

        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(
                        new LargeStackSlotWidget(getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true,
                                io.support(IO.IN))
                                .setBackgroundTexture(GuiTextures.SLOT)
                                .setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}

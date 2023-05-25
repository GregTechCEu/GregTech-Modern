package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.msic.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote ConveyorCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConveyorCover extends CoverBehavior implements IUICover, IControllable {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConveyorCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);
    public final int tier;
    public final int maxItemTransferRate;
    @Persisted @Getter
    protected int transferRate;
    @Persisted @DescSynced @Getter
    protected IO io;
    @Persisted @Getter
    protected boolean isWorkingEnabled = true;
    @Persisted @DescSynced @Getter
    protected ItemStack filterItem;
    @Nullable
    private ItemFilter filterHandler;
    protected int itemsLeftToTransferLastSecond;
    private TickableSubscription subscription;

    public ConveyorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = 2 * (int) Math.pow(4, tier); // 8 32 128 512 1024
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.filterItem = ItemStack.EMPTY;
        this.io = IO.OUT;
        if (coverHolder.isRemote()) {
            addSyncUpdateListener("io", (s, o, t1) -> coverHolder.scheduleRenderUpdate());
        }
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canAttach() {
        return ItemTransferHelper.getItemTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
    }

    public void setTransferRate(int transferRate) {
        if (transferRate <= maxItemTransferRate) {
            this.transferRate = transferRate;
        }
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (coverHolder.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSubscription));
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    //////////////////////////////////////
    //*****     Transfer Logic     *****//
    //////////////////////////////////////

    public Predicate<ItemStack> getFilterHandler() {
        if (filterHandler == null) {
            if (filterItem.isEmpty()) {
                return itemStack -> true;
            } else {
                filterHandler = ItemFilter.loadFilter(filterItem);
            }
        }
        return filterHandler;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        updateSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            updateSubscription();
        }
    }

    protected void updateSubscription() {
        var level = coverHolder.getLevel();
        var pos = coverHolder.getPos();
        if (isWorkingEnabled() && ItemTransferHelper.getItemTransfer(level, pos.relative(attachedSide), attachedSide.getOpposite()) != null) {
            subscription = coverHolder.subscribeServerTick(subscription, this::update);
        } else if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 == 0) {
            if (itemsLeftToTransferLastSecond > 0) {
                var level = coverHolder.getLevel();
                var pos = coverHolder.getPos();
                var itemHandler = ItemTransferHelper.getItemTransfer(level, pos.relative(attachedSide), attachedSide.getOpposite());
                var myItemHandler = ItemTransferHelper.getItemTransfer(level, pos, attachedSide);
                if (itemHandler != null && myItemHandler != null) {
                    int totalTransferred = doTransferItems(itemHandler, myItemHandler, itemsLeftToTransferLastSecond);
                    this.itemsLeftToTransferLastSecond -= totalTransferred;
                }
            }
            if (timer % 20 == 0) {
                this.itemsLeftToTransferLastSecond = transferRate;
            }
            updateSubscription();
        }
    }

    protected int doTransferItems(IItemTransfer itemHandler, IItemTransfer myItemHandler, int maxTransferAmount) {
        return doTransferItemsAny(itemHandler, myItemHandler, maxTransferAmount);
    }

    protected int doTransferItemsAny(IItemTransfer itemHandler, IItemTransfer myItemHandler, int maxTransferAmount) {
        if (io == IO.IN) {
            return moveInventoryItems(itemHandler, myItemHandler, maxTransferAmount);
        } else if (io == IO.OUT) {
            return moveInventoryItems(myItemHandler, itemHandler, maxTransferAmount);
        }
        return 0;
    }

    protected int moveInventoryItems(IItemTransfer sourceInventory, IItemTransfer targetInventory, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }

            if (!getFilterHandler().test(sourceStack)) {
                continue;
            }

            ItemStack remainder = ItemTransferHelper.insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                if (!sourceStack.isEmpty()) {
                    ItemTransferHelper.insertItem(targetInventory, sourceStack, false);
                    itemsLeftToTransfer -= sourceStack.getCount();

                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }


    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var filterContainer = new ItemStackTransfer(filterItem);
        filterContainer.setFilter(itemStack -> ItemFilter.FILTERS.containsKey(itemStack.getItem()));
        var filterGroup = new WidgetGroup(0, 70, 176, 60);
        if (!filterItem.isEmpty()) {
            filterHandler = ItemFilter.loadFilter(filterItem);
            filterGroup.addWidget(filterHandler.openConfigurator((176 - 80) / 2, (60 - 55) / 2));
        }
        return new ModularUI(176, 130 + 82, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 5, LocalizationUtils.format("cover.conveyor.title", GTValues.VN[tier])))
                .widget(new ButtonWidget(10, 20, 30, 20,
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("-1")), cd -> {
                          if (!cd.isRemote) {
                              int amount = cd.isCtrlClick ? cd.isShiftClick ? 512 : 64 : cd.isShiftClick ? 8 : 1;
                              setTransferRate(Mth.clamp(getTransferRate() - amount, 1, maxItemTransferRate));
                          }
                        }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"))
                .widget(new ButtonWidget(136, 20, 30, 20,
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("+1")), cd -> {
                    if (!cd.isRemote) {
                        int amount = cd.isCtrlClick ? cd.isShiftClick ? 512 : 64 : cd.isShiftClick ? 8 : 1;
                        setTransferRate(Mth.clamp(getTransferRate() + amount, 1, maxItemTransferRate));
                    }
                }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"))
                .widget(new TextFieldWidget(42, 20, 92, 20, () -> String.valueOf(transferRate), val -> setTransferRate(Mth.clamp(Integer.parseInt(val), 1, maxItemTransferRate))).setNumbersOnly(1, maxItemTransferRate))
                .widget(new SwitchWidget(10, 45, 75, 20, (clickData, value) -> {
                    if (!clickData.isRemote) {
                        setIo(value ? IO.IN : IO.OUT);
                    }
                })
                        .setTexture(
                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("cover.conveyor.mode.export")),
                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("cover.conveyor.mode.import")))
                        .setPressed(io == IO.IN))
                .widget(new SlotWidget(filterContainer, 0, 90, 45)
                        .setChangeListener(() -> {
                            if (isRemote()) {
                                if (!filterContainer.getStackInSlot(0).isEmpty() && !filterItem.isEmpty()) {
                                    return;
                                }
                            }
                            this.filterItem = filterContainer.getStackInSlot(0);
                            this.filterHandler = null;
                            filterGroup.clearAllWidgets();
                            if (!filterItem.isEmpty()) {
                                filterHandler = ItemFilter.loadFilter(filterItem);
                                filterGroup.addWidget(filterHandler.openConfigurator((176 - 80) / 2, (60 - 55) / 2));
                            }
                        })
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)))
                .widget(filterGroup)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),  GuiTextures.SLOT, 7, 130, true));
    }
}

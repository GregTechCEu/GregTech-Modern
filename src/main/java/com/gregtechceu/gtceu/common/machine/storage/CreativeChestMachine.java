package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.InfiniteItemTransferProxy;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public class CreativeChestMachine extends QuantumChestMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeChestMachine.class,
            QuantumChestMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DropSaved
    private int itemsPerCycle = 1;
    @Persisted
    @DropSaved
    private int ticksPerCycle = 1;

    private final InfiniteItemTransferProxy capabilityTransferProxy;

    public CreativeChestMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, -1);

        capabilityTransferProxy = new InfiniteItemTransferProxy(cache, true, true);
    }

    @Nullable
    @Override
    public IItemTransfer getItemTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        if (side == null || (useCoverCapability && coverContainer.hasCover(side)))
            return super.getItemTransferCap(side, useCoverCapability);

        return capabilityTransferProxy;
    }

    @Override
    protected NotifiableItemStackHandler createCacheItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 1, IO.BOTH, IO.NONE) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                stack.setCount(1);
                this.storage.setStackInSlot(slot, stack);
                this.onContentsChanged();
            }

            @Override
            public void onContentsChanged() {
                super.onContentsChanged();
                if (!isRemote()) {
                    stored = getStackInSlot(0).copy();
                    storedAmount = stored.getCount();
                    stored.setCount(1);
                }
            }
        };
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % ticksPerCycle == 0) {
            if (isAutoOutputItems() && getOutputFacingItems() != null) {
                updateItemTick();
            }
            updateAutoOutputSubscription();
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 176, 131);
        group.addWidget(new PhantomSlotWidget(cache, 0, 36, 6)
                .setClearSlotOnRightClick(true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setChangeListener(this::markDirty));
        group.addWidget(new LabelWidget(7, 9, "gtceu.creative.chest.item"));
        group.addWidget(new ImageWidget(7, 48, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 50, 152, 10, () -> String.valueOf(itemsPerCycle), value -> {
            if (!value.isEmpty()) {
                itemsPerCycle = Integer.parseInt(value);
            }
        }).setMaxStringLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 28, "gtceu.creative.chest.ipc"));

        group.addWidget(new ImageWidget(7, 85, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 87, 152, 10, () -> String.valueOf(ticksPerCycle), value -> {
            if (!value.isEmpty()) {
                ticksPerCycle = Integer.parseInt(value);
            }
        }).setMaxStringLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 65, "gtceu.creative.chest.tpc"));

        group.addWidget(new SwitchWidget(7, 101, 162, 20, (clickData, value) -> setWorkingEnabled(value))
                .setTexture(
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.off")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.on")))
                .setPressed(isWorkingEnabled()));

        return group;
    }

    public void updateItemTick() {
        ItemStack stack = cache.getStackInSlot(0).copy();
        this.stored = stack; // For rendering purposes
        if (ticksPerCycle == 0 || getOffsetTimer() % ticksPerCycle != 0) return;
        if (getLevel().isClientSide || !isWorkingEnabled() || stack.isEmpty()) return;

        IItemTransfer transfer = ItemTransferHelper.getItemTransfer(getLevel(),
                getPos().relative(getOutputFacingItems()), getOutputFacingItems().getOpposite());
        if (transfer != null) {
            stack.setCount(itemsPerCycle);

            ItemStack remainder = GTTransferUtils.insertItem(transfer, stack, true);
            int amountToInsert = stack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                GTTransferUtils.insertItem(transfer, stack, false);
            }
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (hit.getDirection() == getFrontFacing()) {
            var held = player.getMainHandItem();
            if (!held.isEmpty() && (ItemTransferHelper.canItemStacksStack(held, stored) || stored.isEmpty())) { // push
                if (!isRemote()) {
                    var remaining = cache.insertItem(0, held, false);
                    player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (direction == getFrontFacing() && !isRemote()) {
            if (!stored.isEmpty()) { // pull
                var drained = cache.extractItem(0, player.isShiftKeyDown() ? stored.getItem().getMaxStackSize() : 1,
                        false);
                if (!drained.isEmpty()) {
                    if (player.addItem(drained)) {
                        Block.popResource(world, getPos().relative(getFrontFacing()), drained);
                    }
                }
            }
        }
        return super.onLeftClick(player, world, hand, pos, direction);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

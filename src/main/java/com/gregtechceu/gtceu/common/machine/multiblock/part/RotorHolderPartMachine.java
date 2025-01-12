package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.*;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote RotorHolderPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RotorHolderPartMachine extends TieredPartMachine
                                    implements IMachineLife, IRotorHolderMachine, IInteractedMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            RotorHolderPartMachine.class, TieredPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableItemStackHandler inventory;
    @Getter
    public final int maxRotorHolderSpeed;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    @RequireRerender
    public int rotorSpeed;
    @Setter
    @Persisted
    @DescSynced
    @RequireRerender
    public int rotorColor; // 0 - no rotor
    @Nullable
    protected TickableSubscription rotorSpeedSubs;
    @Nullable
    protected ISubscription rotorInvSubs;

    public RotorHolderPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH);
        this.maxRotorHolderSpeed = 2000 + 1000 * tier;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return rotorColor;
        }
        return super.tintColor(index);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updateRotorSubscription();
            rotorInvSubs = this.inventory.addChangedListener(this::onRotorInventoryChanged);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (rotorInvSubs != null) {
            rotorInvSubs.unsubscribe();
        }
    }

    @Override
    public boolean canShared() {
        return false;
    }

    //////////////////////////////////////
    // ****** Rotor Holder ******//
    //////////////////////////////////////

    private void onRotorInventoryChanged() {
        var stack = getRotorStack();
        var rotorBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
        var color = 0;
        if (rotorBehaviour != null) {
            color = rotorBehaviour.getPartMaterial(stack).getMaterialARGB();
        }
        this.rotorColor = color;
    }

    @Override
    public boolean hasRotor() {
        return inventory.getStackInSlot(0) != ItemStack.EMPTY;
    }

    protected void updateRotorSubscription() {
        if (getRotorSpeed() > 0) {
            rotorSpeedSubs = subscribeServerTick(rotorSpeedSubs, this::updateRotorSpeed);
        } else if (rotorSpeedSubs != null) {
            rotorSpeedSubs.unsubscribe();
            rotorSpeedSubs = null;
        }
    }

    private void updateRotorSpeed() {
        if (isFormed() && getControllers().first() instanceof IWorkableMultiController workable) {
            if (workable.getRecipeLogic().isWorking()) return;
        }
        if (!hasRotor()) {
            setRotorSpeed(0);
        } else if (getRotorSpeed() > 0) {
            setRotorSpeed(Math.max(0, getRotorSpeed() - SPEED_DECREMENT));
        }
        updateRotorSubscription();
    }

    @Override
    public boolean onWorking(IWorkableMultiController controller) {
        if (getRotorSpeed() < getMaxRotorHolderSpeed()) {
            setRotorSpeed(getRotorSpeed() + SPEED_INCREMENT);
            updateRotorSubscription();
        }
        if (self().getOffsetTimer() % 20 == 0) {
            var numMaintenanceProblems = 0;
            if (isFormed() && getControllers().first() instanceof IMaintenanceMachine maintenance) {
                numMaintenanceProblems = maintenance.getNumMaintenanceProblems();
            }
            damageRotor(1 + numMaintenanceProblems);
        }
        return true;
    }

    public int getTierDifference() {
        if (isFormed() && getControllers().first() instanceof ITieredMachine tieredMachine) {
            return getTier() - tieredMachine.getTier();
        }
        return -1;
    }

    @Override
    public ItemStack getRotorStack() {
        return inventory.getStackInSlot(0);
    }

    @Override
    public void setRotorStack(ItemStack rotorStack) {
        inventory.setStackInSlot(0, rotorStack);
        inventory.onContentsChanged();
    }

    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isRemote() && getRotorSpeed() > 0 && !player.isCreative()) {
            player.hurt(GTDamageTypes.TURBINE.source(level),
                    TurbineRotorBehaviour.getBehaviour(getRotorStack()).getDamage(getRotorStack()));
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 18 + 16, 18 + 16);
        var container = new WidgetGroup(4, 4, 18 + 8, 18 + 8);
        container.addWidget(new BlockableSlotWidget(inventory.storage, 0, 4, 4)
                .setIsBlocked(() -> rotorSpeed != 0)
                .setBackground(GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY));
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}

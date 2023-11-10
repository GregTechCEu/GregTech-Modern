package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.*;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote RotorHolderPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RotorHolderPartMachine extends TieredPartMachine implements IMachineModifyDrops, IRotorHolderMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RotorHolderPartMachine.class, TieredPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableItemStackHandler inventory;
    @Getter
    public final int maxRotorHolderSpeed;
    @Getter @Setter
    @Persisted @DescSynced @RequireRerender
    public int rotorSpeed;
    @Setter @Persisted @DescSynced @RequireRerender
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
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, inventory.storage);
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

    //////////////////////////////////////
    //******     Rotor Holder     ******//
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
        return rotorColor != 0;
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
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IWorkableMultiController workableMultiController) {
                if (workableMultiController.getRecipeLogic().isWorking()) {
                    return;
                }
            }
        }
        if (!hasRotor()) {
            setRotorSpeed(0);
        } else if (getRotorSpeed() > 0) {
            setRotorSpeed(Math.max(0, getRotorSpeed() - SPEED_DECREMENT));
        }
        updateRotorSubscription();
    }

    @Override
    public void onWorking(IWorkableMultiController controller) {
        if (getRotorSpeed() < getMaxRotorHolderSpeed()) {
            setRotorSpeed(getRotorSpeed() + SPEED_INCREMENT);
            updateRotorSubscription();
        }
        if (self().getOffsetTimer() % 20 == 0) {
            var numMaintenanceProblems = 0;
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof IMaintenanceMachine maintenance) {
                    numMaintenanceProblems = maintenance.getNumMaintenanceProblems();
                    break;
                }
            }
            damageRotor(1 + numMaintenanceProblems);
        }
    }

    public int getTierDifference() {
        for (IMultiController controller : getControllers()) {
            if (controller instanceof ITieredMachine tieredMachine) {
                return getTier() - tieredMachine.getTier();
            }
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

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 18 + 16, 18 + 16);
        var container = new WidgetGroup(4, 4, 18 + 8, 18 + 8);
        container.addWidget(new SlotWidget(inventory.storage, 0, 4, 4, true, true)
                .setBackground(GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY));
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }

}

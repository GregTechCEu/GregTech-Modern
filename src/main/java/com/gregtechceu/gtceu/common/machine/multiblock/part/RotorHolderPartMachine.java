package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
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
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.*;

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
    @Persisted
    @DescSynced
    public int rotorSpeed;
    @Setter
    @Persisted
    @DescSynced
    @NotNull
    public Material rotorMaterial = GTMaterials.NULL; // 0 - no rotor
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
        if (index >= 2) {
            return getRotorMaterial().getLayerARGB(index - 2);
        } else if (index <= -103) {
            return getRotorMaterial().getLayerARGB(index + 2);
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

    @Override
    public @NotNull Material getRotorMaterial() {
        // handles clients trying to get the material before server data sync
        // noinspection ConstantValue
        if (rotorMaterial == null) {
            return GTMaterials.NULL;
        }
        return rotorMaterial;
    }

    private void onRotorInventoryChanged() {
        var stack = getRotorStack();
        var rotorBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
        if (rotorBehaviour != null) {
            this.rotorMaterial = rotorBehaviour.getPartMaterial(stack);

            boolean emissive = this.rotorMaterial.hasProperty(PropertyKey.ORE) &&
                    this.rotorMaterial.getProperty(PropertyKey.ORE).isEmissive();
            setRenderState(getRenderState()
                    .setValue(HAS_ROTOR, true)
                    .setValue(IS_EMISSIVE_ROTOR, emissive));
        } else {
            this.rotorMaterial = GTMaterials.NULL;
            setRenderState(getRenderState()
                    .setValue(HAS_ROTOR, false)
                    .setValue(IS_EMISSIVE_ROTOR, false));
        }
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

    public void setRotorSpeed(int rotorSpeed) {
        if ((this.rotorSpeed > 0 && rotorSpeed <= 0) || (this.rotorSpeed <= 0 && rotorSpeed > 0)) {
            setRenderState(getRenderState().setValue(IS_ROTOR_SPINNING, rotorSpeed > 0));
        }
        this.rotorSpeed = rotorSpeed;
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
            TurbineRotorBehaviour behaviour = TurbineRotorBehaviour.getBehaviour(getRotorStack());
            if (behaviour != null) {
                player.hurt(GTDamageTypes.TURBINE.source(level), behaviour.getDamage(getRotorStack()));
            }
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

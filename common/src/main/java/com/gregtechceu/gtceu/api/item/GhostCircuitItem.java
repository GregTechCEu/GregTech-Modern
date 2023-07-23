package com.gregtechceu.gtceu.api.item;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GhostCircuitItem implements IItemTransfer {

    /**
     * Special circuit value indicating no circuit value is set.
     */
    public static final int NO_CONFIG = -1;

    private final List<MetaTileEntity> notifiableEntities = new ArrayList<>();

    private int circuitValue = NO_CONFIG;
    private ItemStack circuitStack = ItemStack.EMPTY;

    /**
     * Return the circuit value, or {@link GhostCircuitItem#NO_CONFIG} if
     * no circuit value is set.
     *
     * @return A valid circuit value if present, otherwise {@link GhostCircuitItem#NO_CONFIG}
     */
    public int getCircuitValue() {
        return this.circuitValue;
    }

    /**
     * Returns whether this instance contains valid circuit value.
     *
     * @return Whether this instance contains valid circuit value.
     */
    public boolean hasCircuitValue() {
        return this.circuitValue != NO_CONFIG;
    }

    /**
     * Set the circuit value of this inventory to given value and update the item. The item is set to circuit item
     * with corresponding int value, or an empty itemstack if {@link GhostCircuitItem#NO_CONFIG} is given.
     * <p>
     * The value is expected to be either a valid circuit value
     * ({@link IntCircuitIngredient#CIRCUIT_MIN} ~ {@link IntCircuitIngredient#CIRCUIT_MAX}, both inclusive)
     * or {@link GhostCircuitItem#NO_CONFIG}; any other value will produce IllegalArgumentException.
     *
     * @param config New config value
     * @throws IllegalArgumentException On invalid input
     */
    public void setCircuitValue(int config) {
        if (config == NO_CONFIG) {
            this.circuitValue = NO_CONFIG;
            this.circuitStack = ItemStack.EMPTY;
        } else if (config >= IntCircuitIngredient.CIRCUIT_MIN && config <= IntCircuitIngredient.CIRCUIT_MAX) {
            this.circuitValue = config;
            this.circuitStack = IntCircuitIngredient.getIntegratedCircuit(config);
        } else {
            throw new IllegalArgumentException("Circuit value out of range: " + config);
        }
        for (MetaTileEntity mte : notifiableEntities) {
            if (mte != null && mte.isValid()) {
                addToNotifiedList(mte, this, false);
            }
        }
    }

    /**
     * Set the circuit value of this inventory from given item. Circuit value is set to valid circuit value only if
     * the supplied item is int circuit; providing any other item will set the circuit value to {@link
     * GhostCircuitItem#NO_CONFIG}.
     *
     * @param stack Item stack to read circuit value from
     */
    public void setCircuitValueFromStack(@Nonnull ItemStack stack) {
        setCircuitValue(!stack.isEmpty() && IntCircuitIngredient.isIntegratedCircuit(stack) ?
                IntCircuitIngredient.getCircuitConfiguration(stack) : NO_CONFIG);
    }

    /**
     * Add given value to preexisting circuit value. The resulting value is capped in range of valid circuit value.
     * If there was no circuit value present, this method does nothing.
     *
     * @param configDelta Amount of circuit value to add, can be negative
     */
    public void addCircuitValue(int configDelta) {
        if (hasCircuitValue()) {
            setCircuitValue(MathHelper.clamp(getCircuitValue() + configDelta,
                    IntCircuitIngredient.CIRCUIT_MIN, IntCircuitIngredient.CIRCUIT_MAX));
        }
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        validateSlot(slot);
        setCircuitValueFromStack(stack);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        return this.circuitStack;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return null;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        validateSlot(slot);
        return stack; // reject all item insertions
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        validateSlot(slot);
        if (!simulate) {
            setCircuitValue(NO_CONFIG);
        }
        return this.circuitStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }

    protected void validateSlot(int slot) {
        if (slot != 0) throw new IndexOutOfBoundsException("Slot index out of bounds: " + slot);
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        if (metaTileEntity == null) return;
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    public void write(@Nonnull NBTTagCompound tag) {
        if (this.circuitValue != NO_CONFIG) {
            tag.setByte("GhostCircuit", (byte) this.circuitValue);
        }
    }

    public void read(@Nonnull NBTTagCompound tag) {
        int circuitValue = tag.hasKey("GhostCircuit", Constants.NBT.TAG_ANY_NUMERIC) ? tag.getInteger("GhostCircuit") : NO_CONFIG;
        if (circuitValue < IntCircuitIngredient.CIRCUIT_MIN || circuitValue > IntCircuitIngredient.CIRCUIT_MAX)
            circuitValue = NO_CONFIG;
        setCircuitValue(circuitValue);
    }
}

package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.*;

import com.lowdragmc.lowdraglib.gui.editor.runtime.PersistedParser;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine.MAX_PATTERN_COUNT;

@SuppressWarnings("unused")
public class MEPatternBufferProxy extends TieredIOPartMachine implements IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferProxy.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @ReadOnlyManaged(onDirtyMethod = "onDirty", serializeMethod = "onSave", deserializeMethod = "onLoadCircuit")
    protected NotifiableItemStackHandler circuitInventorySimulated;

    @Getter
    @ReadOnlyManaged(onDirtyMethod = "onDirty", serializeMethod = "onSave", deserializeMethod = "onLoadShare")
    protected NotifiableItemStackHandler shareInventory;

    @Getter
    @ReadOnlyManaged(onDirtyMethod = "onDirtyFluid", serializeMethod = "onSaveFluid", deserializeMethod = "onLoadFluid")
    protected NotifiableFluidTank shareTank;

    @Getter
    @ReadOnlyManaged(onDirtyMethod = "onDirtyInternal",
                     serializeMethod = "onSaveInternal",
                     deserializeMethod = "onLoadInternal")
    protected MEPatternBufferPartMachine.InternalSlot[] internalInventory = new MEPatternBufferPartMachine.InternalSlot[MAX_PATTERN_COUNT];

    @Persisted
    @Getter
    private BlockPos bufferPos;

    public MEPatternBufferProxy(IMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.BOTH);
    }

    public boolean setIOBuffer(BlockPos pos) {
        if (pos == null) return false;
        if (MetaMachine.getMachine(getLevel(), pos) instanceof MEPatternBufferPartMachine machine) {
            this.bufferPos = pos;

            this.internalInventory = machine.internalInventory;
            this.circuitInventorySimulated = machine.getCircuitInventorySimulated();
            this.shareInventory = machine.getShareInventory();
            this.shareTank = machine.getShareTank();

            machine.addProxy(this);

            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private MEPatternBufferPartMachine getIOBuffer() {
        if (bufferPos == null) return null;
        if (MetaMachine.getMachine(getLevel(), bufferPos) instanceof MEPatternBufferPartMachine buffer) {
            return buffer;
        } else {
            this.bufferPos = null;
            return null;
        }
    }

    @Override
    public MetaMachine self() {
        var buffer = getIOBuffer();
        return buffer != null ? buffer.self() : super.self();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        var buffer = getIOBuffer();
        return buffer != null && super.shouldOpenUI(player, hand, hit);
    }

    @Override
    public @Nullable ModularUI createUI(Player entityPlayer) {
        GTCEu.LOGGER.warn("'createUI' of the Crafting Buffer Proxy was incorrectly called!");
        return null;
    }

    @Override
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        var handlers = new ArrayList<>(super.getRecipeHandlers());
        if (getIOBuffer() != null)
            handlers.addAll(getIOBuffer().getRecipeHandlers());
        return handlers;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachineRemoved() {
        if (MetaMachine.getMachine(getLevel(), this.bufferPos) instanceof MEPatternBufferPartMachine machine) {
            machine.removeProxy(this);
        }
    }

    private boolean onDirty(NotifiableItemStackHandler handler) {
        return handler != null && (handler.getSyncStorage().hasDirtySyncFields() ||
                handler.getSyncStorage().hasDirtyPersistedFields());
    }

    private NotifiableItemStackHandler onLoadShare(CompoundTag tag) {
        if (shareInventory != null)
            PersistedParser.deserializeNBT(tag, new HashMap<>(), NotifiableItemStackHandler.class, this.shareInventory);
        return this.shareInventory;
    }

    private NotifiableItemStackHandler onLoadCircuit(CompoundTag tag) {
        if (circuitInventorySimulated != null)
            PersistedParser.deserializeNBT(tag, new HashMap<>(), NotifiableItemStackHandler.class,
                    this.circuitInventorySimulated);
        return this.circuitInventorySimulated;
    }

    private CompoundTag onSave(NotifiableItemStackHandler handler) {
        CompoundTag tag = new CompoundTag();
        if (handler != null) {
            PersistedParser.serializeNBT(tag, NotifiableItemStackHandler.class, handler);
        }
        return tag;
    }

    private boolean onDirtyFluid(NotifiableFluidTank handler) {
        return handler != null && (handler.getSyncStorage().hasDirtySyncFields() ||
                handler.getSyncStorage().hasDirtyPersistedFields());
    }

    private NotifiableFluidTank onLoadFluid(CompoundTag tag) {
        if (shareTank != null)
            PersistedParser.deserializeNBT(tag, new HashMap<>(), NotifiableFluidTank.class, this.shareTank);
        return this.shareTank;
    }

    private CompoundTag onSaveFluid(NotifiableFluidTank handler) {
        CompoundTag tag = new CompoundTag();
        if (handler != null) {
            PersistedParser.serializeNBT(tag, NotifiableFluidTank.class, handler);
        }
        return tag;
    }

    private boolean onDirtyInternal(MEPatternBufferPartMachine.InternalSlot[] handler) {
        return handler != null;
    }

    private MEPatternBufferPartMachine.InternalSlot[] onLoadInternal(CompoundTag tag) {
        if (internalInventory != null)
            PersistedParser.deserializeNBT(tag, new HashMap<>(), MEPatternBufferPartMachine.InternalSlot[].class,
                    this.internalInventory);
        return this.internalInventory;
    }

    private CompoundTag onSaveInternal(MEPatternBufferPartMachine.InternalSlot[] handler) {
        CompoundTag tag = new CompoundTag();
        if (handler != null) {
            PersistedParser.serializeNBT(tag, MEPatternBufferPartMachine.InternalSlot[].class, handler);
        }
        return tag;
    }
}

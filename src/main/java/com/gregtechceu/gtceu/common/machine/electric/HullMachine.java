package com.gregtechceu.gtceu.common.machine.electric;

import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHostTrait;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HullMachine extends TieredPartMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HullMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final Object gridNodeHost;
    @Persisted
    protected NotifiableEnergyContainer energyContainer;

    public HullMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        if (GTCEu.isAE2Loaded()) {
            this.gridNodeHost = new GridNodeHostTrait(this);
        } else {
            this.gridNodeHost = null;
        }
        reinitializeEnergyContainer();
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = new NotifiableEnergyContainer(this, tierVoltage * 16L, tierVoltage, 1L, tierVoltage, 1L);
        this.energyContainer.setSideOutputCondition(s -> s == getFrontFacing());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (GTCEu.isAE2Loaded() && gridNodeHost instanceof GridNodeHostTrait connectedBlockEntity && getLevel() instanceof ServerLevel level) {
            level.getServer().tell(new TickTask(0, connectedBlockEntity::init));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (GTCEu.isAE2Loaded() && gridNodeHost instanceof GridNodeHostTrait connectedBlockEntity) {
            connectedBlockEntity.getMainNode().destroy();
        }
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);
        if (isFacingValid(facing)) {
            if (GTCEu.isAE2Loaded() && gridNodeHost instanceof GridNodeHostTrait connectedBlockEntity) {
                connectedBlockEntity.init();
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (GTCEu.isAE2Loaded() && gridNodeHost instanceof IGridConnectedBlockEntity connectedBlockEntity) {
            CompoundTag nbt = new CompoundTag();
            connectedBlockEntity.getMainNode().saveToNBT(nbt);
            tag.put("grid_node", nbt);
        }
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (GTCEu.isAE2Loaded() && gridNodeHost instanceof IGridConnectedBlockEntity connectedBlockEntity) {
            connectedBlockEntity.getMainNode().loadFromNBT(tag.getCompound("grid_node"));
        }
    }
}

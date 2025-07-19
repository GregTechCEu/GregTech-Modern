package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHostTrait;
import com.gregtechceu.gtceu.syncdata.annotations.CustomDataField;
import com.gregtechceu.gtceu.syncdata.annotations.FieldDataModifier;
import com.gregtechceu.gtceu.syncdata.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import appeng.me.helpers.IGridConnectedBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HullMachine extends TieredPartMachine {

    @CustomDataField
    @SaveField(nbtKey = "grid_node")
    private final Object gridNodeHost;

    @SaveField
    protected NotifiableEnergyContainer energyContainer;

    public HullMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        if (GTCEu.Mods.isAE2Loaded()) {
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
        if (GTCEu.Mods.isAE2Loaded() && gridNodeHost instanceof GridNodeHostTrait connectedBlockEntity &&
                getLevel() instanceof ServerLevel level) {
            level.getServer().tell(new TickTask(0, connectedBlockEntity::init));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (GTCEu.Mods.isAE2Loaded() && gridNodeHost instanceof GridNodeHostTrait connectedBlockEntity) {
            connectedBlockEntity.getMainNode().destroy();
        }
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);
        if (isFacingValid(facing)) {
            if (GTCEu.Mods.isAE2Loaded() && gridNodeHost instanceof GridNodeHostTrait connectedBlockEntity) {
                connectedBlockEntity.init();
            }
        }
    }

    @FieldDataModifier(fieldName = "gridNodeHost", target = FieldDataModifier.ModifyTarget.SAVE_NBT)
    private Tag saveGridNodeHost(Tag saved) {
        if (GTCEu.Mods.isAE2Loaded() && gridNodeHost instanceof IGridConnectedBlockEntity connectedBlockEntity) {
            var compound = new CompoundTag();
            connectedBlockEntity.getMainNode().saveToNBT(compound);
            return compound;
        }
        return saved;
    }

    @FieldDataModifier(fieldName = "gridNodeHost", target = FieldDataModifier.ModifyTarget.LOAD_NBT)
    private void loadGridNodeHost(Tag saved) {
        if (GTCEu.Mods.isAE2Loaded() && gridNodeHost instanceof IGridConnectedBlockEntity connectedBlockEntity &&
                saved instanceof CompoundTag tag) {
            connectedBlockEntity.getMainNode().loadFromNBT(tag);
        }
    }

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }
}

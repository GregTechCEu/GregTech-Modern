package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCAComponentHatch;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class HPCAComponentPartMachine extends TieredPartMachine implements IHPCAComponentHatch, IMachineModifyDrops {

    @Persisted
    @DescSynced
    private boolean damaged;

    public HPCAComponentPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.ZPM);
    }

    public abstract boolean isAdvanced();

    public boolean doesAllowBridging() {
        return false;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    // Handle damaged state

    @Override
    public final boolean isBridge() {
        return doesAllowBridging() && !(canBeDamaged() && isDamaged());
    }

    @Override
    public boolean isDamaged() {
        return canBeDamaged() && damaged;
    }

    @Override
    public void setDamaged(boolean damaged) {
        if (!canBeDamaged()) return;
        if (this.damaged != damaged) {
            this.damaged = damaged;
            markDirty();
        }
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        if (canBeDamaged() && isDamaged()) {
            if (isAdvanced()) {
                drops.add(GTBlocks.ADVANCED_COMPUTER_CASING.asStack());
            } else {
                drops.add(GTBlocks.COMPUTER_CASING.asStack());
            }
        }
    }
}
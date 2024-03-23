package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class HPCAComponentPartMachine extends MultiblockPartMachine implements IHPCAComponentHatch {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HPCAComponentPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced @RequireRerender
    private boolean damaged;

    public HPCAComponentPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public abstract boolean isAdvanced();

    public boolean doesAllowBridging() {
        return false;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
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

    /*
    @Override
    public boolean shouldDropWhenDestroyed() {
        return super.shouldDropWhenDestroyed() && !(canBeDamaged() && isDamaged());
    }

    @Override
    public void getDrops(NonNullList<ItemStack> dropsList, @Nullable EntityPlayer harvester) {
        if (canBeDamaged() && isDamaged()) {
            if (isAdvanced()) {
                dropsList.add(MetaBlocks.COMPUTER_CASING
                    .getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING));
            } else {
                dropsList
                    .add(MetaBlocks.COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING));
            }
        }
    }
    */

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

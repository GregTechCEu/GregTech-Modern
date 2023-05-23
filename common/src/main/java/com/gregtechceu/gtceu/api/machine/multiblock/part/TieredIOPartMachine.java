package com.gregtechceu.gtceu.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtlib.syncdata.annotation.DescSynced;
import com.gregtechceu.gtlib.syncdata.annotation.Persisted;
import com.gregtechceu.gtlib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote TieredIOPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredIOPartMachine extends TieredPartMachine implements IControllable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TieredIOPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    protected final IO io;

    /**
     * AUTO IO working?
     */
    @Getter @Setter @Persisted @DescSynced
    protected boolean workingEnabled;

    public TieredIOPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier);
        this.io = io;
        this.workingEnabled = true;
        if (isRemote()) {
            addSyncUpdateListener("workingEnabled", this::scheduleRender);
        }
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

}

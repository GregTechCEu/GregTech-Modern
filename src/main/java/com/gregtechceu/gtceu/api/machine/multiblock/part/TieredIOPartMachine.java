package com.gregtechceu.gtceu.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredIOPartMachine extends TieredPartMachine implements IControllable {

    protected final IO io;

    /**
     * AUTO IO working?
     */
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean workingEnabled;

    public TieredIOPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier);
        this.io = io;
        this.workingEnabled = true;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("workingEnabled");
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    /*
     * @Nullable
     *
     * @Override
     * public PageGroupingData getPageGroupingData() {
     * return switch (this.io) {
     * case IN -> new PageGroupingData("gtceu.multiblock.page_switcher.io.import", 1);
     * case OUT -> new PageGroupingData("gtceu.multiblock.page_switcher.io.export", 2);
     * case BOTH -> new PageGroupingData("gtceu.multiblock.page_switcher.io.both", 3);
     * case NONE -> null;
     * };
     * }
     */
}

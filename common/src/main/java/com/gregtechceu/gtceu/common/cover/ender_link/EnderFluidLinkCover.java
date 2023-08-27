package com.gregtechceu.gtceu.common.cover.ender_link;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends PumpCover {
    private @Nullable EnderLinkControllerMachine controller;

    @Persisted @DescSynced @Getter
    private int channel = 1;

    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, GTValues.HV); // TODO support multiple tiers
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // TODO attach this instance to the controller if it is in a loaded chunk
    }

    @Override
    public void onUnload() {
        super.onUnload();
        // TODO detach this instance from linked controller if it is loaded
    }

    // TODO add a method for changing the linked controller (using some sort of link item) that:
    //      - ensures the controller's chunk is loaded
    //      - registers this cover on the controller
    //      - unregisters this cover from the previous controller

    @Override
    public void onRemoved() {
        super.onRemoved();
        // TODO unregister linked cover from controller and (on the controller's side) detach this instance from it
    }

    @Override
    protected @Nullable IFluidTransfer getAdjacentFluidTransfer() {
        if (controller == null)
            return null;

        return controller.getVirtualFluidTransfer(this.channel);
    }

    public void setChannel(int channel) {
        if (controller == null)
            return; // TODO figure out if there's a better way to do this

        this.channel = Mth.clamp(channel, 1, controller.getMaxChannels());
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderFluidLinkCover.class, PumpCover.MANAGED_FIELD_HOLDER);
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.DirectionalGlobalPos;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.cover.ender_link.EnderFluidLinkCover;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.gregtechceu.gtceu.api.GTValues.*;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderLinkControllerMachine extends MultiblockControllerMachine implements IMachineModifyDrops {
    @Persisted @Getter
    private final UUID uuid;

    @Persisted
    private final List<DirectionalGlobalPos> linkedCoverPositions = new ArrayList<>();

    private final IFluidTransfer[] channelFluidTransfers;

    private final int tier;
    private final List<EnderFluidLinkCover> loadedLinkedCovers = new ArrayList<>();

    public EnderLinkControllerMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);

        this.tier = tier;
        this.uuid = UUID.randomUUID();

        channelFluidTransfers = createChannelFluidTransfers();
    }

    private IFluidTransfer[] createChannelFluidTransfers() {
        return IntStream.range(1, getMaxChannels())
                .mapToObj(ChannelFluidTransfer::new)
                .toArray(IFluidTransfer[]::new);
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        // TODO add stored controller linking cards here
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // TODO attach this instance to all currently loaded linked covers (by stored positions)
    }

    @Override
    public void onUnload() {
        super.onUnload();
        // TODO detach this instance from all currently loaded linked covers
    }

    //////////////////////////////////////
    //*****    CONTROLLER LOGIC    *****//
    //////////////////////////////////////

    public int getMaxChannels() {
        return 8; // TODO adapt to tier, as well as linked controllers
    }

    public int getMaxRange() {
        return 32; // TODO adapt to tier
    }

    @Nullable
    public IFluidTransfer getVirtualFluidTransfer(int channel) {
        if (channel < 0 || channel >= channelFluidTransfers.length)
            return null;

        return channelFluidTransfers[channel - 1];
    }

    //////////////////////////////////////
    //********     Structure    ********//
    //////////////////////////////////////
    public static Block getCasingState(int tier) {
        return switch (tier) {
            case MV -> GTBlocks.CASING_STEEL_SOLID.get();
            case HV -> GTBlocks.CASING_STAINLESS_CLEAN.get();
            case EV -> GTBlocks.CASING_TITANIUM_STABLE.get();
            case IV -> GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
            case LuV -> GTBlocks.CASING_HSSE_STURDY.get();
            default -> throw new IllegalStateException("Unexpected value: " + tier);
        };
    }

    public static ResourceLocation getBaseTexture(int tier) {
        return switch (tier) {
            case MV -> GTCEu.id("block/casings/solid/machine_casing_solid_steel");
            case HV -> GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel");
            case EV -> GTCEu.id("block/casings/solid/machine_casing_stable_titanium");
            case IV -> GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel");
            case LuV -> GTCEu.id("block/casings/solid/machine_casing_study_hsse");
            default -> throw new NotImplementedException("Not yet implemented"); // TODO
        };
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderLinkControllerMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /////////////////////////////////////
    //*****   VIRTUAL TRANSFERS   *****//
    /////////////////////////////////////

    private class ChannelFluidTransfer implements IFluidTransfer {
        // TODO extract some sort of common MultiFluidTransferProxy and implement this class

        private final int channel;

        private ChannelFluidTransfer(int channel) {
            this.channel = channel;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return null;
        }

        @Override
        public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {

        }

        @Override
        public long getTankCapacity(int tank) {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @NotNull
        @Override
        public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return null;
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return null;
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {

        }
    }
}

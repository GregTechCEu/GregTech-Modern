package com.gregtechceu.gtceu.common.cover.ender_link;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.EnderLinkCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkCardReader;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkChannel;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Stream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends PumpCover implements EnderLinkCover {
    @Nullable
    private EnderLinkControllerMachine controller;

    @Persisted @DescSynced @Getter
    private int channel = 1;

    @Persisted @DescSynced
    private final EnderLinkCardReader cardReader;


    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, GTValues.HV); // TODO support multiple tiers

        cardReader = new EnderLinkCardReader(c -> setController(c.orElse(null)));
    }

    //////////////////////////////////////
    //********     OVERRIDES    ********//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        linkToController();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unlinkFromController();
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        linkToController();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        unlinkFromController();
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        return Stream.of(
                super.getAdditionalDrops(),
                cardReader.getDroppedItems()
        ).flatMap(List::stream).toList();
    }

    @Override
    protected boolean isSubscriptionActive() {
        // Normally, the adjacent transfer would be considered here, but for an ender link cover, there is no way
        // to start the subscription again after the adjacent transfer would have been null (normally that would be
        // done on neighboring block updates)
        return isWorkingEnabled();
    }

    @Override
    protected @Nullable IFluidTransfer getAdjacentFluidTransfer() {
        if (controller == null)
            return null;

        EnderLinkChannel channel = controller.getNetwork().getChannel(this.channel);
        return (channel != null) ? channel.getFluidTransferWrapper(this.io.oppositeDirection()) : null;
    }

    @Override
    public void setIo(IO io) {
        super.setIo(io);

        if (controller != null)
            controller.updateCover(this);
    }

    //////////////////////////////////////
    //**********   BEHAVIOR   **********//
    //////////////////////////////////////

    private void setController(@Nullable EnderLinkControllerMachine controller) {
        unlinkFromController();
        this.controller = controller;
        linkToController();

        subscriptionHandler.updateSubscription();
    }

    private void linkToController() {
        if (this.controller != null)
            this.controller.linkCover(this);
    }

    private void unlinkFromController() {
        if (this.controller != null)
            this.controller.unlinkCover(this);

        this.controller = null;
    }

    public void setChannel(int channel) {
        int maxChannels = controller != null ? controller.getMaxChannels() : 1;
        this.channel = Mth.clamp(channel, 1, maxChannels);

        if (controller != null) {
            controller.updateCover(this);
        }
    }


    @Override
    public void unlinkController(EnderLinkControllerMachine controller) {
        if (this.controller == controller)
            this.controller = null;
    }

    @Override
    public EnderLinkChannel.TransferType getTransferType() {
        return EnderLinkChannel.TransferType.FLUID;
    }

    @Nullable
    @Override
    public IFluidTransfer getFluidTransfer() {
        return getOwnFluidTransfer();
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.ender_link.fluid.label";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(cardReader.createUI(131, 46));
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

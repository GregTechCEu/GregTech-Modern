package com.gregtechceu.gtceu.common.pipelike.enderlink;

import com.gregtechceu.gtceu.api.cover.IEnderLinkCover;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderLinkNetwork {
    private final EnderLinkChannel[] channels;
    private final int channelCount;

    public EnderLinkNetwork(IntSupplier currentTickSupplier, int channelCount) {
        this.channelCount = channelCount;
        this.channels = IntStream.range(1, channelCount)
                .mapToObj(id -> new EnderLinkChannel(currentTickSupplier, id))
                .toArray(EnderLinkChannel[]::new);
    }

    public @Nullable EnderLinkChannel getChannel(int channelID) {
        if (channelID < 0 || channelID >= channelCount)
            return null;

        return channels[channelID - 1];
    }

    public List<EnderLinkChannel> getChannelsByCurrentTransferType(EnderLinkChannel.TransferType type) {
        return Arrays.stream(channels).filter(channel -> channel.getCurrentTransferType() == type).toList();
    }

    public void registerCover(IEnderLinkCover cover) {
        EnderLinkChannel channel = getChannel(cover.getChannel());

        if (channel == null)
            return;

        switch (cover.getTransferType()) {
            case ITEM -> channel.addItemTransfer(cover.getIo(), cover, cover.getItemTransfer());
            case FLUID -> channel.addFluidTransfer(cover.getIo(), cover, cover.getFluidTransfer());
            case CONTROLLER -> {
            }
        }
    }

    public void unregisterCover(IEnderLinkCover cover) {
        EnderLinkChannel channel = getChannel(cover.getChannel());

        if (channel == null)
            return;

        switch (cover.getTransferType()) {
            case ITEM -> channel.removeItemTransfer(cover);
            case FLUID -> channel.removeFluidTransfer(cover);
            case CONTROLLER -> {
            }
        }
    }

}

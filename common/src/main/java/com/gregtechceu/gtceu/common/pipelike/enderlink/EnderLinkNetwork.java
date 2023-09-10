package com.gregtechceu.gtceu.common.pipelike.enderlink;

import com.gregtechceu.gtceu.api.cover.IEnderLinkCover;
import com.gregtechceu.gtceu.api.pipenet.enderlink.ITransferType;
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

    public <T> List<EnderLinkChannel> getChannelsByCurrentTransferType(ITransferType<T> type) {
        return Arrays.stream(channels).filter(channel -> channel.getCurrentTransferType() == type).toList();
    }

    public <T> void registerCover(IEnderLinkCover<T> cover) {
        EnderLinkChannel channel = getChannel(cover.getChannel());

        if (channel == null)
            return;

        channel.addTransfer(cover.getTransferType(), cover.getIo(), cover, cover.getTransfer());
    }

    public <T> void unregisterCover(IEnderLinkCover<T> cover) {
        EnderLinkChannel channel = getChannel(cover.getChannel());

        if (channel == null)
            return;

        channel.removeTransfer(cover.getTransferType(), cover);
    }

    public void transferAll() {
        for (EnderLinkChannel channel : channels) {
            channel.transferAll();
        }
    }
}

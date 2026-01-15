package com.gregtechceu.gtceu.common.cover;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record MonitorCoverConfig(
                                 List<String> lines,
                                 List<String> args,
                                 int updateInterval) {

    public List<String> getLines() {
        return new ArrayList(lines);
    }

    public List<String> getArgs() {
        return new ArrayList(args);
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public static final Codec<MonitorCoverConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.listOf().fieldOf("lines").forGetter(MonitorCoverConfig::lines),
            Codec.STRING.listOf().fieldOf("args").forGetter(MonitorCoverConfig::args),
            Codec.INT.fieldOf("updateInterval").forGetter(MonitorCoverConfig::updateInterval))
            .apply(i, MonitorCoverConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MonitorCoverConfig> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MonitorCoverConfig::lines,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MonitorCoverConfig::args,
            ByteBufCodecs.INT, MonitorCoverConfig::updateInterval,
            MonitorCoverConfig::new);
}

package com.gregtechceu.gtceu.common.item.datacomponents;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.With;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public record ComputerMonitorConfig(@With List<String> lines, @With List<String> args, @With int updateInterval)
        implements TooltipProvider {

    // spotless:off
    public static final Codec<ComputerMonitorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("lines").forGetter(ComputerMonitorConfig::lines),
            Codec.STRING.listOf().fieldOf("arguments").forGetter(ComputerMonitorConfig::args),
            ExtraCodecs.POSITIVE_INT.fieldOf("update_interval").forGetter(ComputerMonitorConfig::updateInterval)
    ).apply(instance, ComputerMonitorConfig::new));
    public static final StreamCodec<ByteBuf, ComputerMonitorConfig> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ComputerMonitorConfig::lines,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ComputerMonitorConfig::args,
            ByteBufCodecs.VAR_INT, ComputerMonitorConfig::updateInterval,
            ComputerMonitorConfig::new
    );
    // spotless:on

    public static final ComputerMonitorConfig EMPTY = new ComputerMonitorConfig(Collections.emptyList(),
            Collections.emptyList(), 100);

    public ComputerMonitorConfig {
        lines = Collections.unmodifiableList(lines);
        args = Collections.unmodifiableList(args);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        tooltipAdder.accept(Component.translatable("gtceu.tooltip.computer_monitor_config"));
    }
}

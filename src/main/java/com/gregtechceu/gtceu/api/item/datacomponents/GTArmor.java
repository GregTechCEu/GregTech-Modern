package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.utils.StreamCodecUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

public record GTArmor(
        boolean enabled,
        boolean hover,
        boolean canShare,
        boolean nightVision,
        boolean boostedJump,
        boolean onGround,
        byte toggleTimer,
        short burnTimer,
        int nightVisionTimer,
        byte runningTimer,
        byte boostedJumpTimer,
        byte consumerTicks) {

    public static final Codec<GTArmor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(GTArmor::enabled),
            Codec.BOOL.fieldOf("hover").forGetter(GTArmor::hover),
            Codec.BOOL.fieldOf("can_share").forGetter(GTArmor::canShare),
            Codec.BOOL.fieldOf("night_vision").forGetter(GTArmor::nightVision),
            Codec.BOOL.fieldOf("boosted_jump").forGetter(GTArmor::boostedJump),
            Codec.BOOL.fieldOf("on_ground").forGetter(GTArmor::onGround),
            Codec.BYTE.fieldOf("toggle_timer").forGetter(GTArmor::toggleTimer),
            Codec.SHORT.fieldOf("burn_timer").forGetter(GTArmor::burnTimer),
            Codec.INT.fieldOf("night_vision_timer").forGetter(GTArmor::nightVisionTimer),
            Codec.BYTE.fieldOf("running_timer").forGetter(GTArmor::runningTimer),
            Codec.BYTE.fieldOf("boosted_jump_timer").forGetter(GTArmor::boostedJumpTimer),
            Codec.BYTE.fieldOf("consumer_ticks").forGetter(GTArmor::consumerTicks)).apply(instance, GTArmor::new));
    public static final StreamCodec<ByteBuf, GTArmor> STREAM_CODEC = StreamCodecUtils.composite(
            ByteBufCodecs.BOOL, GTArmor::enabled,
            ByteBufCodecs.BOOL, GTArmor::hover,
            ByteBufCodecs.BOOL, GTArmor::canShare,
            ByteBufCodecs.BOOL, GTArmor::nightVision,
            ByteBufCodecs.BOOL, GTArmor::boostedJump,
            ByteBufCodecs.BOOL, GTArmor::onGround,
            ByteBufCodecs.BYTE, GTArmor::toggleTimer,
            ByteBufCodecs.SHORT, GTArmor::burnTimer,
            ByteBufCodecs.INT, GTArmor::nightVisionTimer,
            ByteBufCodecs.BYTE, GTArmor::runningTimer,
            ByteBufCodecs.BYTE, GTArmor::boostedJumpTimer,
            ByteBufCodecs.BYTE, GTArmor::consumerTicks,
            GTArmor::new);

    public GTArmor() {
        this(false, false, false, false, false, false, (byte) 0, (short) 0, 0, (byte) 0, (byte) 0, (byte) 0);
    }

    public GTArmor setEnabled(boolean enabled) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setHover(boolean hover) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setCanShare(boolean canShare) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setNightVision(boolean nightVision) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setBoostedJump(boolean boostedJump) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setOnGround(boolean onGround) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setToggleTimer(byte toggleTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setBurnTimer(short burnTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setNightVisionTimer(int nightVisionTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setRunningTimer(byte runningTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setBoostedJumpTimer(byte boostedJumpTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setConsumerTicks(byte consumerTicks) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, toggleTimer, burnTimer, nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }
}

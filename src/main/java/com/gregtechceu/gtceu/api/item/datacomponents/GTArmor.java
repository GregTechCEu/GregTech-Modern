package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.utils.codec.StreamCodecUtils;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public record GTArmor(
                      boolean enabled,
                      boolean hover,
                      boolean canShare,
                      boolean nightVision,
                      boolean boostedJump,
                      boolean onGround,
                      boolean stepAssist,
                      byte toggleTimer,
                      short burnTimer,
                      int nightVisionTimer,
                      byte runningTimer,
                      byte boostedJumpTimer,
                      byte consumerTicks) {

    // spotless:off
    public static final Codec<GTArmor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.orElse(false).fieldOf("enabled").forGetter(GTArmor::enabled),
            Codec.BOOL.orElse(false).fieldOf("hover").forGetter(GTArmor::hover),
            Codec.BOOL.orElse(false).fieldOf("can_share").forGetter(GTArmor::canShare),
            Codec.BOOL.orElse(false).fieldOf("night_vision").forGetter(GTArmor::nightVision),
            Codec.BOOL.orElse(false).fieldOf("boosted_jump").forGetter(GTArmor::boostedJump),
            Codec.BOOL.orElse(false).fieldOf("on_ground").forGetter(GTArmor::onGround),
            Codec.BOOL.orElse(false).fieldOf("step_assist").forGetter(GTArmor::stepAssist),
            Codec.BYTE.orElse((byte) 0).fieldOf("toggle_timer").forGetter(GTArmor::toggleTimer),
            Codec.SHORT.orElse((short) 0).fieldOf("burn_timer").forGetter(GTArmor::burnTimer),
            Codec.INT.orElse(ArmorUtils.NIGHTVISION_DURATION).fieldOf("night_vision_timer").forGetter(GTArmor::nightVisionTimer),
            Codec.BYTE.orElse((byte) 0).fieldOf("running_timer").forGetter(GTArmor::runningTimer),
            Codec.BYTE.orElse((byte) 0).fieldOf("boosted_jump_timer").forGetter(GTArmor::boostedJumpTimer),
            Codec.BYTE.orElse((byte) 0).fieldOf("consumer_ticks").forGetter(GTArmor::consumerTicks)).apply(instance, GTArmor::new));
    // spotless:on
    public static final StreamCodec<ByteBuf, GTArmor> STREAM_CODEC = StreamCodecUtils.composite(
            ByteBufCodecs.BOOL, GTArmor::enabled,
            ByteBufCodecs.BOOL, GTArmor::hover,
            ByteBufCodecs.BOOL, GTArmor::canShare,
            ByteBufCodecs.BOOL, GTArmor::nightVision,
            ByteBufCodecs.BOOL, GTArmor::boostedJump,
            ByteBufCodecs.BOOL, GTArmor::onGround,
            ByteBufCodecs.BOOL, GTArmor::stepAssist,
            ByteBufCodecs.BYTE, GTArmor::toggleTimer,
            ByteBufCodecs.SHORT, GTArmor::burnTimer,
            ByteBufCodecs.VAR_INT, GTArmor::nightVisionTimer,
            ByteBufCodecs.BYTE, GTArmor::runningTimer,
            ByteBufCodecs.BYTE, GTArmor::boostedJumpTimer,
            ByteBufCodecs.BYTE, GTArmor::consumerTicks,
            GTArmor::new);

    public static final GTArmor EMPTY = new GTArmor();

    private GTArmor() {
        this(false, false, false, false, false, false, false, (byte) 0, (short) 0, 0, (byte) 0, (byte) 0, (byte) 0);
    }

    public GTArmor setEnabled(boolean enabled) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setHover(boolean hover) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setCanShare(boolean canShare) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setNightVision(boolean nightVision) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setBoostedJump(boolean boostedJump) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setOnGround(boolean onGround) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setStepAssist(boolean stepAssist) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setToggleTimer(byte toggleTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setBurnTimer(short burnTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setNightVisionTimer(int nightVisionTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setRunningTimer(byte runningTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setBoostedJumpTimer(byte boostedJumpTimer) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public GTArmor setConsumerTicks(byte consumerTicks) {
        return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    public Mutable toMutable() {
        return new Mutable(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                burnTimer,
                nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
    }

    @Accessors(chain = true, fluent = true)
    @AllArgsConstructor
    public static class Mutable {

        @Getter
        @Setter
        private boolean enabled;
        @Getter
        @Setter
        private boolean hover;
        @Getter
        @Setter
        private boolean canShare;
        @Getter
        @Setter
        private boolean nightVision;
        @Getter
        @Setter
        private boolean boostedJump;
        @Getter
        @Setter
        private boolean stepAssist;
        @Getter
        @Setter
        private boolean onGround;
        @Getter
        @Setter
        private byte toggleTimer;
        @Getter
        @Setter
        private short burnTimer;
        @Getter
        @Setter
        private int nightVisionTimer;
        @Getter
        @Setter
        private byte runningTimer;
        @Getter
        @Setter
        private byte boostedJumpTimer;
        @Getter
        @Setter
        private byte consumerTicks;

        public GTArmor toImmutable() {
            return new GTArmor(enabled, hover, canShare, nightVision, boostedJump, onGround, stepAssist, toggleTimer,
                    burnTimer,
                    nightVisionTimer, runningTimer, boostedJumpTimer, consumerTicks);
        }
    }
}

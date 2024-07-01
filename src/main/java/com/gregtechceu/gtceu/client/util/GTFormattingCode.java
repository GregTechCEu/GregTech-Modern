package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.gregtechceu.gtceu.api.GTValues.ALL_COLORS;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.RED;
import static net.minecraft.ChatFormatting.WHITE;
import static net.minecraft.ChatFormatting.YELLOW;

public enum GTFormattingCode implements StringRepresentable {

    /** Oscillates through all colors, changing each tick */
    RAINBOW_FAST(1, ALL_COLORS),
    /** Oscillates through all colors, changing every 5 ticks */
    RAINBOW(5, ALL_COLORS),
    /** Oscillates through all colors, changing every 25 ticks */
    RAINBOW_SLOW(25, ALL_COLORS),
    /** Switches between AQUA and WHITE, changing every 5 ticks */
    BLINKING_CYAN(5, AQUA, WHITE),
    /** Switches between RED and WHITE, changing every 5 ticks */
    BLINKING_RED(5, RED, WHITE),
    /** Switches between GOLD and YELLOW, changing every 25 ticks */
    BLINKING_ORANGE(25, GOLD, YELLOW),
    /** Switches between GRAY and DARK_GRAY, changing every 25 ticks */
    BLINKING_GRAY(25, GRAY, DARK_GRAY);

    private final int rate;
    private final ChatFormatting[] codes;
    private int index = 0;

    /**
     * Creates a Formatting Code which can oscillate through a number of different formatting codes at a specified rate.
     *
     * @param rate  The number of ticks this should wait before changing to the next code. MUST be greater than zero.
     * @param codes The codes, in order, that this formatting code should oscillate through. MUST be at least 2.
     */
    GTFormattingCode(int rate, ChatFormatting... codes) {
        if (rate <= 0) {
            throw new IllegalArgumentException(
                    "Could not create GT Formatting Code with rate %s, must be greater than zero!".formatted(rate));
        }
        if (codes == null || codes.length <= 1) {
            throw new IllegalArgumentException(
                    "Could not create GT Formatting Code with codes %s, must have length greater than one!"
                            .formatted(Arrays.toString(codes)));
        }
        this.rate = rate;
        this.codes = codes;
    }

    public void updateIndex() {
        if (GTValues.CLIENT_TIME % rate == 0) {
            if (index + 1 >= codes.length) index = 0;
            else index++;
        }
    }

    public ChatFormatting getCurrent() {
        return codes[index];
    }

    @Override
    public String toString() {
        return codes[index].toString();
    }

    @Override
    public @NotNull String getSerializedName() {
        return codes[index].getSerializedName();
    }

    public static void onClientTick() {
        for (GTFormattingCode code : values()) {
            code.updateIndex();
        }
    }
}

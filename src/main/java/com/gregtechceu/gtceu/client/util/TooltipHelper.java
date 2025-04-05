package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minecraft.ChatFormatting.*;

public class TooltipHelper {

    /**
     * Returns a continually changing rainbow TextColor
     * 
     * @param speed degrees of hue change per tick
     * @return Rainbow TextColor
     */
    public static TextColor rainbowColor(float speed) {
        return TextColor.fromRgb(GradientUtil.toRGB((GTValues.CLIENT_TIME & ((1 << 20) - 1)) * speed, 95f, 60f));
    }

    public static final UnaryOperator<Style> RAINBOW_HSL = style -> style.withColor(rainbowColor(2.5f));
    public static final UnaryOperator<Style> RAINBOW_HSL_SLOW = style -> style.withColor(rainbowColor(1.25f));
    public static final UnaryOperator<Style> RAINBOW_HSL_FAST = style -> style.withColor(rainbowColor(3.75f));

    private static final List<GTFormattingCode> CODES = new ArrayList<>();

    /** Array of TextFormatting codes that are used to create a rainbow effect */
    private static final ChatFormatting[] ALL_COLORS = new ChatFormatting[] {
            RED, GOLD, YELLOW, GREEN, AQUA, DARK_AQUA, DARK_BLUE, BLUE, DARK_PURPLE, LIGHT_PURPLE
    };

    /** Oscillates through all colors, changing each tick */
    public static final GTFormattingCode RAINBOW_FAST = createNewCode(1, ALL_COLORS);
    /** Oscillates through all colors, changing every 5 ticks */
    public static final GTFormattingCode RAINBOW = createNewCode(5, ALL_COLORS);
    /** Oscillates through all colors, changing every 25 ticks */
    public static final GTFormattingCode RAINBOW_SLOW = createNewCode(25, ALL_COLORS);
    /** Switches between AQUA and WHITE, changing every 5 ticks */
    public static final GTFormattingCode BLINKING_CYAN = createNewCode(5, AQUA, WHITE);
    /** Switches between RED and WHITE, changing every 5 ticks */
    public static final GTFormattingCode BLINKING_RED = createNewCode(5, RED, WHITE);
    /** Switches between GOLD and YELLOW, changing every 25 ticks */
    public static final GTFormattingCode BLINKING_ORANGE = createNewCode(25, GOLD, YELLOW);
    /** Switches between GRAY and DARK_GRAY, changing every 25 ticks */
    public static final GTFormattingCode BLINKING_GRAY = createNewCode(25, GRAY, DARK_GRAY);

    /**
     * Creates a Formatting Code which can oscillate through a number of different formatting codes at a specified rate.
     *
     * @param rate  The number of ticks this should wait before changing to the next code. MUST be greater than zero.
     * @param codes The codes, in order, that this formatting code should oscillate through. MUST be at least 2.
     */
    public static GTFormattingCode createNewCode(int rate, ChatFormatting... codes) {
        if (rate <= 0) {
            GTCEu.LOGGER.error("Could not create GT Formatting Code with rate {}, must be greater than zero!", rate);
            return null;
        }
        if (codes == null || codes.length <= 1) {
            GTCEu.LOGGER.error("Could not create GT Formatting Code with codes {}, must have length greater than one!",
                    Arrays.toString(codes));
            return null;
        }
        GTFormattingCode code = new GTFormattingCode(rate, codes);
        CODES.add(code);
        return code;
    }

    public static void onClientTick() {
        CODES.forEach(GTFormattingCode::updateIndex);
    }

    public static class GTFormattingCode {

        private final int rate;
        private final ChatFormatting[] codes;
        private int index = 0;

        private GTFormattingCode(int rate, ChatFormatting... codes) {
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
    }
}

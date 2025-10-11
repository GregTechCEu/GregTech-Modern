package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public class AnimatedText extends StyledText {

    private MutableComponent full;
    private String fullString;
    private String currentString = "";
    private int currentIndex;
    private int speed = 40; // ms per char
    private long timeLastDraw;
    private boolean forward = true;

    private boolean isAnimating = false;

    public AnimatedText(IKey key) {
        super(key);
    }

    @Override
    public MutableComponent get() {
        return Component.literal(this.currentString);
    }

    public void reset() {
        this.full = null;
    }

    private void advance() {
        if (!this.isAnimating || (this.forward && this.currentIndex >= this.fullString.length()) ||
                (!this.forward && this.currentIndex < 0))
            return;
        long time = Util.getMillis();
        int amount = (int) ((time - this.timeLastDraw) / this.speed);
        if (amount == 0) return;
        if (this.forward) {
            int max = Math.min(this.fullString.length() - 1, this.currentIndex + amount);
            this.currentIndex = Math.max(this.currentIndex, 0);
            for (int i = this.currentIndex; i < max; i++) {
                char c = this.fullString.charAt(i);
                if (c == ' ') {
                    max = Math.min(this.fullString.length() - 1, max + 1);
                }
                // noinspection StringConcatenationInLoop
                this.currentString += c;
            }
            this.currentIndex = max;
        } else {
            int min = Math.max(0, this.currentIndex - amount);
            this.currentIndex = Math.min(this.currentIndex, this.currentString.length() - 1);
            for (int i = this.currentIndex; i >= min; i--) {
                char c = this.fullString.charAt(i);
                if (c == ' ') {
                    min = Math.max(0, min - 1);
                }
                this.currentString = this.currentString.substring(0, i);
            }
            this.currentIndex = min;
        }
        this.timeLastDraw += (long) amount * this.speed;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.full == null || !this.full.equals(super.get())) {
            if (this.isAnimating) {
                this.full = super.get();
                this.fullString = this.full.getString();
                this.currentString = this.forward ? "" : this.fullString;
                this.currentIndex = this.forward ? 0 : this.fullString.length() - 1;
                this.timeLastDraw = Util.getMillis();
            } else {
                this.currentString = this.forward ? "" : this.fullString;
            }
        }
        advance();
        if (this.currentString.isEmpty()) return;
        super.draw(context, x, y, width, height, widgetTheme);
    }

    public AnimatedText startAnimation() {
        this.isAnimating = true;
        return this;
    }

    public AnimatedText stopAnimation() {
        this.isAnimating = false;
        return this;
    }

    public AnimatedText forward(boolean forward) {
        this.forward = forward;
        return this;
    }

    @Override
    public AnimatedText style(ChatFormatting formatting) {
        return (AnimatedText) super.style(formatting);
    }

    @Override
    public AnimatedText alignment(Alignment alignment) {
        return (AnimatedText) super.alignment(alignment);
    }

    @Override
    public AnimatedText color(int color) {
        return color(() -> color);
    }

    @Override
    public AnimatedText color(@Nullable IntSupplier color) {
        return (AnimatedText) super.color(color);
    }

    @Override
    public AnimatedText scale(float scale) {
        return (AnimatedText) super.scale(scale);
    }

    @Override
    public AnimatedText shadow(@Nullable Boolean shadow) {
        return (AnimatedText) super.shadow(shadow);
    }

    /**
     * How fast the characters appear
     *
     * @param speed in ms per character
     */
    public AnimatedText speed(int speed) {
        this.speed = speed;
        return this;
    }
}

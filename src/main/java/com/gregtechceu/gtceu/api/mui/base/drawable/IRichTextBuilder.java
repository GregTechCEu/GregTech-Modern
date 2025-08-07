package com.gregtechceu.gtceu.api.mui.base.drawable;

import com.gregtechceu.gtceu.api.mui.drawable.text.Spacer;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;

import net.minecraft.network.chat.Component;

public interface IRichTextBuilder<T extends IRichTextBuilder<T>> {

    T getThis();

    IRichTextBuilder<?> getRichText();

    default T add(Component c) {
        getRichText().add(c);
        return getThis();
    }

    default T add(String s) {
        getRichText().add(s);
        return getThis();
    }

    default T add(IDrawable drawable) {
        getRichText().add(drawable);
        return getThis();
    }

    default T addLine(String s) {
        getRichText().add(s).newLine();
        return getThis();
    }

    default T addLine(ITextLine line) {
        getRichText().addLine(line);
        return getThis();
    }

    default T addLine(IDrawable line) {
        getRichText().add(line).newLine();
        return getThis();
    }

    default T newLine() {
        return add(IKey.LINE_FEED);
    }

    default T space() {
        return add(IKey.SPACE);
    }

    default T spaceLine(int pixelSpace) {
        return addLine(Spacer.of(pixelSpace));
    }

    default T spaceLine() {
        return addLine(Spacer.SPACER_2PX);
    }

    default T emptyLine() {
        return addLine(Spacer.LINE_SPACER);
    }

    default T addElements(Iterable<IDrawable> drawables) {
        for (IDrawable drawable : drawables) {
            getRichText().add(drawable);
        }
        return getThis();
    }

    default T addDrawableLines(Iterable<IDrawable> drawables) {
        for (IDrawable drawable : drawables) {
            getRichText().add(drawable).newLine();
        }
        return getThis();
    }

    default T addStringLines(Iterable<String> drawables) {
        for (String drawable : drawables) {
            getRichText().add(drawable).newLine();
        }
        return getThis();
    }

    default T clearText() {
        getRichText().clearText();
        return getThis();
    }

    default T alignment(Alignment alignment) {
        getRichText().alignment(alignment);
        return getThis();
    }

    default T textColor(int color) {
        getRichText().textColor(color);
        return getThis();
    }

    default T scale(float scale) {
        getRichText().scale(scale);
        return getThis();
    }

    default T textShadow(boolean shadow) {
        getRichText().textShadow(shadow);
        return getThis();
    }
}

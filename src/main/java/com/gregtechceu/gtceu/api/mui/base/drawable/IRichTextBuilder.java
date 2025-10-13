package com.gregtechceu.gtceu.api.mui.base.drawable;

import com.gregtechceu.gtceu.api.mui.drawable.text.Spacer;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public interface IRichTextBuilder<T extends IRichTextBuilder<T>> {

    T getThis();

    IRichTextBuilder<?> getRichText();

    /**
     * Adds a component to the current line
     *
     * @param c component to add
     * @return this
     */
    default T add(FormattedText c) {
        getRichText().add(c);
        return getThis();
    }

    /**
     * Adds a string to the current line
     *
     * @param s string to add
     * @return this
     */
    default T add(String s) {
        getRichText().add(s);
        return getThis();
    }

    /**
     * Adds a drawable to the current line. If the drawable is not a {@link IIcon} it will convert to one with
     * {@link IDrawable#asIcon()}.
     * If that icon then has no default height (<=0) then it is set to the default text height (9 pixel). If the width
     * of the icon is not set, then the width of the widest tooltip line is used.
     *
     * @param drawable drawable to add.
     * @return this
     */
    default T add(IDrawable drawable) {
        getRichText().add(drawable);
        return getThis();
    }

    /**
     * Adds a vanilla {@link TooltipComponent} to the current line.
     * The tooltip component will always be converted into a {@link IIcon} regardless of what it is and drawn inline
     * with the other components.
     * It's recommended to use {@link #addLine(TooltipComponent)} instead if you want to preserve how vanilla handles
     * this.
     *
     * @param tooltipComponent tooltip component to add.
     * @return this
     * @see #addLine(TooltipComponent)
     */
    default T add(TooltipComponent tooltipComponent) {
        getRichText().add(tooltipComponent);
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

    default T addLine(TooltipComponent tooltipComponent) {
        getRichText().add(tooltipComponent).newLine();
        return getThis();
    }

    /**
     * Adds a drawable to the current line and creates a new line.
     * Refer to {@link #add(IDrawable)} for additional information.
     *
     * @param line drawable to add.
     * @return this
     * @see #add(IDrawable)
     */
    default T addLine(IDrawable line) {
        getRichText().add(line).newLine();
        return getThis();
    }

    /**
     * Starts a new line. This is always preferred over {@code "\n"} or {@code IKey.str("\n")}, it reduces computation a
     * lot and maybe saves a tiny bit of memory.
     *
     * @return this
     */
    default T newLine() {
        return add(IKey.LINE_FEED);
    }

    /**
     * Adds a space character to the current line. This is rarely useful.
     *
     * @return this
     */
    default T space() {
        return add(IKey.SPACE);
    }

    /**
     * Adds a line with a given thickness in pixels. This will result in larger text line gap.
     *
     * @param pixelSpace thickness in pixel
     * @return this
     */
    default T spaceLine(int pixelSpace) {
        return addLine(Spacer.of(pixelSpace));
    }

    /**
     * Adds a two pixel thick empty line. This will result in larger text line gap.
     * This is useful for titles.
     *
     * @return this
     */
    default T spaceLine() {
        return addLine(Spacer.SPACER_2PX);
    }

    /**
     * Adds an empty line which is as tall as a normal text line.
     *
     * @return this
     */
    default T emptyLine() {
        return addLine(Spacer.LINE_SPACER);
    }

    /**
     * Adds multiple drawables to the current line.
     * Refer to {@link #add(IDrawable)} for additional information.
     *
     * @param drawables drawables to add.
     * @return this
     * @see #add(IDrawable)
     */
    default T addElements(Iterable<IDrawable> drawables) {
        for (IDrawable drawable : drawables) {
            getRichText().add(drawable);
        }
        return getThis();
    }

    /**
     * Adds each drawable and creates a new line after each.
     * Refer to {@link #add(IDrawable)} for additional information.
     *
     * @param drawables drawables to add.
     * @return this
     * @see #add(IDrawable)
     */
    default T addDrawableLines(Iterable<IDrawable> drawables) {
        for (IDrawable drawable : drawables) {
            getRichText().add(drawable).newLine();
        }
        return getThis();
    }

    /**
     * Adds each string and creates a new line after each.
     * Refer to {@link #add(String)} for additional information.
     *
     * @param strings strings to add.
     * @return this
     * @see #add(IDrawable)
     */
    default T addStringLines(Iterable<String> strings) {
        for (String string : strings) {
            getRichText().add(string).newLine();
        }
        return getThis();
    }

    /**
     * Finds the next element which contains the matching regex and put the cursor after it.
     * If none was found the cursor is at the end.
     *
     * @param regex regex to match strings for
     * @return this
     */
    default T moveCursorAfterElement(String regex) {
        return moveCursorAfterElement(Pattern.compile(regex));
    }

    /**
     * Finds the next element which contains the matching regex and put the cursor after it.
     * If none was found the cursor is at the end.
     *
     * @param regex regex to match strings for
     * @return this
     */
    default T moveCursorAfterElement(Pattern regex) {
        getRichText().moveCursorAfterElement(regex);
        return getThis();
    }

    /**
     * Finds the next element which contains the matching regex and replaces the whole element with the result of the
     * function.
     * The cursor is then placed after the new element. If the function returns {@code null}, then the element is
     * removed.
     * If no element is found nothing happens and the cursor stays in place.
     *
     * @param regex    regex to match strings for
     * @param function function to modify the found element
     * @return this
     */
    default T replace(String regex, UnaryOperator<IKey> function) {
        return replace(Pattern.compile(regex), function);
    }

    /**
     * Finds the next element which contains the matching regex and replaces the whole element with the result of the
     * function.
     * The cursor is then placed after the new element. If the function returns {@code null}, then the element is
     * removed.
     * If no element is found nothing happens and the cursor stays in place.
     *
     * @param regex    regex to match strings for
     * @param function function to modify the found element
     * @return this
     */
    default T replace(Pattern regex, UnaryOperator<IKey> function) {
        getRichText().replace(regex, function);
        return getThis();
    }

    /**
     * Moves the cursor to the very start.
     *
     * @return this
     */
    default T moveCursorToStart() {
        getRichText().moveCursorToStart();
        return getThis();
    }

    /**
     * Moves the cursor to the very end (default).
     *
     * @return this
     */
    default T moveCursorToEnd() {
        getRichText().moveCursorToEnd();
        return getThis();
    }

    /**
     * Moves the cursor a given number of elements forward. The cursor will be clamped at the end.
     *
     * @param by amount to move cursor by
     * @return this
     */
    default T moveCursorForward(int by) {
        getRichText().moveCursorForward(by);
        return getThis();
    }

    /**
     * Moves the cursor one element forward. The cursor will be clamped at the end.
     *
     * @return this
     */
    default T moveCursorForward() {
        return moveCursorForward(1);
    }

    /**
     * Moves the cursor a given number of elements backward. The cursor will be clamped at the start.
     *
     * @param by amount to move cursor by
     * @return this
     */
    default T moveCursorBackward(int by) {
        getRichText().moveCursorBackward(by);
        return getThis();
    }

    /**
     * Moves the cursor one element backward. The cursor will be clamped at the start.
     *
     * @return this
     */
    default T moveCursorBackward() {
        return moveCursorBackward(1);
    }

    /**
     * This finds the next element ending with a line break and moves the cursor after it. Note that if the line break
     * is somewhere in the middle of the element, that element will be ignored.
     *
     * @return this
     */
    default T moveCursorToNextLine() {
        getRichText().moveCursorToNextLine();
        return getThis();
    }

    /**
     * When the cursor is locked it will no longer move automatically when elements are added, but it can still be moved
     * manually with the move methods from above.
     *
     * @return this
     * @see #unlockCursor()
     */
    default T lockCursor() {
        getRichText().lockCursor();
        return getThis();
    }

    /**
     * When the cursor is locked it will no longer move automatically when elements are added, but it can still be moved
     * manually with the move methods from above.
     *
     * @return this
     * @see #lockCursor() ()
     */
    default T unlockCursor() {
        getRichText().unlockCursor();
        return getThis();
    }

    /**
     * Removes all text.
     *
     * @return this
     */

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

package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.drawable.ITextLine;
import com.gregtechceu.gtceu.api.mui.drawable.text.StyledText;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Helper interface with tooltip builder methods for widgets.
 *
 * @param <W> widget type
 */
public interface ITooltip<W extends ITooltip<W>> {

    /**
     * @return the current tooltip of this widget. Null if there is none
     */
    @Nullable
    RichTooltip getTooltip();

    /**
     * @return the current tooltip of this widget. Creates a new one if there is none
     */
    @NotNull
    RichTooltip tooltip();

    /**
     * Overwrites the current tooltip with the given one
     *
     * @param tooltip new tooltip
     * @return this
     */
    W tooltip(RichTooltip tooltip);

    /**
     * @return true if this widget has a tooltip
     */
    default boolean hasTooltip() {
        return getTooltip() != null && !getTooltip().isEmpty();
    }

    /**
     * @return this cast to the true widget type
     */
    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    /**
     * Helper method to call tooltip setters within a widget tree initialisation.
     * Only called once.
     *
     * @param tooltipConsumer tooltip function
     * @return this
     */
    default W tooltip(Consumer<RichTooltip> tooltipConsumer) {
        return tooltipStatic(tooltipConsumer);
    }

    /**
     * Helper method to call tooltip setters within a widget tree initialisation.
     * Only called once.
     *
     * @param tooltipConsumer tooltip function
     * @return this
     */
    default W tooltipStatic(Consumer<RichTooltip> tooltipConsumer) {
        tooltipConsumer.accept(tooltip());
        return getThis();
    }

    /**
     * Sets a tooltip builder. The builder will be called every time the tooltip is marked dirty.
     * Should be used for dynamic tooltips.
     *
     * @param tooltipBuilder tooltip function
     * @return this
     */
    default W tooltipBuilder(Consumer<RichTooltip> tooltipBuilder) {
        return tooltipDynamic(tooltipBuilder);
    }

    /**
     * Sets a tooltip builder. The builder will be called every time the tooltip is marked dirty.
     * Should be used for dynamic tooltips.
     *
     * @param tooltipBuilder tooltip function
     * @return this
     */
    default W tooltipDynamic(Consumer<RichTooltip> tooltipBuilder) {
        tooltip().tooltipBuilder(tooltipBuilder);
        return getThis();
    }

    /**
     * Sets a general tooltip position. The true position is calculated every frame.
     *
     * @param pos tooltip pos
     * @return this
     */
    default W tooltipPos(RichTooltip.Pos pos) {
        tooltip().pos(pos);
        return getThis();
    }

    /**
     * Sets a fixed tooltip position.
     *
     * @param x x pos
     * @param y y pos
     * @return this
     */
    default W tooltipPos(int x, int y) {
        tooltip().pos(x, y);
        return getThis();
    }

    /**
     * Sets an alignment. The alignment determines how the content is aligned in the tooltip.
     *
     * @param alignment alignment
     * @return this
     */
    default W tooltipAlignment(Alignment alignment) {
        tooltip().alignment(alignment);
        return getThis();
    }

    /**
     * Sets if the tooltip text should have shadow enabled by default.
     * Can be overridden with {@link StyledText} lines.
     *
     * @param textShadow true if text should have a shadow
     * @return this
     */
    default W tooltipTextShadow(boolean textShadow) {
        tooltip().textShadow(textShadow);
        return getThis();
    }

    /**
     * Sets a default tooltip text color. Can be overridden with text formatting.
     *
     * @param textColor text color
     * @return this
     */
    default W tooltipTextColor(int textColor) {
        tooltip().textColor(textColor);
        return getThis();
    }

    /**
     * Sets a tooltip scale. The whole tooltip with content will be drawn with this scale.
     *
     * @param scale scale
     * @return this
     */
    default W tooltipScale(float scale) {
        tooltip().scale(scale);
        return getThis();
    }

    /**
     * Sets a show up timer. This is the time in ticks needed for the cursor to hover this widget for the tooltip to
     * appear.
     *
     * @param showUpTimer show up timer in ticks
     * @return this
     */
    default W tooltipShowUpTimer(int showUpTimer) {
        tooltip().showUpTimer(showUpTimer);
        return getThis();
    }

    /**
     * Sets whether the tooltip should automatically update on every render tick. In most of the cases you don't need
     * this,
     * as ValueSyncHandler handles tooltip update for you when value is updated. However, if you don't handle
     * differently,
     * you either need to manually set change listener for the sync value, or set auto update to true.
     *
     * @param update true if the tooltip should automatically update
     * @return this
     */
    default W tooltipAutoUpdate(boolean update) {
        tooltip().autoUpdate(update);
        return getThis();
    }

    /**
     * Sets whether the tooltip has a title margin, which is 2px space between first and second line inserted by
     * default.
     *
     * @param hasTitleMargin true if the tooltip should have a title margin
     * @return this
     */
    default W tooltipHasTitleMargin(boolean hasTitleMargin) {
        // tooltip().setHasTitleMargin(hasTitleMargin);
        return getThis();
    }

    /**
     * Sets the line padding for the tooltip. 1px by default, and you can disable it by passing 0.
     *
     * @param linePadding line padding in px
     * @return this
     */
    default W tooltipLinePadding(int linePadding) {
        // tooltip().setLinePadding(linePadding);
        return getThis();
    }

    default W addTooltipElement(String s) {
        tooltip().add(s);
        return getThis();
    }

    default W addTooltipElement(IDrawable drawable) {
        tooltip().add(drawable);
        return getThis();
    }

    default W addTooltipLine(ITextLine line) {
        tooltip().addLine(line);
        return getThis();
    }

    /**
     * Adds any drawable as a new line. Inlining elements is currently not possible.
     *
     * @param drawable drawable element.
     * @return this
     */
    default W addTooltipLine(IDrawable drawable) {
        tooltip().add(drawable).newLine();
        return getThis();
    }

    /**
     * Helper method to add a simple string as a line. Adds multiple lines if string contains <code>\n</code>.
     *
     * @param line text line
     * @return this
     */
    default W addTooltipLine(String line) {
        return addTooltipLine(IKey.str(line));
    }

    /**
     * Helper method to add multiple drawable lines.
     *
     * @param lines collection of drawable elements
     * @return this
     */
    default W addTooltipDrawableLines(Iterable<IDrawable> lines) {
        tooltip().addDrawableLines(lines);
        return getThis();
    }

    /**
     * Helper method to add multiple text lines.
     *
     * @param lines lines of text
     * @return this
     */
    default W addTooltipStringLines(Iterable<String> lines) {
        tooltip().addStringLines(lines);
        return getThis();
    }
}

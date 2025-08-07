package com.gregtechceu.gtceu.api.mui.base.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that handles user interactions on {@link IWidget} objects.
 * These methods get called on the client
 */
public interface Interactable {

    /**
     * Called when this widget is pressed.
     *
     * @param mouseX the X coordinate of the mouse.
     * @param mouseY the Y coordinate of the mouse.
     * @param button mouse button that was pressed.
     * @return result that determines what happens to other widgets
     *         {@link #onMouseTapped(double, double, int)} is only called if this returns {@link Result#ACCEPT} or
     *         {@link Result#SUCCESS}
     */
    @NotNull
    default Result onMousePressed(double mouseX, double mouseY, int button) {
        return Result.ACCEPT;
    }

    /**
     * Called when a mouse button was released over this widget.
     *
     * @param mouseX the X coordinate of the mouse.
     * @param mouseY the Y coordinate of the mouse.
     * @param button mouse button that was released.
     * @return whether other widgets should get called to. If this returns false,
     *         {@link #onMouseTapped(double, double, int)} will NOT be called.
     */
    default boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    /**
     * Called when this widget was pressed and then released within a certain time frame.
     *
     * @param mouseX the X coordinate of the mouse.
     * @param mouseY the Y coordinate of the mouse.
     * @param button mouse button that was pressed.
     * @return result that determines if other widgets should get tapped to
     *         {@link Result#IGNORE IGNORE} and {@link Result#ACCEPT ACCEPT} will both "ignore" the result and
     *         {@link Result#STOP STOP} and {@link Result#SUCCESS SUCCESS} will both stop other widgets
     *         from getting tapped.
     */
    @NotNull
    default Result onMouseTapped(double mouseX, double mouseY, int button) {
        return Result.IGNORE;
    }

    /**
     * Called when a key over this widget is pressed.
     *
     * @param keyCode   key that was pressed.
     * @param scanCode  character that was pressed.
     * @param modifiers any modifiers that were used.
     * @return result that determines what happens to other widgets
     *         {@link #onKeyTapped(int, int, int)} is only called if this returns {@link Result#ACCEPT} or
     *         {@link Result#SUCCESS}
     */
    @NotNull
    default Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return Result.IGNORE;
    }

    /**
     * Called when a key was released over this widget.
     *
     * @param keyCode   key that was pressed.
     * @param scanCode  character that was pressed.
     * @param modifiers any modifiers that were used.
     * @return whether other widgets should get called too. If this returns false, {@link #onKeyTapped(int, int, int)}
     *         will NOT be called.
     */
    default boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Called when this widget was pressed and then released within a certain time frame.
     *
     * @param keyCode   key that was pressed.
     * @param scanCode  character that was pressed.
     * @param modifiers any modifiers that were used.
     * @return result that determines if other widgets should get tapped to
     *         {@link Result#IGNORE} and {@link Result#ACCEPT} will both "ignore" the result and {@link Result#STOP} and
     *         {@link Result#SUCCESS} will both stop other widgets from getting tapped.
     */
    @NotNull
    default Result onKeyTapped(int keyCode, int scanCode, int modifiers) {
        return Result.IGNORE;
    }

    /**
     * Called when a key over this widget is pressed.
     *
     * @param codePoint character that was typed
     * @param modifiers any modifiers that were used.
     * @return result that determines what happens to other widgets
     *         {@link #onKeyTapped(int, int, int)} is only called if this returns {@link Result#ACCEPT} or
     *         {@link Result#SUCCESS}
     */
    @NotNull
    default Result onCharTyped(char codePoint, int modifiers) {
        return Result.IGNORE;
    }

    /**
     * Called when this widget is focused or when the mouse is above this widget.
     * This method should return true if it can scroll at all and not if it scrolled right now.
     * If this scroll view scrolled to the end and this returns false, the scroll will get passed through another scroll
     * view below this.
     *
     * @param mouseX the X coordinate of the mouse.
     * @param mouseY the Y coordinate of the mouse.
     * @param delta  amount scrolled by (usually irrelevant)
     * @return true if this widget can be scrolled at all
     */
    default boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    /**
     * Called when this widget was clicked and mouse is now dragging.
     *
     * @param mouseX the X coordinate of the mouse.
     * @param mouseY the Y coordinate of the mouse.
     * @param button mouse button that drags
     * @param dragX
     * @param dragY
     */
    default void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {}

    /**
     * @return if left or right ctrl/cmd is pressed
     */
    @OnlyIn(Dist.CLIENT)
    static boolean hasControlDown() {
        return Screen.hasControlDown();
    }

    /**
     * @return if left or right shift is pressed
     */
    @OnlyIn(Dist.CLIENT)
    static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    /**
     * @return if alt or alt gr is pressed
     */
    @OnlyIn(Dist.CLIENT)
    static boolean hasAltDown() {
        return Screen.hasAltDown();
    }

    /**
     * @param key key id, see {@link InputConstants}
     * @return if the key is pressed
     */
    @OnlyIn(Dist.CLIENT)
    static boolean isKeyPressed(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }

    /**
     * Plays the default button click sound
     */
    @OnlyIn(Dist.CLIENT)
    static void playButtonClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    enum Result {

        /**
         * Nothing happens.
         */
        IGNORE(false, false),
        /**
         * Interaction is accepted, but other widgets will get checked.
         */
        ACCEPT(true, false),
        /**
         * Interaction is rejected and no other widgets will get checked.
         */
        STOP(false, true),
        /**
         * Interaction is accepted and no other widgets will get checked.
         */
        SUCCESS(true, true);

        public final boolean accepts;
        public final boolean stops;

        Result(boolean accepts, boolean stops) {
            this.accepts = accepts;
            this.stops = stops;
        }
    }
}

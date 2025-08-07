package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.api.mui.base.UIFactory;
import com.gregtechceu.gtceu.api.mui.base.XeiSettings;
import com.gregtechceu.gtceu.api.mui.factory.GuiData;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class UISettings {

    public static final double DEFAULT_INTERACT_RANGE = 8.0;

    private IntFunction<ModularContainerMenu> containerCreator;
    private Predicate<Player> canInteractWith;
    @Getter
    private final XeiSettings xeiSettings;

    public UISettings() {
        this(new XeiSettingsImpl());
    }

    public UISettings(XeiSettings xeiSettings) {
        this.xeiSettings = xeiSettings;
    }

    /**
     * A function for a custom {@link ModularContainerMenu} implementation. This overrides
     * {@link UIFactory#createContainer(int)}.
     *
     * @param containerCreator container creator function. Must return a new instance.
     */
    public void customContainer(IntFunction<ModularContainerMenu> containerCreator) {
        this.containerCreator = containerCreator;
    }

    /**
     * Overrides the default can interact check of {@link UIFactory#canInteractWith(Player, GuiData)}.
     *
     * @param canInteractWith function to test if a player can interact with the ui. This is called every tick while UI
     *                        is open. Once this
     *                        function returns false, the UI is immediately closed.
     */
    public void canInteractWith(Predicate<Player> canInteractWith) {
        this.canInteractWith = canInteractWith;
    }

    @ApiStatus.Internal
    public <D extends GuiData> void defaultCanInteractWith(UIFactory<D> factory, D guiData) {
        canInteractWith(player -> factory.canInteractWith(player, guiData));
    }

    public void canInteractWithinRange(Vec3 pos, double range) {
        canInteractWith(player -> player.distanceToSqr(pos) <= range * range);
    }

    public void canInteractWithinRange(BlockPos pos, double range) {
        canInteractWithinRange(pos.getCenter(), range);
    }

    public void canInteractWithinRange(PosGuiData guiData, double range) {
        canInteractWithinRange(guiData.getBlockPos(), range);
    }

    public void canInteractWithinDefaultRange(Vec3 pos) {
        canInteractWithinRange(pos, DEFAULT_INTERACT_RANGE);
    }

    public void canInteractWithinDefaultRange(BlockPos pos) {
        canInteractWithinRange(pos, DEFAULT_INTERACT_RANGE);
    }

    public void canInteractWithinDefaultRange(PosGuiData guiData) {
        canInteractWithinRange(guiData, DEFAULT_INTERACT_RANGE);
    }

    @ApiStatus.Internal
    public ModularContainerMenu createContainer(int containerId) {
        return containerCreator.apply(containerId);
    }

    public boolean hasContainer() {
        return containerCreator != null;
    }

    public boolean canPlayerInteractWithUI(Player player) {
        return canInteractWith == null || canInteractWith.test(player);
    }
}

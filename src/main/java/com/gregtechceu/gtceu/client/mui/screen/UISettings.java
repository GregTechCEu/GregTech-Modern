package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.UIFactory;
import com.gregtechceu.gtceu.api.mui.base.XeiSettings;
import com.gregtechceu.gtceu.api.mui.factory.GuiData;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class UISettings {

    public static final double DEFAULT_INTERACT_RANGE = 8.0;

    private IntFunction<ModularContainerMenu> containerCreator;
    @OnlyIn(Dist.CLIENT)
    private GuiCreator guiSupplier;
    private Predicate<Player> canInteractWith;
    @Getter
    private String theme;
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
     * A function for a custom {@link IMuiScreen} implementation. This overrides
     * {@link UIFactory#createScreenWrapper(ModularContainerMenu, ModularScreen)} (ModularContainer, ModularScreen)}.
     * Note that {@link IMuiScreen#getScreen()} has to be an
     * instance of {@link AbstractContainerScreen } otherwise an exception is thrown, when the UI opens.
     *
     * @param guiSupplier a supplier for a gui creator function. It has to be a double function because it crashes on
     *                    server otherwise.
     */
    public void customGui(Supplier<GuiCreator> guiSupplier) {
        if (GTCEu.isClientThread()) {
            this.guiSupplier = guiSupplier.get();
        }
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

    public void useTheme(String theme) {
        this.theme = theme;
    }

    @ApiStatus.Internal
    public ModularContainerMenu createContainer(int containerId) {
        return containerCreator.apply(containerId);
    }

    @ApiStatus.Internal
    @SideOnly(Side.CLIENT)
    public IMuiScreen createGui(ModularContainerMenu container, ModularScreen screen) {
        return guiSupplier.create(container, screen);
    }

    public boolean hasCustomContainer() {
        return containerCreator != null;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasCustomGui() {
        return guiSupplier != null;
    }

    public boolean canPlayerInteractWithUI(Player player) {
        return canInteractWith == null || canInteractWith.test(player);
    }

    public interface GuiCreator {

        IMuiScreen create(ModularContainerMenu container, ModularScreen screen);
    }
}

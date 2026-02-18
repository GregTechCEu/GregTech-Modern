package com.gregtechceu.gtceu.integration.jei;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;

import net.minecraft.client.gui.screens.Screen;

import lombok.Getter;
import mezz.jei.api.gui.handlers.IGuiProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * This needs to be an immutable class.
 */
public class GTJeiProperties implements IGuiProperties {

    @Getter
    private final Class<? extends Screen> screenClass;
    @Getter
    private final int guiLeft;
    @Getter
    private final int guiTop;
    @Getter
    private final int guiXSize;
    @Getter
    private final int guiYSize;
    @Getter
    private final int screenWidth;
    @Getter
    private final int screenHeight;

    public GTJeiProperties(IMuiScreen screen) {
        Area mainArea = screen.getScreen().getMainPanel().getArea();
        Area screenArea = screen.getScreen().getScreenArea();
        this.screenClass = screen.getWrappedScreen().getClass();
        this.guiLeft = mainArea.x;
        this.guiTop = mainArea.y;
        this.guiXSize = mainArea.width;
        this.guiYSize = mainArea.height;
        this.screenWidth = screenArea.width;
        this.screenHeight = screenArea.height;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("x", getGuiLeft())
                .append("y", getGuiTop())
                .append("width", getGuiXSize())
                .append("height", getGuiYSize())
                .append("screenWidth", getScreenWidth())
                .append("screenHeight", getScreenHeight())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GTJeiProperties that = (GTJeiProperties) o;
        return guiLeft == that.guiLeft && guiTop == that.guiTop && guiXSize == that.guiXSize &&
                guiYSize == that.guiYSize &&
                screenWidth == that.screenWidth && screenHeight == that.screenHeight &&
                Objects.equals(screenClass, that.screenClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screenClass, guiLeft, guiTop, guiXSize, guiYSize, screenWidth, screenHeight);
    }
}

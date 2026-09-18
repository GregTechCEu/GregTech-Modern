package brachy.modularui.integration.jei;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.gui.screens.Screen;

import lombok.Getter;
import lombok.experimental.Accessors;
import mezz.jei.api.gui.handlers.IGuiProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * This needs to be an immutable class, otherwise JEI shits itself.
 */
@Accessors(fluent = true)
public class ModularUIJeiProperties implements IGuiProperties {

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

    public ModularUIJeiProperties(IMuiScreen screen) {
        Area mainArea = screen.screen().getMainPanel().getArea();
        Area screenArea = screen.screen().getScreenArea();
        this.screenClass = screen.wrappedScreen().getClass();
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
                .append("x", guiLeft())
                .append("y", guiTop())
                .append("width", guiXSize())
                .append("height", guiYSize())
                .append("screenWidth", screenWidth())
                .append("screenHeight", screenHeight())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModularUIJeiProperties that = (ModularUIJeiProperties) o;
        return guiLeft == that.guiLeft && guiTop == that.guiTop && guiXSize == that.guiXSize && guiYSize == that.guiYSize &&
                screenWidth == that.screenWidth && screenHeight == that.screenHeight &&
                Objects.equals(screenClass, that.screenClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screenClass, guiLeft, guiTop, guiXSize, guiYSize, screenWidth, screenHeight);
    }
}

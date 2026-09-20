package brachy.modularui.screen.viewport;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.UIType;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.utils.Stencil;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * A gui context contains various properties like screen size, mouse position, last clicked button etc.
 * It also is a matrix/pose stack.
 * A default instance can be obtained using {@link #getDefault()}, which can be used in {@link IDrawable IDrawables} for
 * example.
 * That instance is automatically updated at all times (except when no UI is currently open).
 */
public class GuiContext extends GuiViewportStack {

    public static GuiContext getDefault() {
        return ClientScreenHandler.getBestContext();
    }

    @Getter
    private final UIType UItype;

    @Getter
    private final Area screenArea = new Area();
    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    private GuiGraphicsExtractor graphics = null;
    private @Nullable Font overrideFont = null;
    @Getter
    private final Stencil stencil = new Stencil(this);

    /* Mouse states */
    private int mouseX;
    private int mouseY;
    @Getter private int lastMouseButton;
    @Getter private boolean lastButtonPress; // button pressed = true, button released = false
    @Getter private double lastMouseScrollDeltaX, lastMouseScrollDeltaY;

    /* Keyboard states */
    @Getter private int lastKeyCode;
    @Getter private int lastScanCode;
    @Getter private int lastKeyModifiers;
    @Getter private int lastCodePoint;
    @Getter private boolean lastKeyPress; // key pressed = true, key released = false

    /* Render states */
    @Getter private float renderPartialTicks;
    @Getter private long tick = 0;
    @Getter private int currentDrawingZ = 0;

    public GuiContext(UIType UItype) {
        this.UItype = UItype;
    }

    public boolean isAbove(IWidget widget) {
        return isMouseAbove(widget.getArea());
    }

    /**
     * @return true the mouse is anywhere above the widget
     */
    public boolean isMouseAbove(IWidget widget) {
        return isMouseAbove(widget.getArea());
    }

    /**
     * @return true the mouse is anywhere above the area
     */
    public boolean isMouseAbove(Area area) {
        return area.isInside(this.mouseX, this.mouseY);
    }

    @ApiStatus.Internal
    public void updateState(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.renderPartialTicks = partialTicks;
    }

    @ApiStatus.Internal
    public void updateMouseButton(int button, boolean pressed) {
        this.lastMouseButton = button;
        this.lastButtonPress = pressed;
    }

    @ApiStatus.Internal
    public void updateMouseWheel(double scrollDeltaX, double scrollDeltaY) {
        this.lastMouseScrollDeltaX = scrollDeltaX;
        this.lastMouseScrollDeltaY = scrollDeltaY;
    }

    @ApiStatus.Internal
    public void updateKey(int keyCode, int scanCode, int modifiers, boolean pressed) {
        this.lastKeyCode = keyCode;
        this.lastScanCode = scanCode;
        this.lastKeyModifiers = modifiers;
        this.lastKeyPress = pressed;
    }

    @ApiStatus.Internal
    public void updateTypedChar(int codePoint, int modifiers) {
        this.lastCodePoint = codePoint;
        this.lastKeyModifiers = modifiers;
    }

    @ApiStatus.Internal
    public void updateScreenArea(int w, int h) {
        this.screenArea.set(0, 0, w, h);
        this.screenArea.rx = 0;
        this.screenArea.ry = 0;
    }

    public void updateZ(int z) {
        this.currentDrawingZ = z;
    }

    @OnlyIn(Dist.CLIENT)
    public Minecraft getMC() {
        return Minecraft.getInstance();
    }

    @OnlyIn(Dist.CLIENT)
    public Font getFont() {
        if (overrideFont != null) {
            return overrideFont;
        } else {
            return MCHelper.getFont();
        }
    }

    @ApiStatus.Internal
    @OnlyIn(Dist.CLIENT)
    public void setOverrideFont(@Nullable Font overrideFont) {
        this.overrideFont = overrideFont;
    }

    public void tick() {
        this.tick += 1;
    }

    /* Viewport */

    public Matrix4f getLastGraphicsPose() {
        if (graphics == null) return new Matrix4f();
        return brachy.modularui.utils.GuiPoseTransforms.snapshot(graphics.pose());
    }

    public org.joml.Matrix3x2fStack graphicsPose() {
        if (graphics == null) throw new IllegalStateException("No GUI graphics extractor is active");
        return graphics.pose();
    }

    public int getMouseX() {
        return unTransformX(this.mouseX, this.mouseY);
    }

    public int getMouseY() {
        return unTransformY(this.mouseX, this.mouseY);
    }

    /**
     * Get absolute X coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseX() {
        return this.mouseX;
    }

    /**
     * Get absolute Y coordinate of the mouse without the
     * scrolling areas applied
     */
    public int getAbsMouseY() {
        return this.mouseY;
    }

    public int getMouse(GuiAxis axis) {
        return axis.isHorizontal() ? getMouseX() : getMouseY();
    }

    public int getAbsMouse(GuiAxis axis) {
        return axis.isHorizontal() ? getAbsMouseX() : getAbsMouseY();
    }

    public boolean isMuiContext() {
        return false;
    }

    public ModularGuiContext getMuiContext() {
        throw new UnsupportedOperationException("This is not a MuiContext");
    }
}

package brachy.modularui.utils;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.drawable.GuiShapeRenderState;
import brachy.modularui.drawable.GuiStencil;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.widget.sizer.Area;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Transformed GUI clipping backed by deferred NeoForge stencil commands. */
public class Stencil {
    private static final ObjectArrayList<Area> stencils = new ObjectArrayList<>();
    private final GuiContext context;

    @ApiStatus.Internal
    public Stencil(GuiContext context) {
        this.context = context;
    }

    public static void reset() {
        stencils.clear();
        GuiStencil.reset();
    }

    public void push(@NotNull Rectangle area) {
        push(area.x, area.y, area.width, area.height);
    }

    public void pushAtZero(@NotNull Rectangle area) {
        push(0, 0, area.width, area.height);
    }

    public void push(float x, float y, float w, float h) {
        push(() -> GuiShapeRenderState.submit(context.getGraphics(), List.of(
                new GuiShapeRenderState.Vertex(x, y, -1),
                new GuiShapeRenderState.Vertex(x, y + h, -1),
                new GuiShapeRenderState.Vertex(x + w, y + h, -1),
                new GuiShapeRenderState.Vertex(x + w, y, -1)), GuiShapeRenderState.Topology.QUADS),
                (int) Math.floor(x), (int) Math.floor(y),
                (int) Math.ceil(x + w) - (int) Math.floor(x),
                (int) Math.ceil(y + h) - (int) Math.floor(y));
    }

    public void push(Runnable shape, boolean hide) {
        push(shape, 0, 0, 0, 0, hide);
    }

    public void push(Runnable shape, int x, int y, int w, int h) {
        push(shape, x, y, w, h, true);
    }

    public void push(Runnable shape, int x, int y, int w, int h, boolean hide) {
        Area scissor = new Area(x, y, w, h);
        scissor.transformAndRectanglerize(context);
        if (!stencils.isEmpty()) stencils.top().clamp(scissor);
        GuiStencil.push(context.getGraphics(), shape, hide);
        stencils.add(scissor);
    }

    public void pop() {
        if (stencils.isEmpty()) throw new IllegalStateException("Tried to pop an empty stencil stack!");
        GuiStencil.pop(context.getGraphics());
        stencils.pop();
    }

    public static boolean isInsideScissorArea(Area area, IViewportStack stack) {
        if (stencils.isEmpty()) return true;
        Area.SHARED.set(0, 0, area.width, area.height);
        Area.SHARED.transformAndRectanglerize(stack);
        return stencils.top().intersects(Area.SHARED);
    }
}

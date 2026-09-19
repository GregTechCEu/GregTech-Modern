package brachy.modularui.drawable;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.StencilManager;
import net.neoforged.neoforge.client.stencil.StencilOperation;
import net.neoforged.neoforge.client.stencil.StencilPerFaceTest;
import net.neoforged.neoforge.client.stencil.StencilTest;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/** Captures stencil operations in the GUI command stream, never in immediate GL state. */
public final class GuiStencil {
    private static final List<List<GuiElementRenderState>> MASKS = new ArrayList<>();
    private static final Map<PipelineKey, RenderPipeline> PIPELINES = new HashMap<>();
    private static List<GuiElementRenderState> capture;
    private static Integer renderingBit;
    private static int activeBit;
    private static int bitA, bitB;

    private GuiStencil() {}

    public static void reset() {
        MASKS.clear();
        capture = null;
        activeBit = 0;
    }

    public static int currentBit() {
        return renderingBit == null ? activeBit : renderingBit;
    }

    /** Called by the GUI-state mixin, including for vanilla/custom drawable submissions. */
    public static boolean capture(GuiElementRenderState state) {
        if (capture == null || state instanceof StencilState) return false;
        capture.add(state);
        return true;
    }

    public static GuiElementRenderState clip(GuiElementRenderState state) {
        int bit = currentBit();
        if (bit == 0 || state instanceof StencilState) return state;
        return wrap(state, new StencilTest(face(StencilOperation.KEEP, CompareOp.EQUAL), bit, 0, bit), false);
    }

    public static <T> Consumer<T> scoped(Consumer<T> consumer, Map<Object, Integer> bits) {
        return state -> {
            Integer previous = renderingBit;
            renderingBit = bits.getOrDefault(state, 0);
            try {
                consumer.accept(state);
            } finally {
                renderingBit = previous;
            }
        };
    }

    public static void push(GuiGraphicsExtractor graphics, Runnable shape, boolean hide) {
        if (capture != null) throw new IllegalStateException("Cannot nest stencil pushes inside a mask callback");
        reserveBits();
        List<GuiElementRenderState> geometry = new ArrayList<>();
        capture = geometry;
        try {
            shape.run();
        } finally {
            capture = null;
        }
        if (!hide) geometry.forEach(graphics::submitGuiElementRenderState);
        MASKS.add(List.copyOf(geometry));
        rebuild(graphics);
    }

    public static void pop(GuiGraphicsExtractor graphics) {
        if (MASKS.isEmpty()) throw new IllegalStateException("Tried to pop an empty stencil stack");
        MASKS.removeLast();
        rebuild(graphics);
    }

    private static void reserveBits() {
        if (bitA != 0) return;
        int a = StencilManager.reserveBit();
        int b = StencilManager.reserveBit();
        if (a < 0 || a > 7 || b < 0 || b > 7) {
            StencilManager.releaseBit(a);
            StencilManager.releaseBit(b);
            throw new IllegalStateException("ModularUI requires two available stencil bits");
        }
        bitA = 1 << a;
        bitB = 1 << b;
    }

    private static void rebuild(GuiGraphicsExtractor graphics) {
        // Strata are ordering barriers: batching must not move masks across their consumers.
        graphics.nextStratum();
        activeBit = 0;
        if (MASKS.isEmpty()) return;
        var screen = GuiShapeRenderState.extract(new Matrix3x2f(), null, List.of(
                new GuiShapeRenderState.Vertex(0, 0, -1),
                new GuiShapeRenderState.Vertex(0, graphics.guiHeight(), -1),
                new GuiShapeRenderState.Vertex(graphics.guiWidth(), graphics.guiHeight(), -1),
                new GuiShapeRenderState.Vertex(graphics.guiWidth(), 0, -1)), GuiShapeRenderState.Topology.QUADS);
        for (List<GuiElementRenderState> mask : MASKS) {
            int next = activeBit == bitA ? bitB : bitA;
            // Clear only our destination bit, preserving other mods' stencil allocations.
            graphics.submitGuiElementRenderState(wrap(screen,
                    new StencilTest(face(StencilOperation.ZERO, CompareOp.ALWAYS_PASS), 0, next, 0), true));
            graphics.nextStratum();
            var test = new StencilTest(face(StencilOperation.REPLACE,
                    activeBit == 0 ? CompareOp.ALWAYS_PASS : CompareOp.EQUAL), activeBit, next, activeBit | next);
            for (GuiElementRenderState state : mask) graphics.submitGuiElementRenderState(wrap(state, test, true));
            graphics.nextStratum();
            activeBit = next;
        }
    }

    private static StencilPerFaceTest face(StencilOperation pass, CompareOp compare) {
        return new StencilPerFaceTest(StencilOperation.KEEP, StencilOperation.KEEP, pass, compare);
    }

    private static GuiElementRenderState wrap(GuiElementRenderState state, StencilTest test, boolean mask) {
        var key = new PipelineKey(state.pipeline(), test, mask);
        RenderPipeline pipeline = PIPELINES.computeIfAbsent(key, k -> {
            if (k.base().getStencilTest().isPresent()) {
                throw new IllegalArgumentException("Cannot combine an existing stencil pipeline with ModularUI clipping");
            }
            var builder = k.base().toBuilder()
                    .withLocation(Identifier.fromNamespaceAndPath("modularui", "stencil/" + PIPELINES.size()))
                    .withStencilTest(test);
            if (mask) builder.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, 0));
            return builder.build();
        });
        return new StencilState(state, pipeline);
    }

    private record PipelineKey(RenderPipeline base, StencilTest test, boolean mask) {}

    private record StencilState(GuiElementRenderState delegate, RenderPipeline pipeline) implements GuiElementRenderState {
        @Override public void buildVertices(VertexConsumer consumer) { delegate.buildVertices(consumer); }
        @Override public TextureSetup textureSetup() { return delegate.textureSetup(); }
        @Override public ScreenRectangle scissorArea() { return delegate.scissorArea(); }
        @Override public ScreenRectangle bounds() { return delegate.bounds(); }
    }
}

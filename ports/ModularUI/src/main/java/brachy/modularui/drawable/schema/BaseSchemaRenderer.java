package brachy.modularui.drawable.schema;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.Icon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widgets.SchemaWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Extracts a structure snapshot on the game thread for deferred picture-in-picture rendering. */
@Accessors(fluent = true)
public class BaseSchemaRenderer implements IDrawable {
    @Getter private final ISchema schema;
    private final RenderLevel renderLevel;
    @Getter private final Camera camera = new Camera();
    @Getter private BlockHitResult lastRayTrace;
    @Getter private RenderFilter renderFilter = RenderFilter.ALL;
    @Getter private final Matrix4f projection = new Matrix4f();
    @Getter @Setter private boolean captureDebugInfo;
    @Getter private final Vector3f openGLMousePos = new Vector3f();
    private List<SchemaGeometry.Layer> layers = List.of();
    private final List<BlockEntity> blockEntities = new ArrayList<>();
    private final List<SchemaGeometry.Vertex> highlights = new ArrayList<>();
    private Object modelSet;
    private boolean dirty = true;
    private CompileStatus status = CompileStatus.CANCELED;
    private int viewportWidth = 1, viewportHeight = 1;

    public BaseSchemaRenderer(ISchema schema) {
        this.schema = schema;
        this.renderLevel = new RenderLevel(schema, (pos, state) -> renderFilter.shouldRender(pos, state));
    }
    public void notifyRecompile() { dirty = true; }
    protected void cancelCompilation() { if (status != CompileStatus.DISABLED) status = CompileStatus.CANCELED; }
    public boolean isCompiling() { return status == CompileStatus.COMPILING; }
    public boolean isCompleted() { return status == CompileStatus.SUCCESS; }
    public boolean isCanceled() { return status == CompileStatus.CANCELED; }
    public void dispose() {
        layers = List.of();
        blockEntities.clear();
        highlights.clear();
        modelSet = null;
        status = CompileStatus.DISABLED;
    }
    @Override public SchemaWidget asWidget() { return new SchemaWidget(this); }
    @Override public Icon asIcon() { return IDrawable.super.asIcon().size(50); }

    protected void recompile() {
        status = CompileStatus.COMPILING;
        try {
            Minecraft minecraft = Minecraft.getInstance();
            var models = minecraft.getModelManager().getBlockStateModelSet();
            var blocks = new ModelBlockRenderer(true, true, minecraft.getBlockColors());
            var fluids = new FluidRenderer(minecraft.getModelManager().getFluidStateModelSet());
            Map<RenderType, SchemaGeometry.Builder> builders = new LinkedHashMap<>();
            List<BlockEntity> entities = new ArrayList<>();
            var lightEngine = renderLevel.getLightEngine();
            while (lightEngine.hasLightWork()) lightEngine.runLightUpdates();
            for (var entry : schema) {
                var pos = entry.getKey();
                var state = entry.getValue();
                if (!renderFilter.shouldRender(pos, state)) continue;
                if (state.hasBlockEntity()) {
                    var entity = renderLevel.getBlockEntity(pos);
                    if (entity != null) entities.add(entity);
                }
                if (!state.getFluidState().isEmpty()) {
                    fluids.tesselate(renderLevel, pos, layer -> {
                        var type = layer == ChunkSectionLayer.TRANSLUCENT
                                ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
                        return builders.computeIfAbsent(type, ignored -> new SchemaGeometry.Builder())
                                .offset(pos.getX() & ~15, pos.getY() & ~15, pos.getZ() & ~15);
                    }, state, state.getFluidState());
                }
                if (state.getRenderShape() == RenderShape.MODEL) {
                    blocks.tesselateBlock((x, y, z, quad, instance) -> {
                        var builder = builders.computeIfAbsent(quad.materialInfo().itemRenderType(),
                                ignored -> new SchemaGeometry.Builder()).offset(0, 0, 0);
                        builder.putBlockBakedQuad(x, y, z, quad, instance);
                    }, pos.getX(), pos.getY(), pos.getZ(), renderLevel, pos, state, models.get(state), state.getSeed(pos));
                }
            }
            layers = builders.entrySet().stream()
                    .map(entry -> new SchemaGeometry.Layer(entry.getKey(), entry.getValue().build()))
                    .filter(layer -> !layer.vertices().isEmpty()).toList();
            blockEntities.clear();
            blockEntities.addAll(entities);
            modelSet = models;
            dirty = false;
            status = CompileStatus.SUCCESS;
            onRendered();
        } catch (RuntimeException | Error error) {
            status = CompileStatus.CANCELED;
            throw error;
        }
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme theme) {
        if (width <= 0 || height <= 0 || status == CompileStatus.DISABLED) return;
        if (dirty || status == CompileStatus.CANCELED ||
                modelSet != Minecraft.getInstance().getModelManager().getBlockStateModelSet()) recompile();
        onSetupCamera();
        setupCamera(width, height);
        highlights.clear();
        if (doRayTrace() || captureDebugInfo) {
            int mouseX = context.getMouseX() - x, mouseY = context.getMouseY() - y;
            BlockHitResult hit = mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height
                    ? rayTrace(mouseX, mouseY, width, height) : null;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) onSuccessfulRayTrace(new PoseStack(), hit);
            else if (lastRayTrace != null) onRayTraceFailed();
            lastRayTrace = hit;
        }
        List<BlockEntityRenderState> entities = new ArrayList<>();
        if (isBEREnabled()) {
            for (BlockEntity entity : blockEntities) {
                var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
                if (renderer != null && entity.hasLevel()) {
                    var state = renderer.createRenderState();
                    renderer.extractRenderState(entity, state, context.getRenderPartialTicks(), new Vec3(camera.pos()), null);
                    entities.add(state);
                }
            }
        }
        var graphics = context.getGraphics();
        var scissor = graphics.peekScissorStack();
        var bounds = new ScreenRectangle(x, y, width, height).transformMaxBounds(graphics.pose());
        if (scissor != null) bounds = bounds.intersection(scissor);
        if (bounds == null) return;
        GuiDraw.drawRect(graphics, x, y, width, height, getClearColor());
        graphics.submitPictureInPictureRenderState(new SchemaRenderState(layers, entities, highlights,
                DummyLightTexture.extract(renderLevel), SchemaCameraTransform.view(camera.pos(), camera.lookAt()),
                camera.pos(), isIsometric(), SchemaCameraTransform.orthoHeight(camera.pos(), camera.lookAt()),
                getClearColor(), graphics.pose(), x, y, x + width, y + height, scissor, bounds));
        if (captureDebugInfo) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            drawProjectedBlockPos(graphics, width, height);
            graphics.pose().popMatrix();
        }
    }

    protected void queueHighlight(BlockHighlight highlight, BlockHitResult result) {
        highlights.addAll(highlight.extract(result, camera.pos()));
    }
    public PoseStack createWorldRenderPose() {
        var pose = new PoseStack();
        pose.translate(-camera.pos().x, -camera.pos().y, -camera.pos().z);
        return pose;
    }
    protected void setupCamera(int width, int height) {
        viewportWidth = width;
        viewportHeight = height;
        projection.set(SchemaCameraTransform.projection(width, height, isIsometric(),
                SchemaCameraTransform.orthoHeight(camera.pos(), camera.lookAt()), false, false))
                .mul(SchemaCameraTransform.view(camera.pos(), camera.lookAt()));
    }
    protected BlockHitResult rayTrace(int x, int y, int width, int height) {
        var from = SchemaCameraTransform.unproject(projection, x, y, width, height, 0);
        var to = SchemaCameraTransform.unproject(projection, x, y, width, height, 1);
        openGLMousePos.set(x, height - y, 1);
        return renderLevel.clip(new ClipContext(new Vec3(from), new Vec3(to),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));
    }
    public Vector3f screenToOpenGLPos(int x, int y, int width, int height, Vector3f dest) {
        return screenToOpenGLPos(x, y, width, height, 0, dest);
    }
    public Vector3f screenToOpenGLPos(int x, int y, int width, int height, float depth, Vector3f dest) {
        return dest.set((float) x * viewportWidth / width, (float) (height - y) * viewportHeight / height, depth);
    }
    public Vector3f screenToWorldPos(int x, int y, int width, int height) {
        var hit = rayTrace(x, y, width, height);
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) return hit.getLocation().toVector3f();
        return SchemaCameraTransform.unproject(projection, x, y, width, height, 0);
    }
    public void drawProjectedBlockPos(GuiGraphicsExtractor graphics, int width, int height) {
        for (var entry : schema) {
            if (entry.getValue().isAir() || !renderFilter.shouldRender(entry.getKey(), entry.getValue())) continue;
            var p = entry.getKey();
            var point = projection.project(p.getX() + 0.5f, p.getY() + 0.5f, p.getZ() + 0.5f,
                    new int[]{0, 0, width, height}, new Vector3f());
            if (point.z >= 0 && point.z <= 1) GuiDraw.drawRect(graphics, point.x - 1, height - point.y - 1, 2, 2, 0xff0000ff);
        }
    }
    protected void onSetupCamera() {}
    protected void onRendered() {}
    protected void onSuccessfulRayTrace(PoseStack pose, @NotNull BlockHitResult result) {}
    protected void onRayTraceFailed() {}
    public boolean doRayTrace() { return false; }
    public int getClearColor() { return 0x80ffffff; }
    public boolean isIsometric() { return false; }
    public boolean isBEREnabled() { return true; }
    public void updateRenderFilter(RenderFilter filter) {
        renderFilter = filter == null ? RenderFilter.ALL : filter;
        notifyRecompile();
    }
    protected enum CompileStatus { DISABLED, COMPILING, SUCCESS, CANCELED }
}

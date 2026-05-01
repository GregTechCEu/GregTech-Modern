package com.lowdragmc.lowdraglib.gui.widget;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.client.scene.ParticleManager;
import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.lowdragmc.lowdraglib2.client.scene.ISceneBlockRenderHook;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SceneWidget extends WidgetGroup {

    private TrackedDummyWorld dummyWorld;
    protected WorldSceneRenderer renderer;
    protected Vector3f center = new Vector3f();
    protected float zoom = 1;
    protected float range = 1;
    protected float rotationYaw;
    protected float rotationPitch;
    protected boolean draggable = true;
    protected boolean scalable = true;
    protected boolean intractable = true;
    protected boolean hoverTips = true;
    protected boolean renderFacing = true;
    protected boolean renderSelect = true;
    protected boolean useCache;
    protected boolean useOrtho;
    protected boolean dragging;
    protected int currentMouseX;
    protected int currentMouseY;
    protected BlockPosFace clickPosFace;
    protected BlockPosFace hoverPosFace;
    protected BlockPosFace selectedPosFace;
    protected ItemStack hoverItem = ItemStack.EMPTY;
    protected Set<BlockPos> core = Set.of();
    protected Consumer<SceneWidget> beforeWorldRender;
    protected Consumer<SceneWidget> afterWorldRender;
    private BiConsumer<BlockPos, Direction> onSelected = (pos, side) -> {};

    public SceneWidget(int x, int y, int width, int height, Level level, boolean renderFacing) {
        super(x, y, width, height);
        createScene(level, renderFacing);
    }

    public SceneWidget(int x, int y, int width, int height, Level level) {
        this(x, y, width, height, level, true);
    }

    public SceneWidget setOnAddedTooltips(BiConsumer<SceneWidget, List<Component>> onAddedTooltips) {
        return this;
    }

    public SceneWidget useCacheBuffer() {
        return useCacheBuffer(true);
    }

    public SceneWidget useCacheBuffer(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    public SceneWidget useOrtho() {
        return useOrtho(true);
    }

    public SceneWidget useOrtho(boolean useOrtho) {
        this.useOrtho = useOrtho;
        return this;
    }

    public SceneWidget setBeforeWorldRender(Consumer<SceneWidget> beforeWorldRender) {
        this.beforeWorldRender = beforeWorldRender;
        return this;
    }

    public SceneWidget setAfterWorldRender(Consumer<SceneWidget> afterWorldRender) {
        this.afterWorldRender = afterWorldRender;
        return this;
    }

    public float camZoom() {
        return zoom;
    }

    public ParticleManager getParticleManager() {
        return new ParticleManager();
    }

    @Override
    public void setGui(ModularUI gui) {
        super.setGui(gui);
    }

    public void releaseCacheBuffer() {}

    public void needCompileCache() {}

    public final void createScene(Level level) {
        createScene(level, true);
    }

    public final void createScene(Level level, boolean renderFacing) {
        this.renderFacing = renderFacing;
        this.dummyWorld = level instanceof TrackedDummyWorld tracked ? tracked : new TrackedDummyWorld(level);
        this.renderer = new WorldSceneRenderer(dummyWorld) {};
    }

    public WorldSceneRenderer getRenderer() {
        return renderer;
    }

    public TrackedDummyWorld getDummyWorld() {
        return dummyWorld;
    }

    public SceneWidget setOnSelected(BiConsumer<BlockPos, Direction> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public SceneWidget setClearColor(int color) {
        return this;
    }

    public SceneWidget setRenderSelect(boolean renderSelect) {
        this.renderSelect = renderSelect;
        return this;
    }

    public SceneWidget setRenderFacing(boolean renderFacing) {
        this.renderFacing = renderFacing;
        return this;
    }

    public SceneWidget setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public SceneWidget setScalable(boolean scalable) {
        this.scalable = scalable;
        return this;
    }

    public SceneWidget setIntractable(boolean intractable) {
        this.intractable = intractable;
        return this;
    }

    public SceneWidget setHoverTips(boolean hoverTips) {
        this.hoverTips = hoverTips;
        return this;
    }

    public SceneWidget setRenderedCore(Collection<BlockPos> core) {
        this.core = Set.copyOf(core);
        return this;
    }

    public SceneWidget setRenderedCore(Collection<BlockPos> core, ISceneBlockRenderHook hook) {
        this.core = Set.copyOf(core);
        if (renderer != null) {
            renderer.addRenderedBlocks(core, hook);
        }
        return this;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        currentMouseX = mouseX;
        currentMouseY = mouseY;
        if (renderer != null) {
            var position = getPosition();
            var size = getSize();
            // LDLib2's render() expects a 3D PoseStack (not the GUI's Matrix3x2fStack).
            renderer.render(new PoseStack(), position.x, position.y, size.width, size.height, mouseX, mouseY);
            renderBlockOverLay(renderer);
        }
    }

    public void renderBlockOverLay(WorldSceneRenderer renderer) {}

    public void drawFacingBorder(PoseStack poseStack, BlockPosFace face, int color) {}

    public void drawFacingBorder(PoseStack poseStack, BlockPosFace face, int color, int width) {}

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {}

    public SceneWidget setCenter(Vector3f center) {
        this.center = center;
        return this;
    }

    public SceneWidget setZoom(float zoom) {
        this.zoom = zoom;
        return this;
    }

    public SceneWidget setOrthoRange(float range) {
        this.range = range;
        return this;
    }

    public SceneWidget setCameraYawAndPitch(float yaw, float pitch) {
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        return this;
    }

    public void setCameraYawAndPitchAnima(float yaw, float pitch, int duration) {
        setCameraYawAndPitch(yaw, pitch);
    }

    public boolean isDragging() {
        return false;
    }

    public boolean isRenderFacing() {
        return renderFacing;
    }

    public boolean isRenderSelect() {
        return renderSelect;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public boolean isScalable() {
        return scalable;
    }

    public boolean isIntractable() {
        return intractable;
    }

    public boolean isHoverTips() {
        return hoverTips;
    }

    public int getCurrentMouseX() {
        return currentMouseX;
    }

    public int getCurrentMouseY() {
        return currentMouseY;
    }

    public Vector3f getCenter() {
        return center;
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    public float getZoom() {
        return zoom;
    }

    public float getRange() {
        return range;
    }

    public BlockPosFace getClickPosFace() {
        return clickPosFace;
    }

    public BlockPosFace getHoverPosFace() {
        return hoverPosFace;
    }

    public BlockPosFace getSelectedPosFace() {
        return selectedPosFace;
    }

    public ItemStack getHoverItem() {
        return hoverItem;
    }

    public BiConsumer<BlockPos, Direction> getOnSelected() {
        return onSelected;
    }

    public Set<BlockPos> getCore() {
        return core;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public boolean isUseOrtho() {
        return useOrtho;
    }

    public boolean isAutoReleased() {
        return true;
    }
}

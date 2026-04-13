package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemEntryHandler;

import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.lowdragmc.lowdraglib.utils.ItemStackKey;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.emi.emi.screen.RecipeScreen;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.shedaniel.rei.impl.client.gui.screen.AbstractDisplayViewingScreen;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH;

@OnlyIn(Dist.CLIENT)
public class PatternPreviewWidget extends WidgetGroup {

    private boolean isLoaded;
    private static TrackedDummyWorld LEVEL;
    private static final int REGION_SIZE = 512;
    private static int LAST_OFFSET_INDEX = 0;
    private static final Map<MultiblockMachineDefinition, MBPattern[]> CACHE = new HashMap<>();
    private final PreviewSceneWidget sceneWidget;
    private final DraggableScrollableWidgetGroup scrollableWidgetGroup;
    public final MultiblockMachineDefinition controllerDefinition;
    public final MBPattern[] patterns;
    private final List<SimplePredicate> predicates;
    public boolean isHighLight;
    private int index;
    public int layer;
    private SlotWidget[] slotWidgets;
    private SlotWidget[] candidates;

    protected PatternPreviewWidget(MultiblockMachineDefinition controllerDefinition) {
        super(0, 0, 160 + getSizeOffset(), 160 + getSizeOffset());
        setClientSideWidget();
        this.controllerDefinition = controllerDefinition;
        predicates = new ArrayList<>();
        layer = -1;
        isHighLight = false;
        sceneWidget = new PreviewSceneWidget(3, 3, 150 + getSizeOffset(), 150 + getSizeOffset(), LEVEL);
        sceneWidget.setOnSelected(this::onPosSelected);
        sceneWidget.setRenderFacing(false);
        addWidget(sceneWidget);

        scrollableWidgetGroup = new DraggableScrollableWidgetGroup(3, 136 + getSizeOffset(), 154 + getSizeOffset(), 22)
                .setXScrollBarHeight(4)
                .setXBarStyle(GuiTextures.SLIDER_BACKGROUND, GuiTextures.BUTTON)
                .setScrollable(true)
                .setDraggable(true);
        scrollableWidgetGroup.setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.HORIZONTAL);
        scrollableWidgetGroup.setScrollYOffset(0);
        addWidget(scrollableWidgetGroup);

        if (ConfigHolder.INSTANCE.client.useVBO) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(sceneWidget::useCacheBuffer);
            } else {
                sceneWidget.useCacheBuffer();
            }
        }

        addWidget(new ImageWidget(3, 3, 160 + getSizeOffset(), 10,
                new TextTexture(controllerDefinition.getDescriptionId(), -1)
                        .setType(TextTexture.TextType.ROLL)
                        .setWidth(170)
                        .setDropShadow(true)));

        this.patterns = CACHE.computeIfAbsent(controllerDefinition, definition -> {
            HashSet<ItemStackKey> drops = new HashSet<>();
            drops.add(new ItemStackKey(this.controllerDefinition.asStack()));
            return controllerDefinition.getMatchingShapes().stream()
                    .map(it -> initializePattern(it, drops))
                    .filter(Objects::nonNull)
                    .toArray(MBPattern[]::new);
        });

        addWidget(new ButtonWidget(138 + getSizeOffset(), 30, 18, 18, new GuiTextureGroup(
                ColorPattern.T_GRAY.rectTexture(),
                new TextTexture("1").setSupplier(() -> "P:" + index)),
                (x) -> setPage((index + 1 >= patterns.length) ? 0 : index + 1))
                .setHoverBorderTexture(1, -1)
                .appendHoverTooltips(Component.translatable("gtceu.gui.switchlevel")));

        addWidget(new ButtonWidget(138 + getSizeOffset(), 50, 18, 18, new GuiTextureGroup(
                ColorPattern.T_GRAY.rectTexture(),
                new TextTexture("1").setSupplier(() -> layer >= 0 ? "L:" + layer : "ALL")),
                cd -> updateLayer())
                .setHoverBorderTexture(1, -1)
                .appendHoverTooltips(Component.translatable("gtceu.gui.showlayer")));
        addWidget(new ButtonWidget(138 + getSizeOffset(), 70, 18, 18, new GuiTextureGroup(
                ColorPattern.T_GRAY.rectTexture(),
                new TextTexture("1").setSupplier(() -> isHighLight ? "ON" : "OFF")),
                cd -> updateHighLight())
                .setHoverBorderTexture(1, -1)
                .appendHoverTooltips(Component.translatable("gtceu.gui.highlight")));
        setPage(0);
    }

    static int sizeOffset = -1;

    public static int getSizeOffset() {
        if (sizeOffset == -1) {
            if (ConfigHolder.INSTANCE != null &&
                    ConfigHolder.INSTANCE.client.widgetScale == ConfigHolder.ClientConfigs.WidgetScale.LARGE) {
                sizeOffset = 40;
            } else {
                sizeOffset = 0;
            }
        }
        return sizeOffset;
    }

    private final class PreviewSceneWidget extends SceneWidget {

        private static final float LINE_HALF_WIDTH = 0.1f;

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.intractable) {
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            } else if (this.dragging && button == 0) {
                this.rotationPitch = (float) ((double) this.rotationPitch + dragX + 360.0);
                this.rotationPitch %= 360.0F;
                this.rotationYaw = (float) Mth.clamp((double) this.rotationYaw + dragY, -89.9, 89.9);
                if (this.renderer != null) {
                    this.renderer.setCameraLookAt(this.center, (double) this.camZoom(),
                            Math.toRadians((double) this.rotationPitch), Math.toRadians((double) this.rotationYaw));
                }
                return false;
            } else if (this.dragging && button == 1) {// 右键情况下
                if (this.renderer == null) return false;

                Vector3f eyePos = new Vector3f(this.renderer.getEyePos());
                Vector3f lookAt = new Vector3f(this.renderer.getLookAt());
                Vector3f worldUp = new Vector3f(this.renderer.getWorldUp());

                float speed = 1.0f;

                // 建议与 zoom 绑定（体验提升非常明显）
                speed *= (float) this.camZoom();

                // forward = lookAt - eyePos
                Vector3f forward = new Vector3f(lookAt).sub(eyePos).normalize();

                // right = forward × worldUp
                Vector3f right = new Vector3f(forward).cross(worldUp).normalize();

                // camera up = right × forward
                Vector3f up = new Vector3f(right).cross(forward).normalize();

                // 移动向量
                Vector3f move = new Vector3f();

                move.add(new Vector3f(right).mul((float) (-dragX / getSizeWidth() * speed)));
                move.add(new Vector3f(up).mul((float) (dragY / getSizeHeight() * speed)));

                eyePos.add(move);
                lookAt.add(move);

                this.center.add(move);

                this.renderer.setCameraLookAt(eyePos, lookAt, worldUp);

                return false;

            } else {
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
        }

        private final Map<BlockPos, Integer> colorCaches = new HashMap<>();

        private VertexBuffer highlightVbo;
        private boolean highlightDirty = true;
        private int vertexCount;

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (!this.intractable) {
                return false;
            } else if (this.isMouseOverElement(mouseX, mouseY)) {
                if (this.draggable) {
                    this.dragging = true;
                }
                this.clickPosFace = this.hoverPosFace;
                return true;
            } else {
                this.dragging = false;
                return false;
            }
        }

        public PreviewSceneWidget(int x, int y, int w, int h, TrackedDummyWorld world) {
            super(x, y, w, h, world);
        }

        public void markHighlightDirty() {
            highlightDirty = true;
            colorCaches.clear();
        }

        private void rebuildHighlightBuffer() {
            highlightDirty = false;

            if (highlightVbo != null) {
                highlightVbo.close();
                highlightVbo = null;
            }

            if (!isHighLight || core.isEmpty()) return;

            Map<BlockPos, TraceabilityPredicate> predicateMap = patterns[index].controllerBase
                    .getMultiblockState()
                    .getMatchContext()
                    .get("predicates");

            if (predicateMap == null) return;

            BufferBuilder builder = Tesselator.getInstance().getBuilder();

            builder.begin(VertexFormat.Mode.QUADS,
                    DefaultVertexFormat.POSITION_COLOR);

            vertexCount = 0;

            for (BlockPos pos : core) {

                if (selectedPosFace != null && pos.equals(selectedPosFace.pos))
                    continue;

                TraceabilityPredicate p = predicateMap.get(pos);
                if (p == null) continue;
                // if (selectedPosFace != null && pos == selectedPosFace.pos) return;
                int color = 0;
                if (!colorCaches.containsKey(pos)) {
                    if (predicateMap.containsKey(pos)) {
                        var predicate = predicateMap.get(pos);
                        List<ItemStack> candidates = new ArrayList<ItemStack>();
                        predicate.common.forEach(y -> candidates.addAll(y.getCandidates()));
                        predicate.limited.forEach(y -> candidates.addAll(y.getCandidates()));
                        int cnt = 0;
                        for (var candidate : candidates) {
                            if (cnt > 1) break;
                            if (candidate.equals(ITEM_IMPORT_BUS[GTValues.LV].asStack(), false) ||
                                    candidate.equals(FLUID_IMPORT_HATCH[GTValues.LV].asStack(), false) ||
                                    candidate.equals(STEAM_IMPORT_BUS.asStack(), false)) {
                                cnt++;
                                color = 0x00ff00ff;// 绿色
                                continue;
                            }
                            if (candidate.equals(ITEM_EXPORT_BUS[GTValues.LV].asStack(), false) ||
                                    candidate.equals(FLUID_EXPORT_HATCH[GTValues.LV].asStack(), false) ||
                                    candidate.equals(STEAM_EXPORT_BUS.asStack(), false)) {
                                cnt++;
                                color = 0xff8000ff;// 橙色
                                continue;
                            }
                            if (candidate.equals(ENERGY_INPUT_HATCH[GTValues.LV].asStack(), false) ||
                                    candidate.equals(ENERGY_OUTPUT_HATCH[GTValues.LV].asStack(), false) ||
                                    candidate.equals(LASER_INPUT_HATCH_256[GTValues.IV].asStack(), false) ||
                                    candidate.equals(LASER_OUTPUT_HATCH_256[GTValues.IV].asStack(), false) ||
                                    candidate.equals(STEAM_HATCH.asStack())) {
                                cnt++;
                                color = 0xffff00ff;// 黄色
                                continue;
                            }
                            if (candidate.equals(MAINTENANCE_HATCH.asStack(), false)) {
                                cnt++;
                                color = 0x00ffffff;// 青色
                                continue;
                            }
                            if (candidate.equals(MUFFLER_HATCH[GTValues.LV].asStack(), false)) {
                                cnt++;
                                color = 0x800080ff;// 紫色
                                continue;
                            }
                            if (candidate.equals(PARALLEL_HATCH[GTValues.IV].asStack(), false)) {
                                cnt++;
                                color = 0xf0ffffff;// 蔚蓝色
                            }
                        }

                        if (cnt > 1) {
                            color = 0x3b2525ff;
                        }
                        colorCaches.put(pos, color);
                    }
                } else {
                    color = colorCaches.get(pos);
                }
                appendCube(builder, pos, color);

                vertexCount += 24;
            }

            BufferBuilder.RenderedBuffer rendered = builder.end();

            highlightVbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            highlightVbo.bind();
            highlightVbo.upload(rendered);
            VertexBuffer.unbind();

            // rendered.release();
        }

        private void appendCube(BufferBuilder b, BlockPos pos, int rgba) {
            float r = ((rgba >> 24) & 255) / 255f;
            float g = ((rgba >> 16) & 255) / 255f;
            float bl = ((rgba >> 8) & 255) / 255f;
            float a = (rgba & 255) / 255f * 0.6f;

            float x = pos.getX();
            float y = pos.getY();
            float z = pos.getZ();

            float x2 = x + 1;
            float y2 = y + 1;
            float z2 = z + 1;

            // front
            addQuad(b, x, y, z, x2, y, z, x2, y2, z, x, y2, z, r, g, bl, a);

            // back
            addQuad(b, x2, y, z2, x, y, z2, x, y2, z2, x2, y2, z2, r, g, bl, a);

            // left
            addQuad(b, x, y, z2, x, y, z, x, y2, z, x, y2, z2, r, g, bl, a);

            // right
            addQuad(b, x2, y, z, x2, y, z2, x2, y2, z2, x2, y2, z, r, g, bl, a);

            // top
            addQuad(b, x, y2, z, x2, y2, z, x2, y2, z2, x, y2, z2, r, g, bl, a);

            // bottom
            addQuad(b, x, y, z2, x2, y, z2, x2, y, z, x, y, z, r, g, bl, a);
        }

        private void addQuad(BufferBuilder b,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float r, float g, float bl, float a) {
            b.vertex(x1, y1, z1).color(r, g, bl, a).endVertex();
            b.vertex(x2, y2, z2).color(r, g, bl, a).endVertex();
            b.vertex(x3, y3, z3).color(r, g, bl, a).endVertex();
            b.vertex(x4, y4, z4).color(r, g, bl, a).endVertex();
        }

        private void renderHighlight() {
            if (!isHighLight) return;

            if (highlightDirty) {
                rebuildHighlightBuffer();
            }

            if (highlightVbo == null) return;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            RenderSystem.depthMask(false);
            GL11.glDepthRange(0.0, 0.01);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            highlightVbo.bind();

            highlightVbo.drawWithShader(
                    RenderSystem.getModelViewMatrix(),
                    RenderSystem.getProjectionMatrix(),
                    RenderSystem.getShader());

            VertexBuffer.unbind();

            GL11.glDepthRange(0.0, 1.0);
            RenderSystem.depthMask(true);

            RenderSystem.disableBlend();
        }

        @Override
        public void renderBlockOverLay(WorldSceneRenderer renderer) {
            PoseStack poseStack = new PoseStack();
            hoverPosFace = null;
            hoverItem = null;
            renderHighlight();
            if (isMouseOverElement(currentMouseX, currentMouseY)) {
                BlockHitResult hit = renderer.getLastTraceResult();
                if (hit != null) {
                    if (core.contains(hit.getBlockPos())) {
                        hoverPosFace = new BlockPosFace(hit.getBlockPos(), hit.getDirection());
                    } else if (!useOrtho) {
                        Vector3f hitPos = hit.getLocation().toVector3f();
                        Level world = renderer.world;
                        Vec3 eyePos = new Vec3(renderer.getEyePos());
                        hitPos.mul(2); // Double view range to ensure pos can be seen.
                        Vec3 endPos = new Vec3((hitPos.x - eyePos.x), (hitPos.y - eyePos.y), (hitPos.z - eyePos.z));
                        double min = Float.MAX_VALUE;
                        for (BlockPos pos : core) {
                            BlockState blockState = world.getBlockState(pos);
                            if (blockState.getBlock() == Blocks.AIR) {
                                continue;
                            }
                            hit = world.clipWithInteractionOverride(eyePos, endPos, pos,
                                    blockState.getShape(world, pos), blockState);
                            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                                double dist = eyePos.distanceToSqr(hit.getLocation());
                                if (dist < min) {
                                    min = dist;
                                    hoverPosFace = new BlockPosFace(hit.getBlockPos(), hit.getDirection());
                                }
                            }
                        }
                    }
                }
            }
            if (hoverPosFace != null) {
                var state = getDummyWorld().getBlockState(hoverPosFace.pos);
                hoverItem = state.getBlock().getCloneItemStack(getDummyWorld(), hoverPosFace.pos, state);
            }
            BlockPosFace tmp = dragging ? clickPosFace : hoverPosFace;
            if (selectedPosFace != null || tmp != null) {
                if (selectedPosFace != null && renderFacing) {
                    drawFacingBorder(poseStack, selectedPosFace, 0xff00ff00);
                }
                if (tmp != null && !tmp.equals(selectedPosFace) && renderFacing) {
                    drawFacingBorder(poseStack, tmp, 0xffffffff);
                }
            }
            if (selectedPosFace != null && renderSelect) {
                RenderUtils.renderBlockOverLay(poseStack, selectedPosFace.pos, 0.6f, 0, 0, 1.03f);
            }

            if (this.afterWorldRender != null) {
                this.afterWorldRender.accept(this);
            }
        }
    }

    private void updateHighLight() {
        isHighLight = !isHighLight;
        sceneWidget.markHighlightDirty();
    }

    private void updateLayer() {
        MBPattern pattern = patterns[index];
        if (layer + 1 >= -1 && layer + 1 <= pattern.maxY - pattern.minY) {
            layer += 1;
            if (pattern.controllerBase.isFormed()) {
                onFormedSwitch(false);
            }
        } else {
            layer = -1;
            if (!pattern.controllerBase.isFormed()) {
                onFormedSwitch(true);
            }
        }
        setupScene(pattern);
        sceneWidget.markHighlightDirty();
    }

    private void setupScene(MBPattern pattern) {
        Stream<BlockPos> stream = pattern.blockMap.keySet().stream()
                .filter(pos -> layer == -1 || layer + pattern.minY == pos.getY());
        if (pattern.controllerBase.isFormed()) {
            LongSet modelDisabled = pattern.controllerBase.getMultiblockState().getMatchContext().getOrDefault(
                    "renderMask",
                    LongSets.EMPTY_SET);
            if (!modelDisabled.isEmpty()) {
                stream = stream.filter(pos -> !modelDisabled.contains(pos.asLong()));
            }
        }
        sceneWidget.setRenderedCore(stream.toList(), null);
    }

    public static PatternPreviewWidget getPatternWidget(MultiblockMachineDefinition controllerDefinition) {
        if (LEVEL == null) {
            if (Minecraft.getInstance().level == null) {
                GTCEu.LOGGER.error("Try to init pattern previews before level load");
                throw new IllegalStateException();
            }
            LEVEL = new TrackedDummyWorld();
        }
        return new PatternPreviewWidget(controllerDefinition);
    }

    public void setPage(int index) {
        if (index >= patterns.length || index < 0) return;
        this.index = index;
        this.layer = -1;
        MBPattern pattern = patterns[index];
        setupScene(pattern);
        sceneWidget.markHighlightDirty();
        if (slotWidgets != null) {
            for (SlotWidget slotWidget : slotWidgets) {
                scrollableWidgetGroup.removeWidget(slotWidget);
            }
        }
        slotWidgets = new SlotWidget[pattern.parts.size()];
        CycleItemEntryHandler itemHandler = CycleItemEntryHandler.fromStacks(pattern.parts);
        int xOffset = 0;
        for (int i = 0; i < slotWidgets.length; i++) {
            int padding = 1;
            if (itemHandler.getStackInSlot(i).getCount() / 100_000 >= 1) {
                padding = 10;
            } else if (itemHandler.getStackInSlot(i).getCount() / 10_000 >= 1) {
                padding = 7;
            } else if (itemHandler.getStackInSlot(i).getCount() / 1_000 >= 1) {
                padding = 4;
            }

            slotWidgets[i] = new PatternPreviewSlotWidget(itemHandler, i, (4 + xOffset + padding), 0, false, false)
                    .setBackgroundTexture(ColorPattern.T_GRAY.rectTexture())
                    .setIngredientIO(IngredientIO.INPUT);
            xOffset += 18 + (2 * padding);
            scrollableWidgetGroup.addWidget(slotWidgets[i]);
        }
    }

    private void onFormedSwitch(boolean isFormed) {
        MBPattern pattern = patterns[index];
        IMultiController controllerBase = pattern.controllerBase;
        if (isFormed) {
            this.layer = -1;
            loadControllerFormed(pattern.blockMap.keySet(), controllerBase);
        } else {
            sceneWidget.setRenderedCore(pattern.blockMap.keySet(), null);
            controllerBase.onStructureInvalid();
        }
        sceneWidget.markHighlightDirty();
    }

    private void onPosSelected(BlockPos pos, Direction facing) {
        // sceneWidget.markHighlightDirty();
        if (index >= patterns.length || index < 0) return;
        TraceabilityPredicate predicate = patterns[index].predicateMap.get(pos);
        if (predicate != null) {
            predicates.clear();
            predicates.addAll(predicate.common);
            predicates.addAll(predicate.limited);
            predicates.removeIf(p -> p == null || p.candidates == null); // why it happens?
            if (candidates != null) {
                for (SlotWidget candidate : candidates) {
                    removeWidget(candidate);
                }
            }
            List<List<ItemStack>> candidateStacks = new ArrayList<>();
            List<List<Component>> predicateTips = new ArrayList<>();
            for (SimplePredicate simplePredicate : predicates) {
                List<ItemStack> itemStacks = simplePredicate.getCandidates();
                if (!itemStacks.isEmpty()) {
                    candidateStacks.add(itemStacks);
                    predicateTips.add(simplePredicate.getToolTips(predicate));
                }
            }
            candidates = new SlotWidget[candidateStacks.size()];
            CycleItemEntryHandler itemHandler = CycleItemEntryHandler.fromStacks(candidateStacks);
            int maxCol = (160 - (((slotWidgets.length - 1) / 9 + 1) * 18) - 35) % 18;
            for (int i = 0; i < candidateStacks.size(); i++) {
                int finalI = i;
                candidates[i] = new CandidateSlotWidget(itemHandler, i, 3 + (i / maxCol) * 18, 3 + (i % maxCol) * 18,
                        false,
                        false)
                        .setIngredientIO(IngredientIO.INPUT)
                        .setBackgroundTexture(new ColorRectTexture(0x4fffffff))
                        .setOnAddedTooltips((slot, list) -> list.addAll(predicateTips.get(finalI)));
                addWidget(candidates[i]);
            }
        }
    }

    /**
     * Finds the next section of the dummy preview level to place a multiblock at in a spiral pattern.
     * <p>
     * This results in positions that are considerably closer to the world origin than
     * the one it replaces, which did {@code prevPos.offset(500, 0, 500)},
     * which results in absurdly high offsets for the later multiblocks.
     * </p>
     * The regions being closer to {@code (0,0)} means that Z-fighting should be less likely,
     * since floating point inaccuracies won't be as large of a factor.
     *
     * @return the area to place the current multiblock at
     */
    public static BlockPos locateNextRegion() {
        int currentIndex = LAST_OFFSET_INDEX++;

        // Origin coordinates scaled back to the offset value, from global
        int x = 0, z = 0;
        if (currentIndex > 0) {
            int v = (int) (Mth.sqrt(currentIndex + 0.25f) - 0.5f);
            int nextV = v + 1;
            int spiralBaseIndex = v * nextV;
            // this is 1 or -1 depending on if v is odd or even
            int flipFlop = (v & 1) * 2 - 1;

            int offset = flipFlop * nextV / 2;
            x += offset;
            z += offset;

            int cornerIndex = spiralBaseIndex + nextV;
            if (currentIndex < cornerIndex) {
                x -= flipFlop * (currentIndex - spiralBaseIndex + 1);
            } else {
                x -= flipFlop * nextV;
                z -= flipFlop * (currentIndex - cornerIndex + 1);
            }
        }
        return new BlockPos(x * REGION_SIZE, 50, z * REGION_SIZE);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // I can only think of this way
        if (!isLoaded && GTCEu.Mods.isEMILoaded() && Minecraft.getInstance().screen instanceof RecipeScreen) {
            setPage(0);
            isLoaded = true;
        } else if (!isLoaded && GTCEu.Mods.isREILoaded() &&
                Minecraft.getInstance().screen instanceof AbstractDisplayViewingScreen) {
                    setPage(0);
                    isLoaded = true;
                }
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    private MBPattern initializePattern(MultiblockShapeInfo shapeInfo, HashSet<ItemStackKey> blockDrops) {
        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        IMultiController controllerBase = null;
        BlockPos multiPos = locateNextRegion();

        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    BlockState blockState = column[z].getBlockState();
                    BlockPos pos = multiPos.offset(x, y, z);
                    if (column[z].getBlockEntity(pos) instanceof IMachineBlockEntity holder &&
                            holder.getMetaMachine() instanceof IMultiController controller) {
                        holder.getSelf().setLevel(LEVEL);
                        controllerBase = controller;
                    }
                    blockMap.put(pos, BlockInfo.fromBlockState(blockState));
                }
            }
        }

        LEVEL.addBlocks(blockMap);
        if (controllerBase != null) {
            LEVEL.setInnerBlockEntity(controllerBase.self().holder.getSelf());
        }

        Map<ItemStackKey, PartInfo> parts = gatherBlockDrops(blockMap);
        blockDrops.addAll(parts.keySet());

        Map<BlockPos, TraceabilityPredicate> predicateMap = new HashMap<>();
        if (controllerBase != null) {
            loadControllerFormed(predicateMap.keySet(), controllerBase);
            predicateMap = controllerBase.getMultiblockState().getMatchContext().get("predicates");
        }
        return controllerBase == null ? null : new MBPattern(blockMap, parts.values().stream().sorted((one, two) -> {
            if (one.isController) return -1;
            if (two.isController) return +1;
            if (one.isTile && !two.isTile) return -1;
            if (two.isTile && !one.isTile) return +1;
            if (one.blockId != two.blockId) return two.blockId - one.blockId;
            return two.amount - one.amount;
        }).map(PartInfo::getItemStack).filter(list -> !list.isEmpty()).collect(Collectors.toList()), predicateMap,
                controllerBase);
    }

    private void loadControllerFormed(Collection<BlockPos> positions, IMultiController controllerBase) {
        BlockPattern pattern = controllerBase.getPattern();
        if (pattern != null && pattern.checkPatternAt(controllerBase.getMultiblockState(), true)) {
            controllerBase.onStructureFormed();
        }
        if (controllerBase.isFormed()) {
            LongSet modelDisabled = controllerBase.getMultiblockState().getMatchContext().getOrDefault("renderMask",
                    LongSets.EMPTY_SET);
            if (!modelDisabled.isEmpty()) {
                positions = new HashSet<>(positions);
                positions.removeIf(pos -> modelDisabled.contains(pos.asLong()));
            }
            sceneWidget.setRenderedCore(positions, null);
        } else {
            GTCEu.LOGGER.warn("Pattern formed checking failed: {}", controllerBase.self().getDefinition());
        }
    }

    private Map<ItemStackKey, PartInfo> gatherBlockDrops(Map<BlockPos, BlockInfo> blocks) {
        Map<ItemStackKey, PartInfo> partsMap = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<BlockPos, BlockInfo> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState blockState = PatternPreviewWidget.LEVEL.getBlockState(pos);
            ItemStack itemStack = blockState.getBlock().getCloneItemStack(PatternPreviewWidget.LEVEL, pos, blockState);

            if (itemStack.isEmpty() && !blockState.getFluidState().isEmpty()) {
                Fluid fluid = blockState.getFluidState().getType();
                itemStack = fluid.getBucket().getDefaultInstance();
            }

            ItemStackKey itemStackKey = new ItemStackKey(itemStack);
            partsMap.computeIfAbsent(itemStackKey, key -> new PartInfo(key, entry.getValue())).amount++;
        }
        return partsMap;
    }

    private static class PartInfo {

        final ItemStackKey itemStackKey;
        boolean isController = false;
        boolean isTile = false;
        final int blockId;
        int amount = 0;

        PartInfo(final ItemStackKey itemStackKey, final BlockInfo blockInfo) {
            this.itemStackKey = itemStackKey;
            this.blockId = Block.getId(blockInfo.getBlockState());
            this.isTile = blockInfo.hasBlockEntity();

            if (blockInfo.getBlockState().getBlock() instanceof MetaMachineBlock block) {
                if (block.definition instanceof MultiblockMachineDefinition)
                    this.isController = true;
            }
        }

        public List<ItemStack> getItemStack() {
            return Arrays.stream(itemStackKey.getItemStack())
                    .map(stack -> stack.copyWithCount(amount))
                    .filter(item -> !item.isEmpty())
                    .toList();
        }
    }

    public static class MBPattern {

        @NotNull
        final List<List<ItemStack>> parts;
        @NotNull
        final Map<BlockPos, TraceabilityPredicate> predicateMap;
        @NotNull
        final Map<BlockPos, BlockInfo> blockMap;
        @NotNull
        final IMultiController controllerBase;
        final int maxY, minY;

        public MBPattern(@NotNull Map<BlockPos, BlockInfo> blockMap, @NotNull List<List<ItemStack>> parts,
                         @NotNull Map<BlockPos, TraceabilityPredicate> predicateMap,
                         @NotNull IMultiController controllerBase) {
            this.parts = parts;
            this.blockMap = blockMap;
            this.predicateMap = predicateMap;
            this.controllerBase = controllerBase;
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (BlockPos pos : blockMap.keySet()) {
                min = Math.min(min, pos.getY());
                max = Math.max(max, pos.getY());
            }
            minY = min;
            maxY = max;
        }
    }
}

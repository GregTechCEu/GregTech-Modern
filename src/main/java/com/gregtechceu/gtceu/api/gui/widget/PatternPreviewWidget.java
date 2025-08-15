package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
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
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemStackHandler;

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
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.screen.RecipeScreen;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.shedaniel.rei.impl.client.gui.screen.AbstractDisplayViewingScreen;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class PatternPreviewWidget extends WidgetGroup {

    private boolean isLoaded;
    private static TrackedDummyWorld LEVEL;
    private static final int REGION_SIZE = 512;
    private static int LAST_OFFSET_INDEX = 0;
    private static final Map<MultiblockMachineDefinition, MBPattern[]> CACHE = new HashMap<>();
    private final SceneWidget sceneWidget;
    private final DraggableScrollableWidgetGroup scrollableWidgetGroup;
    public final MultiblockMachineDefinition controllerDefinition;
    public final MBPattern[] patterns;
    private final List<SimplePredicate> predicates;
    private int index;
    public int layer;
    private SlotWidget[] slotWidgets;
    private SlotWidget[] candidates;

    protected PatternPreviewWidget(MultiblockMachineDefinition controllerDefinition) {
        super(0, 0, 160, 160);
        setClientSideWidget();
        this.controllerDefinition = controllerDefinition;
        predicates = new ArrayList<>();
        layer = -1;

        addWidget(sceneWidget = new SceneWidget(3, 3, 150, 150, LEVEL) {

            @Override
            public void renderBlockOverLay(WorldSceneRenderer renderer) {
                PoseStack poseStack = new PoseStack();
                hoverPosFace = null;
                hoverItem = null;
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
                .setOnSelected(this::onPosSelected)
                .setRenderFacing(false)
                .setRenderFacing(false));

        scrollableWidgetGroup = new DraggableScrollableWidgetGroup(3, 132, 154, 22)
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

        addWidget(new ImageWidget(3, 3, 160, 10,
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

        addWidget(new ButtonWidget(138, 30, 18, 18, new GuiTextureGroup(
                ColorPattern.T_GRAY.rectTexture(),
                new TextTexture("1").setSupplier(() -> "P:" + index)),
                (x) -> setPage((index + 1 >= patterns.length) ? 0 : index + 1))
                .setHoverBorderTexture(1, -1));

        addWidget(new ButtonWidget(138, 50, 18, 18, new GuiTextureGroup(
                ColorPattern.T_GRAY.rectTexture(),
                new TextTexture("1").setSupplier(() -> layer >= 0 ? "L:" + layer : "ALL")),
                cd -> updateLayer())
                .setHoverBorderTexture(1, -1));

        setPage(0);
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
        if (slotWidgets != null) {
            for (SlotWidget slotWidget : slotWidgets) {
                scrollableWidgetGroup.removeWidget(slotWidget);
            }
        }
        slotWidgets = new SlotWidget[Math.min(pattern.parts.size(), 18)];
        var itemHandler = new CycleItemStackHandler(pattern.parts);
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
    }

    private void onPosSelected(BlockPos pos, Direction facing) {
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
            CycleItemStackHandler itemHandler = new CycleItemStackHandler(candidateStacks);
            int maxCol = (160 - (((slotWidgets.length - 1) / 9 + 1) * 18) - 35) % 18;
            for (int i = 0; i < candidateStacks.size(); i++) {
                int finalI = i;
                candidates[i] = new SlotWidget(itemHandler, i, 3 + (i / maxCol) * 18, 3 + (i % maxCol) * 18, false,
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

package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.MemoizedSupplier;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PipeModel {

    public static final ResourceLocation PIPE_BLOCKED_OVERLAY = GTCEu.id("block/pipe/blocked/pipe_blocked");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_UP = GTCEu.id("block/pipe/blocked/pipe_blocked_up");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_DOWN = GTCEu.id("block/pipe/blocked/pipe_blocked_down");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_LEFT = GTCEu.id("block/pipe/blocked/pipe_blocked_left");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_RIGHT = GTCEu.id("block/pipe/blocked/pipe_blocked_right");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_NU = GTCEu.id("block/pipe/blocked/pipe_blocked_nu");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_ND = GTCEu.id("block/pipe/blocked/pipe_blocked_nd");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_NL = GTCEu.id("block/pipe/blocked/pipe_blocked_nl");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_NR = GTCEu.id("block/pipe/blocked/pipe_blocked_nr");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_UD = GTCEu.id("block/pipe/blocked/pipe_blocked_ud");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_UL = GTCEu.id("block/pipe/blocked/pipe_blocked_ul");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_UR = GTCEu.id("block/pipe/blocked/pipe_blocked_ur");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_DL = GTCEu.id("block/pipe/blocked/pipe_blocked_dl");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_DR = GTCEu.id("block/pipe/blocked/pipe_blocked_dr");
    public static final ResourceLocation PIPE_BLOCKED_OVERLAY_LR = GTCEu.id("block/pipe/blocked/pipe_blocked_lr");
    private static final Int2ObjectMap<TextureAtlasSprite> RESTRICTOR_MAP = new Int2ObjectOpenHashMap<>();
    private static boolean isRestrictorInitialized;

    protected static void initializeRestrictor(Function<ResourceLocation, TextureAtlasSprite> atlas) {
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_UP), Border.TOP);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_DOWN), Border.BOTTOM);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_UD), Border.TOP, Border.BOTTOM);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_LEFT), Border.LEFT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_UL), Border.TOP, Border.LEFT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_DL), Border.BOTTOM, Border.LEFT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_NR), Border.TOP, Border.BOTTOM, Border.LEFT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_RIGHT), Border.RIGHT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_UR), Border.TOP, Border.RIGHT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_DR), Border.BOTTOM, Border.RIGHT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_NL), Border.TOP, Border.BOTTOM, Border.RIGHT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_LR), Border.LEFT, Border.RIGHT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_ND), Border.TOP, Border.LEFT, Border.RIGHT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY_NU), Border.BOTTOM, Border.LEFT, Border.RIGHT);
        addRestrictor(atlas.apply(PIPE_BLOCKED_OVERLAY), Border.TOP, Border.BOTTOM, Border.LEFT, Border.RIGHT);
    }

    private static final EnumMap<Direction, EnumMap<Border, Direction>> FACE_BORDER_MAP = Util.make(() -> {
        EnumMap<Direction, EnumMap<Border, Direction>> map = new EnumMap<>(Direction.class);

        map.put(Direction.DOWN, borderMap(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
        map.put(Direction.UP, borderMap(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
        map.put(Direction.NORTH, borderMap(Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST));
        map.put(Direction.SOUTH, borderMap(Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST));
        map.put(Direction.WEST, borderMap(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH));
        map.put(Direction.EAST, borderMap(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH));

        return map;
    });

    public final static int ITEM_CONNECTIONS = 0b001100;
    public final float thickness;
    public final AABB coreCube;
    public final Map<Direction, AABB> sideCubes;

    public MemoizedSupplier<ResourceLocation> sideTexture, endTexture;
    @Nullable
    public MemoizedSupplier<ResourceLocation> secondarySideTexture, secondaryEndTexture;
    @Setter
    public @Nullable ResourceLocation sideOverlayTexture, endOverlayTexture;

    @OnlyIn(Dist.CLIENT)
    private @Nullable TextureAtlasSprite sideSprite, endSprite;
    @OnlyIn(Dist.CLIENT)
    private @Nullable TextureAtlasSprite secondarySideSprite, secondaryEndSprite;
    @OnlyIn(Dist.CLIENT)
    private @Nullable TextureAtlasSprite sideOverlaySprite, endOverlaySprite;

    public PipeModel(float thickness, Supplier<ResourceLocation> sideTexture, Supplier<ResourceLocation> endTexture,
                     @Nullable Supplier<ResourceLocation> secondarySideTexture,
                     @Nullable Supplier<ResourceLocation> secondaryEndTexture) {
        this.sideTexture = GTMemoizer.memoize(sideTexture);
        this.endTexture = GTMemoizer.memoize(endTexture);
        this.secondarySideTexture = secondarySideTexture != null ? GTMemoizer.memoize(secondarySideTexture) :
                null;
        this.secondaryEndTexture = secondaryEndTexture != null ? GTMemoizer.memoize(secondaryEndTexture) : null;
        this.thickness = thickness;
        double min = (1d - thickness) / 2;
        double max = min + thickness;
        this.coreCube = new AABB(min, min, min, max, max, max);
        this.sideCubes = new EnumMap<>(Direction.class);
        for (Direction side : GTUtil.DIRECTIONS) {
            var normal = side.getNormal();
            sideCubes.put(side, new AABB(
                    normal.getX() == 0 ? min : normal.getX() > 0 ? max : 0,
                    normal.getY() == 0 ? min : normal.getY() > 0 ? max : 0,
                    normal.getZ() == 0 ? min : normal.getZ() > 0 ? max : 0,
                    normal.getX() == 0 ? max : normal.getX() > 0 ? 1 : min,
                    normal.getY() == 0 ? max : normal.getY() > 0 ? 1 : min,
                    normal.getZ() == 0 ? max : normal.getZ() > 0 ? 1 : min));
        }

        if (!isRestrictorInitialized) {
            ModelUtils.registerAtlasStitchedEventListener(false, InventoryMenu.BLOCK_ATLAS, event -> {
                initializeRestrictor(event.getAtlas()::getSprite);
            });

            isRestrictorInitialized = true;
        }
        ModelUtils.registerAtlasStitchedEventListener(false, InventoryMenu.BLOCK_ATLAS, event -> {
            TextureAtlas atlas = event.getAtlas();

            sideSprite = atlas.getSprite(sideTexture.get());
            endSprite = atlas.getSprite(endTexture.get());
            if (secondarySideTexture != null) {
                secondarySideSprite = atlas.getSprite(secondarySideTexture.get());
            }
            if (secondaryEndTexture != null) {
                secondaryEndSprite = atlas.getSprite(secondaryEndTexture.get());
            }
            if (sideOverlayTexture != null) {
                sideOverlaySprite = atlas.getSprite(sideOverlayTexture);
            }
            if (endOverlayTexture != null) {
                endOverlaySprite = atlas.getSprite(endOverlayTexture);
            }
        });
    }

    public VoxelShape getShapes(int connections) {
        var shapes = new ArrayList<VoxelShape>(7);
        shapes.add(Shapes.create(coreCube));
        for (Direction side : GTUtil.DIRECTIONS) {
            if (PipeBlockEntity.isConnected(connections, side)) {
                shapes.add(Shapes.create(sideCubes.get(side)));
            }
        }
        return shapes.stream().reduce(Shapes.empty(), Shapes::or);
    }

    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> bakeQuads(@Nullable Direction side, int connections, int blockedConnections) {
        if (side != null) {
            if (thickness == 1) { // full block
                List<BakedQuad> quads = new LinkedList<>();
                quads.add(FaceQuad.builder(side, sideSprite).cube(coreCube).cubeUV().tintIndex(0).bake());
                if (secondarySideSprite != null) {
                    quads.add(FaceQuad.builder(side, secondarySideSprite).cube(coreCube).cubeUV().tintIndex(0).bake());
                }
                return quads;
            }

            if (PipeBlockEntity.isConnected(connections, side)) { // side connected
                List<BakedQuad> quads = new LinkedList<>();
                quads.add(FaceQuad.builder(side, endSprite).cube(sideCubes.get(side).inflate(-0.001)).cubeUV()
                        .tintIndex(1).bake());
                if (secondaryEndSprite != null) {
                    quads.add(FaceQuad.builder(side, secondaryEndSprite).cube(sideCubes.get(side)).cubeUV().tintIndex(1)
                            .bake());
                }
                if (endOverlaySprite != null) {
                    quads.add(FaceQuad.builder(side, endOverlaySprite).cube(sideCubes.get(side)).cubeUV().tintIndex(0)
                            .bake());
                }
                if (sideOverlaySprite != null) {
                    for (Direction face : GTUtil.DIRECTIONS) {
                        if (face.getAxis() != side.getAxis()) {
                            quads.add(FaceQuad.builder(face, sideOverlaySprite).cube(sideCubes.get(side)).cubeUV()
                                    .tintIndex(2).bake());
                        }
                    }
                }
                int borderMask = computeBorderMask(blockedConnections, connections, side);
                if (borderMask != 0) {
                    quads.add(FaceQuad.builder(side, RESTRICTOR_MAP.get(borderMask)).cube(sideCubes.get(side)).cubeUV()
                            .bake());
                }
                return quads;
            }

            return Collections.emptyList();
        }

        List<BakedQuad> quads = new LinkedList<>();
        if (thickness < 1) { // non full block
            // render core cube
            for (Direction face : GTUtil.DIRECTIONS) {
                if (!PipeBlockEntity.isConnected(connections, face)) {
                    quads.add(FaceQuad.builder(face, sideSprite).cube(coreCube).cubeUV().tintIndex(0).bake());
                    if (secondarySideSprite != null) {
                        quads.add(FaceQuad.builder(face, secondarySideSprite).cube(coreCube).cubeUV().tintIndex(0)
                                .bake());
                    }
                }
                // render each connected side
                for (Direction facing : GTUtil.DIRECTIONS) {
                    if (facing.getAxis() != face.getAxis()) {
                        if (PipeBlockEntity.isConnected(connections, facing)) {
                            quads.add(FaceQuad.builder(face, sideSprite).cube(sideCubes.get(facing)).cubeUV()
                                    .tintIndex(0).bake());
                            if (secondarySideSprite != null) {
                                quads.add(FaceQuad.builder(face, secondarySideSprite).cube(sideCubes.get(facing))
                                        .cubeUV().tintIndex(0).bake());
                            }
                            if (sideOverlaySprite != null) {
                                quads.add(FaceQuad.builder(face, sideOverlaySprite)
                                        .cube(sideCubes.get(facing).inflate(0.001)).cubeUV().tintIndex(2).bake());
                            }
                            int borderMask = computeBorderMask(blockedConnections, connections, face);
                            if (borderMask != 0) {
                                quads.add(FaceQuad.builder(face, RESTRICTOR_MAP.get(borderMask))
                                        .cube(coreCube).cubeUV().bake());
                                quads.add(FaceQuad.builder(face, RESTRICTOR_MAP.get(borderMask))
                                        .cube(sideCubes.get(facing)).cubeUV().bake());
                            }
                        }
                    }
                }
            }
        }
        return quads;
    }

    @SuppressWarnings("DataFlowIssue")
    @OnlyIn(Dist.CLIENT)
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sideSprite;
    }

    private final Map<Optional<Direction>, List<BakedQuad>> itemModelCache = new ConcurrentHashMap<>();

    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack matrixStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer,
                combinedLight, combinedOverlay,
                (ItemBakedModel) (state, direction, random) -> itemModelCache.computeIfAbsent(
                        Optional.ofNullable(direction),
                        direction1 -> bakeQuads(direction1.orElse(null), ITEM_CONNECTIONS, 0)));
        IItemRendererProvider.disabled.set(false);
    }

    @OnlyIn(Dist.CLIENT)
    public void registerTextureAtlas(Consumer<ResourceLocation> register) {
        itemModelCache.clear();
        sideTexture.invalidate();
        register.accept(sideTexture.get());
        endTexture.invalidate();
        register.accept(endTexture.get());
        if (secondarySideTexture != null) {
            secondarySideTexture.invalidate();
            if (secondarySideTexture.get() != GTModels.BLANK_TEXTURE) {
                register.accept(secondarySideTexture.get());
            }
        }
        if (secondaryEndTexture != null) {
            secondaryEndTexture.invalidate();
            if (secondaryEndTexture.get() != GTModels.BLANK_TEXTURE) {
                register.accept(secondaryEndTexture.get());
            }
        }
        if (sideOverlayTexture != null) register.accept(sideOverlayTexture);
        if (endOverlayTexture != null) register.accept(endOverlayTexture);
        sideSprite = null;
        endSprite = null;
        endOverlaySprite = null;
    }

    private static EnumMap<Border, Direction> borderMap(Direction topSide, Direction bottomSide, Direction leftSide,
                                                        Direction rightSide) {
        EnumMap<Border, Direction> sideMap = new EnumMap<>(Border.class);
        sideMap.put(Border.TOP, topSide);
        sideMap.put(Border.BOTTOM, bottomSide);
        sideMap.put(Border.LEFT, leftSide);
        sideMap.put(Border.RIGHT, rightSide);
        return sideMap;
    }

    private static void addRestrictor(TextureAtlasSprite sprite, Border... borders) {
        int mask = 0;
        for (Border border : borders) {
            mask |= border.mask;
        }
        RESTRICTOR_MAP.put(mask, sprite);
    }

    protected static Direction getSideAtBorder(Direction side, Border border) {
        return FACE_BORDER_MAP.get(side).get(border);
    }

    protected static int computeBorderMask(int blockedConnections, int connections, Direction side) {
        int borderMask = 0;
        if (blockedConnections != 0) {
            for (Border border : Border.VALUES) {
                Direction borderSide = getSideAtBorder(side, border);
                if (PipeBlockEntity.isFaceBlocked(blockedConnections, borderSide) &&
                        PipeBlockEntity.isConnected(connections, borderSide)) {
                    // only render when the side is blocked *and* connected
                    borderMask |= border.mask;
                }
            }
        }
        return borderMask;
    }

    public enum Border {

        TOP,
        BOTTOM,
        LEFT,
        RIGHT;

        public static final Border[] VALUES = values();

        public final int mask;

        Border() {
            mask = 1 << this.ordinal();
        }
    }
}

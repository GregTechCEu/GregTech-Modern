package com.gregtechceu.gtceu.client.model;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote PipeModel
 */
public class PipeModel {
    public final static int ITEM_CONNECTIONS = 0b1100;
    public final float thickness;
    public final AABB coreCube;
    public final Map<Direction, AABB> sideCubes;

    public Supplier<ResourceLocation> sideTexture, endTexture;
    @Setter
    public ResourceLocation sideOverlayTexture, endOverlayTexture;

    @Environment(EnvType.CLIENT)
    TextureAtlasSprite sideSprite, endSprite, sideOverlaySprite, endOverlaySprite;

    public PipeModel(float thickness, Supplier<ResourceLocation> sideTexture, Supplier<ResourceLocation> endTexture) {
        this.sideTexture = sideTexture;
        this.endTexture = endTexture;
        this.thickness = thickness;
        double min = (1d - thickness) / 2;
        double max = min + thickness;
        this.coreCube = new AABB(min, min, min, max, max, max);
        this.sideCubes = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            var normal = side.getNormal();
            sideCubes.put(side, new AABB(
                    normal.getX() == 0 ? min : normal.getX() > 0 ? max : 0,
                    normal.getY() == 0 ? min : normal.getY() > 0 ? max : 0,
                    normal.getZ() == 0 ? min : normal.getZ() > 0 ? max : 0,
                    normal.getX() == 0 ? max : normal.getX() > 0 ? 1 : min,
                    normal.getY() == 0 ? max : normal.getY() > 0 ? 1 : min,
                    normal.getZ() == 0 ? max : normal.getZ() > 0 ? 1 : min));
        }
    }

    public VoxelShape getShapes(int connections) {
        var shapes = new ArrayList<VoxelShape>(7);
        shapes.add(Shapes.create(coreCube));
        for (Direction side : Direction.values()) {
            if (isConnected(connections, side)) {
                shapes.add(Shapes.create(sideCubes.get(side)));
            }
        }
        return shapes.stream().reduce(Shapes.empty(), Shapes::or);
    }

    public boolean isConnected(int connections, Direction side) {
        return (connections >> side.ordinal() & 1) == 1;
    }

    @Environment(EnvType.CLIENT)
    public List<BakedQuad> bakeQuads(@Nullable Direction side, int connections) {
        if (sideSprite == null) {
            sideSprite = ModelFactory.getBlockSprite(sideTexture.get());
        }
        if (endSprite == null) {
            endSprite = ModelFactory.getBlockSprite(endTexture.get());
        }
        if (sideOverlayTexture != null && sideOverlaySprite == null) {
            sideOverlaySprite = ModelFactory.getBlockSprite(sideOverlayTexture);
        }
        if (endOverlayTexture != null && endOverlaySprite == null) {
            endOverlaySprite = ModelFactory.getBlockSprite(endOverlayTexture);
        }

        if (side != null) {
            if (thickness == 1) { // full block
                return List.of(FaceQuad.builder(side, sideSprite).cube(coreCube).cubeUV().tintIndex(0).bake());
            }

            if (isConnected(connections, side)) { // side connected
                List<BakedQuad> quads = new ArrayList<>();
                quads.add(FaceQuad.builder(side, endSprite).cube(sideCubes.get(side).inflate(-0.001)).cubeUV().tintIndex(1).bake());
                if (endOverlaySprite != null) {
                    quads.add(FaceQuad.builder(side, endOverlaySprite).cube(sideCubes.get(side).inflate(-0.000)).cubeUV().tintIndex(0).bake());
                }
                if (sideOverlaySprite != null) {
                    for (Direction face : Direction.values()) {
                        if (face != side && face != side.getOpposite()) {
                            quads.add(FaceQuad.builder(face, sideOverlaySprite).cube(sideCubes.get(side).inflate(-0.000)).cubeUV().tintIndex(2).bake());
                        }
                    }
                }
                return quads;
            }

            return Collections.emptyList();
        }

        List<BakedQuad> quads = new LinkedList<>();
        if (thickness < 1) { // non full block
            // render core cube
            for (Direction face : Direction.values()) {
                if (!isConnected(connections, face)) {
                    quads.add(FaceQuad.builder(face, sideSprite).cube(coreCube).cubeUV().tintIndex(0).bake());
                }
                // render each connected side
                for (Direction facing : Direction.values()) {
                    if (facing.getAxis() != face.getAxis()) {
                        if (isConnected(connections, facing)) {
                            quads.add(FaceQuad.builder(face, sideSprite).cube(sideCubes.get(facing)).cubeUV().tintIndex(0).bake());
                            //if (endOverlaySprite != null) {
                            //    quads.add(FaceQuad.builder(face, endOverlaySprite).cube(sideCubes.get(facing).inflate(0.01)).cubeUV().tintIndex(0).bake());
                            //}
                            if (sideOverlaySprite != null) {
                                quads.add(FaceQuad.builder(face, sideOverlaySprite).cube(sideCubes.get(facing).inflate(0.001)).cubeUV().tintIndex(2).bake());
                            }
                        }
                    }
                }
            }
        }
        return quads;
    }

    @NotNull
    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        if (sideSprite == null) {
            sideSprite = ModelFactory.getBlockSprite(sideTexture.get());
        }
        return sideSprite;
    }

    @Environment(EnvType.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay,
                (ItemBakedModel) (state, direction, random) -> bakeQuads(direction, ITEM_CONNECTIONS));
        IItemRendererProvider.disabled.set(false);
    }

    @Environment(EnvType.CLIENT)
    public void registerTextureAtlas(Consumer<ResourceLocation> register) {
        register.accept(sideTexture.get());
        register.accept(endTexture.get());
        if (sideOverlayTexture != null) register.accept(sideOverlayTexture);
        if (endOverlayTexture != null) register.accept(endOverlayTexture);
        sideSprite = null;
        endSprite = null;
        endOverlaySprite = null;
    }
}

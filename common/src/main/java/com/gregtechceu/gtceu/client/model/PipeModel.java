package com.gregtechceu.gtceu.client.model;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

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

    public ResourceLocation coreTexture;

    @Environment(EnvType.CLIENT)
    TextureAtlasSprite coreSprite;

    public PipeModel(float thickness, ResourceLocation coreTexture) {
        this.coreTexture = coreTexture;
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
        if (coreSprite == null) {
            coreSprite = ModelFactory.getBlockSprite(coreTexture);
        }

        if (side != null) {
            if (thickness == 1) { // full block
                return List.of(FaceQuad.builder(side, coreSprite).cube(coreCube).cubeUV().tintIndex(0).bake());
            }

            if (isConnected(connections, side)) { // side connected
                return List.of(FaceQuad.builder(side, coreSprite).cube(sideCubes.get(side).inflate(-0.001)).cubeUV().tintIndex(0).bake());
            }

            return Collections.emptyList();
        }

        List<BakedQuad> quads = new LinkedList<>();
        if (thickness < 1) { // non full block
            // render core cube
            for (Direction face : Direction.values()) {
                if (!isConnected(connections, face)) {
                    quads.add(FaceQuad.builder(face, coreSprite).cube(coreCube).cubeUV().tintIndex(0).bake());
                }
                // render each connected side
                for (Direction facing : Direction.values()) {
                    if (facing.getAxis() != face.getAxis()) {
                        if (isConnected(connections, facing)) {
                            quads.add(FaceQuad.builder(face, coreSprite).cube(sideCubes.get(facing)).cubeUV().tintIndex(0).bake());
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
        if (coreSprite == null) {
            coreSprite = ModelFactory.getBlockSprite(coreTexture);
        }
        return coreSprite;
    }

    @Environment(EnvType.CLIENT)
    public void renderItem(ItemStack stack, ItemTransforms.TransformType transformType, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay,
                (ItemBakedModel) (state, direction, random) -> bakeQuads(direction, ITEM_CONNECTIONS));
        IItemRendererProvider.disabled.set(false);
    }

    @Environment(EnvType.CLIENT)
    public void registerTextureAtlas(Consumer<ResourceLocation> register) {
        register.accept(coreTexture);
        coreSprite = null;
    }
}

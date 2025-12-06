package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.data.RuntimeExistingFileHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PipeModel {

    private static final Set<PipeModel> MODELS = new HashSet<>();

    public static PipeModel create(PipeBlock<?, ?, ?> block, float thickness,
                                   ResourceLocation sideTexture, ResourceLocation endTexture) {
        PipeModel model = new PipeModel(block, thickness, sideTexture, endTexture);
        MODELS.add(model);
        return model;
    }

    public static void initModels() {
        MODELS.clear();
        // regenerate all pipe models in case their textures changed
        // cables may do this, others too if something's removed
        for (var block : GTBlocks.LASER_PIPES) block.get().createPipeModel();
        for (var block : GTBlocks.OPTICAL_PIPES) block.get().createPipeModel();
        for (var block : GTBlocks.DUCT_PIPES) block.get().createPipeModel();
        for (var block : GTMaterialBlocks.CABLE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel();
        }
        for (var block : GTMaterialBlocks.FLUID_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel();
        }
        for (var block : GTMaterialBlocks.ITEM_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel();
        }

        for (PipeModel generator : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(generator.block);
            GTDynamicResourcePack.addModel(generator.centerModel.getLocation(), generator.centerModel.toJson());
            GTDynamicResourcePack.addModel(generator.connectionModel.getLocation(), generator.connectionModel.toJson());

            // spotless:off
            // make the "default" model be based on the center part's model
            BlockModelBuilder defaultModel = new BlockModelBuilder(blockId, RuntimeExistingFileHelper.INSTANCE)
                    .parent(generator.centerModel)
                    .customLoader(PipeModelBuilder::begin)
                        .connectionModels()
                            .modelFile(generator.connectionModel)
                        .addModel()
                        .centerModel()
                            .modelFile(generator.centerModel)
                        .addModel()
                    .end()
                    .parent(new ModelFile.UncheckedModelFile("block/block"));

            GTDynamicResourcePack.addBlockModel(blockId, defaultModel.toJson());
            GTDynamicResourcePack.addBlockState(blockId,
                    BlockModelGenerators.createSimpleBlock(generator.block, blockId.withPrefix("block/")));

            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(generator.block.asItem()),
                    new DelegatedModel(ModelLocationUtils.getModelLocation(generator.block)));
            // spotless:on
        }
    }

    private final PipeBlock<?, ?, ?> block;
    @Setter
    public ResourceLocation side, end;
    @Setter
    public @Nullable ResourceLocation sideSecondary, endSecondary;
    @Setter
    public @Nullable ResourceLocation sideOverlay, endOverlay;

    private final BlockModelBuilder centerModel;
    private final BlockModelBuilder connectionModel;

    protected PipeModel(PipeBlock<?, ?, ?> block, float thickness,
                        ResourceLocation side, ResourceLocation end) {
        this.block = block;
        this.side = side;
        this.end = end;

        thickness *= 16.0f;
        float min = (16.0f - thickness) / 2.0f;
        float max = min + thickness;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(this.block);
        ResourceLocation baseModelId = blockId.withPath(path -> "block/pipe/" + path + "/");

        centerModel = makePartModel(baseModelId.withSuffix("center"), null, min, min, min, max, max, max);
        connectionModel = makePartModel(baseModelId.withSuffix("connection"), Direction.DOWN, min, 0, min, max, min,
                max);
    }

    /**
     * Fills out a model builder with applicable pipe model elements and returns it for further use
     *
     * @param name the resulting model's path
     * @param side the model face that's being created
     * @param x1   min X coordinate in the range [-16,32]
     * @param y1   min Y coordinate in the range [-16,32]
     * @param z1   min Z coordinate in the range [-16,32]
     * @param x2   max X coordinate in the range [-16,32]
     * @param y2   max Y coordinate in the range [-16,32]
     * @param z2   max Z coordinate in the range [-16,32]
     * @implNote The coordinates must be in the correct order or the resulting model's cubes will be inside out!
     */
    private BlockModelBuilder makePartModel(ResourceLocation name, @Nullable Direction side,
                                            final float x1, final float y1, final float z1,
                                            final float x2, final float y2, final float z2) {
        Reference2FloatMap<Direction> maxDistances = new Reference2FloatOpenHashMap<>();
        maxDistances.defaultReturnValue(GTMath.max(x1, y1, z1, x2, y2, z2));
        for (Direction dir : GTUtil.DIRECTIONS) {
            maxDistances.put(dir, switch (dir) {
                case DOWN -> Math.min(y1, y2);
                case UP -> Math.max(y1, y2);
                case NORTH -> Math.min(z1, z2);
                case SOUTH -> Math.max(z1, z2);
                case WEST -> Math.min(x1, x2);
                case EAST -> Math.max(x1, x2);
            });
        }

        BlockModelBuilder model = new BlockModelBuilder(name, RuntimeExistingFileHelper.INSTANCE)
                .parent(new ModelFile.UncheckedModelFile("block/block"));
        makePartModelElement(model, side, false, maxDistances, 0.0f, 0, 1,
                x1, y1, z1, x2, y2, z2, this.side, this.end, "side", "end");
        makePartModelElement(model, side, true, maxDistances, 0.001f, 0, 1,
                x1, y1, z1, x2, y2, z2, this.sideSecondary, this.endSecondary, "side_secondary", "end_secondary");
        makePartModelElement(model, side, true, maxDistances, 0.002f, 2, 2,
                x1, y1, z1, x2, y2, z2, this.sideOverlay, this.endOverlay, "side_overlay", "end_overlay");
        return model;
    }

    private void makePartModelElement(BlockModelBuilder model, @Nullable Direction side, boolean useEndWithFullCube,
                                      Reference2FloatMap<Direction> maxDistances,
                                      float offset, int sideTintIndex, int endTintIndex,
                                      final float x1, final float y1, final float z1,
                                      final float x2, final float y2, final float z2,
                                      @Nullable ResourceLocation sideTexture, @Nullable ResourceLocation endTexture,
                                      String sideKey, String endKey) {
        if (sideTexture == null && endTexture == null) {
            return;
        }
        if (sideTexture != null) model.texture(sideKey, sideTexture);
        if (endTexture != null) model.texture(endKey, endTexture);

        boolean fullCube = !useEndWithFullCube &&
                (x1 == y1 && x1 == z1 && x1 <= 0.0f) &&
                (x2 == y2 && x2 == z2 && x2 >= 16.0f);

        var element = model.element()
                .from(x1 - offset, y1 - offset, z1 - offset)
                .to(x2 + offset, y2 + offset, z2 + offset);

        for (Direction dir : GTUtil.DIRECTIONS) {
            BlockModelBuilder.ElementBuilder.FaceBuilder face = null;
            boolean isEnd = (side == dir || side == dir.getOpposite()) && !fullCube;
            if (isEnd && endTexture != null && side != dir.getOpposite()) {
                face = element.face(dir).texture("#" + endKey).tintindex(endTintIndex);
            } else if (!isEnd && sideTexture != null) {
                face = element.face(dir).texture("#" + sideKey).tintindex(sideTintIndex);
            }

            if (face != null && maxDistances.getFloat(dir) >= 16.0f) {
                face.cullface(dir);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PipeModel pipeModel = (PipeModel) o;
        return block == pipeModel.block &&
                Objects.equals(side, pipeModel.side) &&
                Objects.equals(end, pipeModel.end) &&
                Objects.equals(sideSecondary, pipeModel.sideSecondary) &&
                Objects.equals(endSecondary, pipeModel.endSecondary) &&
                Objects.equals(sideOverlay, pipeModel.sideOverlay) &&
                Objects.equals(endOverlay, pipeModel.endOverlay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, side, end, sideSecondary, endSecondary, sideOverlay, endOverlay);
    }
}

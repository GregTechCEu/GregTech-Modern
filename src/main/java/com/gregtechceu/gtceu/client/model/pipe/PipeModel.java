package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.registry.registrate.GTBlockBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.*;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This is an automatic pipe model generator.
 *
 * <h2>For material pipes</h2>
 * If the pipe this model belongs to is generated from a material property (or equivalent),
 * you should call {@link #dynamicModel()}, which adds the model to {@link #DYNAMIC_MODELS}
 * and automatically processes it as a part of runtime asset generation.
 * <p>
 * <strong style="font-size:17">NOTE:</strong><br>
 * You must also initialize the models in an {@link RegisterDynamicResourcesEvent} listener as such:
 * 
 * <pre>
 * {@code
 * 
 * // in a @EventBusSubscriber-annotated class
 * @SubscribeEvent
 * public static void registerDynamicAssets(RegisterDynamicResourcesEvent event) {
 *     for (var block : YourBlocks.YOUR_PIPE_BLOCKS.values()) {
 *         if (block == null) continue;
 *         block.get().createPipeModel(RuntimeExistingFileHelper.INSTANCE).dynamicModel();
 *     }
 * }
 * }
 * </pre>
 * 
 * Remember to replace {@code YourBlocks.YOUR_PIPE_BLOCKS.values()} with a reference to your pipe block collection!
 * </p>
 *
 * <h2>For non-material pipes</h2>
 * Conversely, if the pipe is <strong>not</strong> generated, but has a constant set of variants (such as optical fiber
 * cables),
 * you should <strong>NOT</strong> use {@link #dynamicModel()} and instead set the model with
 * {@link GTBlockBuilder#gtBlockstate(NonNullBiConsumer)} as such:
 * 
 * <pre>
 * {@code
 *     // on your pipe block builder
 *     ... = REGISTRATE.block(...)
 *              .properties(...)
 *              .gtBlockstate(GTModels::createPipeBlockModel)
 *              ...more builder things...
 *              .item(...)
 *              .model(NonNullBiConsumer.noop())
 *              ...more builder things...
 * }
 * </pre>
 * 
 * This makes the pipe model(s) be generated for you without having to process them at runtime.
 *
 */
public class PipeModel {

    // spotless:off
    public static final String
            SIDE_KEY           = "side",
            END_KEY            = "end",
            SIDE_SECONDARY_KEY = "side_secondary",
            END_SECONDARY_KEY  = "end_secondary",
            SIDE_OVERLAY_KEY   = "side_overlay",
            END_OVERLAY_KEY    = "end_overlay";
    // spotless:on

    public static final Set<PipeModel> DYNAMIC_MODELS = new HashSet<>();

    public static void initDynamicModels() {
        for (PipeModel generator : DYNAMIC_MODELS) {
            generator.initModels();
        }
    }

    @Getter
    private final PipeBlock<?, ?, ?> block;
    public final @NotNull ResourceLocation blockId;
    protected final GTBlockstateProvider provider;

    /**
     * The pipe's "thickness" in the (0,16] voxel range, where 1 is 1 voxel and 16 is a full block thick
     */
    protected final float thickness;
    /**
     * The pipe model's 'minimum' coordinate in the (0,16] voxel range.<br>
     * This is ex. the height of the center part's bottom edge.
     */
    protected final float minCoord;
    /**
     * The pipe model's 'maximum' coordinate in the (0,16] voxel range.<br>
     * This is ex. the height of the center part's top edge.
     */
    protected final float maxCoord;
    @Setter
    public ResourceLocation side, end;
    @Setter
    public @Nullable ResourceLocation sideSecondary, endSecondary;
    @Setter
    public @Nullable ResourceLocation sideOverlay, endOverlay;

    /// Use {@link #getOrCreateBlockModel()} instead of referencing this field directly.
    private BlockModelBuilder blockModel;
    /// Use {@link #getOrCreateItemModel()} instead of referencing this field directly.
    private ItemModelBuilder itemModel;

    /// Use {@link #getOrCreateCenterElement()} instead of referencing this field directly.
    private BlockModelBuilder centerElement;
    /// Use {@link #getOrCreateConnectionElement()} instead of referencing this field directly.
    private BlockModelBuilder connectionElement;

    public PipeModel(PipeBlock<?, ?, ?> block, GTBlockstateProvider provider,
                     float thickness, ResourceLocation side, ResourceLocation end) {
        this.block = block;
        this.blockId = BuiltInRegistries.BLOCK.getKey(this.block);
        this.provider = provider;

        // assume thickness is in the 0-1 range
        this.thickness = thickness * 16.0f;
        this.side = side;
        this.end = end;

        this.minCoord = (16.0f - this.thickness) / 2.0f;
        this.maxCoord = this.minCoord + this.thickness;
    }

    public final void dynamicModel() {
        DYNAMIC_MODELS.add(this);
    }

    /**
     * Initialize all models that are required for this block model to exist.<br>
     * <i>Order is important!</i> Dependent models must be initialized <strong>after</strong> their dependencies.
     *
     * @see #getOrCreateBlockModel()
     * @see #getOrCreateCenterElement()
     * @see #getOrCreateConnectionElement()
     */
    @MustBeInvokedByOverriders
    public void initModels() {
        getOrCreateCenterElement();
        getOrCreateConnectionElement();
        getOrCreateBlockModel();
        createBlockState();

        getOrCreateItemModel();
    }

    /**
     * Override this to change the actual model {@link #block this.block} will use.
     * 
     * @return A model builder for the block's actual model.
     * @see #getOrCreateCenterElement()
     * @see #getOrCreateConnectionElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder getOrCreateBlockModel() {
        if (this.blockModel != null) {
            return this.blockModel;
        }
        // spotless:off
        return this.blockModel = this.provider.models().getBuilder(this.blockId.toString())
                // make the "default" model be based on the center part's model
                .parent(this.getOrCreateCenterElement())
                .customLoader(PipeModelBuilder.begin(this.thickness, this.provider))
                    .centerModels(this.getOrCreateCenterElement().getLocation())
                    .connectionModels(this.getOrCreateConnectionElement().getLocation())
                .end();
        // spotless:on
    }

    /**
     * Override this to change the center element's model.
     * 
     * @return A model builder for the center element's model.
     * @see #getOrCreateBlockModel()
     * @see #getOrCreateConnectionElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder getOrCreateCenterElement() {
        if (this.centerElement != null) {
            return this.centerElement;
        }
        return this.centerElement = makeElementModel(this.blockId.withPath(path -> "block/pipe/" + path + "/center"),
                null, minCoord, minCoord, minCoord, maxCoord, maxCoord, maxCoord);
    }

    /**
     * Override this to change the 'connection' element's model.<br>
     * By default, this is rotated & used for all connected sides of the pipe.<br>
     * Note that that is not a hard requirement, and that you may set a model per side in
     * {@link #getOrCreateBlockModel()}.
     * 
     * @return A model builder for the connection element's model.
     * @see #getOrCreateBlockModel()
     * @see #getOrCreateCenterElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder getOrCreateConnectionElement() {
        if (this.connectionElement != null) {
            return this.connectionElement;
        }
        return this.connectionElement = makeElementModel(
                this.blockId.withPath(path -> "block/pipe/" + path + "/connection"),
                Direction.DOWN, minCoord, 0, minCoord, maxCoord, minCoord, maxCoord);
    }

    /**
     * Override this to change the item model.<br>
     * By default, this creates a version of the pipe block model with the north & south sides 'connected'.
     * 
     * @return The item model builder.
     * @see #getOrCreateBlockModel()
     */
    @ApiStatus.OverrideOnly
    protected ItemModelBuilder getOrCreateItemModel() {
        if (this.itemModel != null) {
            return this.itemModel;
        }
        return this.itemModel = createItemModel(this.blockId, this.minCoord, this.maxCoord);
    }

    /**
     * Override this to change the block state set {@link #block this.block} will use.<br>
     * By default, this creates a simple block state with no properties.<br>
     * The activable pipes (laser & optical) use this to add a model for the
     * {@link GTBlockStateProperties#ACTIVE "active"} state of the blocks.
     * 
     * @return The block state generator, usually a {@link MultiVariantGenerator}.
     * @see #getOrCreateBlockModel()
     * @see ActivablePipeModel#createBlockState()
     */
    @ApiStatus.OverrideOnly
    public IGeneratedBlockState createBlockState() {
        // spotless:off
        return this.provider.getVariantBuilder(this.getBlock())
                .partialState()
                    .modelForState()
                        .modelFile(this.provider.models().getExistingFile(this.blockId))
                    .addModel();
        // spotless:on
    }

    /**
     * Creates an item model based on the block model that extends to the north/south end of the block space.
     * 
     * @param name The resulting model's path.
     * @param min  The minimum X/Y coordinate.
     * @param max  The maximum X/Y coordinate.
     * @return An item model builder.
     */
    protected ItemModelBuilder createItemModel(ResourceLocation name, float min, float max) {
        Reference2FloatMap<Direction> faceEndpoints = new Reference2FloatOpenHashMap<>();
        faceEndpoints.put(Direction.DOWN, min);
        faceEndpoints.put(Direction.UP, max);
        faceEndpoints.put(Direction.NORTH, 0);
        faceEndpoints.put(Direction.SOUTH, 16);
        faceEndpoints.put(Direction.WEST, min);
        faceEndpoints.put(Direction.EAST, max);

        ItemModelBuilder model = this.provider.itemModels().getBuilder(name.toString())
                .parent(this.getOrCreateCenterElement());
        makePartModelElement(model, Direction.NORTH, false, faceEndpoints, 0.0f, 0, 1,
                min, min, 0, max, max, 16, this.side, this.end, SIDE_KEY, END_KEY);
        makePartModelElement(model, Direction.NORTH, true, faceEndpoints, 0.001f, 0, 1,
                min, min, 0, max, max, 16, this.sideSecondary, this.endSecondary, SIDE_SECONDARY_KEY,
                END_SECONDARY_KEY);
        makePartModelElement(model, Direction.NORTH, true, faceEndpoints, 0.002f, 2, 2,
                min, min, 0, max, max, 16, this.sideOverlay, this.endOverlay, SIDE_OVERLAY_KEY, END_OVERLAY_KEY);
        return model;
    }

    /**
     * Fills out a model builder with applicable pipe model elements and returns it for further use
     *
     * @param name    the resulting model's path
     * @param endFace the model face that's being created
     * @param x1      min X coordinate in the range [-16,32]
     * @param y1      min Y coordinate in the range [-16,32]
     * @param z1      min Z coordinate in the range [-16,32]
     * @param x2      max X coordinate in the range [-16,32]
     * @param y2      max Y coordinate in the range [-16,32]
     * @param z2      max Z coordinate in the range [-16,32]
     * @implNote The coordinates must be in the correct order or the resulting model's cubes will be inside out!
     */
    protected BlockModelBuilder makeElementModel(ResourceLocation name, @Nullable Direction endFace,
                                                 final float x1, final float y1, final float z1,
                                                 final float x2, final float y2, final float z2) {
        Reference2FloatMap<Direction> faceEndpoints = makeFaceEndpointMap(x1, y1, z1, x2, y2, z2);

        BlockModelBuilder model = this.provider.models().getBuilder(name.toString())
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("particle", "#" + (this.side != null ? SIDE_KEY : END_KEY));
        makePartModelElement(model, endFace, false, faceEndpoints, 0.0f, 0, 1,
                x1, y1, z1, x2, y2, z2, this.side, this.end, SIDE_KEY, END_KEY);
        makePartModelElement(model, endFace, true, faceEndpoints, 0.001f, 0, 1,
                x1, y1, z1, x2, y2, z2, this.sideSecondary, this.endSecondary, "side_secondary", "end_secondary");
        makePartModelElement(model, endFace, true, faceEndpoints, 0.002f, 2, 2,
                x1, y1, z1, x2, y2, z2, this.sideOverlay, this.endOverlay, "side_overlay", "end_overlay");
        return model;
    }

    protected <T extends ModelBuilder<T>> void makePartModelElement(T model, @Nullable Direction endFace,
                                                                    boolean useEndWithFullCube,
                                                                    Reference2FloatMap<Direction> faceEndpoints,
                                                                    float offset, int sideTintIndex, int endTintIndex,
                                                                    final float x1, final float y1, final float z1,
                                                                    final float x2, final float y2, final float z2,
                                                                    @Nullable ResourceLocation sideTexture,
                                                                    @Nullable ResourceLocation endTexture,
                                                                    String sideKey, String endKey) {
        makePartModelElement(model, endFace, useEndWithFullCube, faceEndpoints,
                offset, sideTintIndex, endTintIndex, x1, y1, z1, x2, y2, z2,
                sideTexture, endTexture, sideKey, endKey, (face, texture, builder) -> {});
    }

    protected <T extends ModelBuilder<T>> void makePartModelElement(T model, @Nullable Direction endFace,
                                                                    boolean useEndWithFullCube,
                                                                    Reference2FloatMap<Direction> faceEndpoints,
                                                                    float offset, int sideTintIndex, int endTintIndex,
                                                                    final float x1, final float y1, final float z1,
                                                                    final float x2, final float y2, final float z2,
                                                                    @Nullable ResourceLocation sideTexture,
                                                                    @Nullable ResourceLocation endTexture,
                                                                    String sideKey, String endKey,
                                                                    FaceConfigurator<T> faceConfigurator) {
        if (sideTexture == null && (endFace == null || endTexture == null)) {
            return;
        }
        if (sideTexture != null) model.texture(sideKey, sideTexture);
        if (endFace != null && endTexture != null) model.texture(endKey, endTexture);

        boolean fullCube = !useEndWithFullCube &&
                (x1 == y1 && x1 == z1 && x1 <= 0.0f) &&
                (x2 == y2 && x2 == z2 && x2 >= 16.0f);

        ModelBuilder<T>.ElementBuilder element = model.element()
                .from(x1 - offset, y1 - offset, z1 - offset)
                .to(x2 + offset, y2 + offset, z2 + offset);

        for (Direction dir : GTUtil.DIRECTIONS) {
            ModelBuilder<T>.ElementBuilder.FaceBuilder face = null;
            boolean isEnd = !fullCube && endFace != null && (endFace == dir || endFace == dir.getOpposite());
            if (isEnd && endTexture != null) {
                face = element.face(dir).texture("#" + endKey).tintindex(endTintIndex);
                faceConfigurator.accept(dir, endKey, face);
            } else if (!isEnd && sideTexture != null) {
                face = element.face(dir).texture("#" + sideKey).tintindex(sideTintIndex);
                faceConfigurator.accept(dir, sideKey, face);
            }

            float faceEnd = faceEndpoints.getFloat(dir);
            if (face != null && (faceEnd >= 16.0f || faceEnd <= 0.0f)) {
                face.cullface(dir);
            }
        }
    }

    protected final Reference2FloatMap<Direction> makeFaceEndpointMap(final float x1, final float y1, final float z1,
                                                                      final float x2, final float y2, final float z2) {
        Reference2FloatMap<Direction> faceEndpoints = new Reference2FloatOpenHashMap<>();
        faceEndpoints.defaultReturnValue(GTMath.max(x1, y1, z1, x2, y2, z2));
        for (Direction dir : GTUtil.DIRECTIONS) {
            faceEndpoints.put(dir, switch (dir) {
                case DOWN -> Math.min(y1, y2);
                case UP -> Math.max(y1, y2);
                case NORTH -> Math.min(z1, z2);
                case SOUTH -> Math.max(z1, z2);
                case WEST -> Math.min(x1, x2);
                case EAST -> Math.max(x1, x2);
            });
        }
        return faceEndpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PipeModel pipeModel)) return false;
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

    @FunctionalInterface
    public interface FaceConfigurator<T extends ModelBuilder<T>> {

        /**
         * This is a callback for modifying a block element face builder in ways not supported by "basic" API.<br>
         * For example, you can make faces emissive, like {@link ActivablePipeModel#makePartModelElement}.
         * 
         * @param face    The normal direction of this face.
         * @param texture The texture of the face, usually in {@code #reference} format.
         *                <b>Note that the String does NOT begin with {@code #}</b>.
         * @param builder The face builder.
         * @see ActivablePipeModel#makePartModelElement(ModelBuilder, Direction, boolean, Reference2FloatMap, float,
         *      int, int, float, float, float, float, float, float, ResourceLocation, ResourceLocation, String, String,
         *      boolean, boolean) ActivablePipeModel.makePartModelElement
         */
        void accept(Direction face, String texture, ModelBuilder<T>.ElementBuilder.FaceBuilder builder);
    }
}

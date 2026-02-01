package com.gregtechceu.gtceu.data.model.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModelLoader;
import com.gregtechceu.gtceu.core.mixins.forge.ConfiguredModelBuilderAccessor;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.function.MemoizedBiFunction;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.BlockStateProvider.ConfiguredModelList;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder.configuredModelListToJSON;
import static com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder.configuredModelToJSON;

@Accessors(fluent = true, chain = true)
@SuppressWarnings("UnusedReturnValue")
public class PipeModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    // spotless:off
    public static <T extends ModelBuilder<T>> BiFunction<T, ExistingFileHelper, PipeModelBuilder<T>> begin(@Range(from = 0, to = 16) float thickness,
                                                                                                           GTBlockstateProvider provider) {
        return (parent, existingFileHelper) -> new PipeModelBuilder<>(parent, existingFileHelper, thickness, provider);
    }
    // spotless:on

    @Accessors(fluent = false)
    @Getter
    private final Map<@Nullable Direction, ConfiguredModelList> parts = new IdentityHashMap<>();
    private final float thickness;
    private final GTBlockstateProvider provider;
    private BlockModelBuilder @Nullable [] restrictors = null;

    protected PipeModelBuilder(T parent, ExistingFileHelper existingFileHelper,
                               float thickness, GTBlockstateProvider provider) {
        super(PipeModelLoader.ID, parent, existingFileHelper);

        Preconditions.checkArgument(thickness > 0.0f && thickness <= 16.0f,
                "Thickness must be between 0 (exclusive) and 16 (inclusive). It is %s", thickness);
        this.thickness = thickness;
        this.provider = provider;
    }

    /**
     * Set the models for all pipe elements at the same time
     *
     * @param centerModel      The model to use for the center part of the pipe
     * @param connectionModels The models to use for all the connection elements
     * @return {@code this}
     * @see #allModels(ModelFile, ModelFile...)
     * @see #allModels(ResourceLocation, ResourceLocation...)
     */
    public PipeModelBuilder<T> allModels(ConfiguredModel centerModel, ConfiguredModel... connectionModels) {
        centerModels(centerModel);
        connectionModels(connectionModels);
        return this;
    }

    /**
     * Set the models for all pipe elements at the same time
     *
     * @param centerModel      The model to use for the center part of the pipe
     * @param connectionModels The models to use for all the connection elements
     * @return {@code this}
     * @see #allModels(ModelFile, ModelFile...)
     * @see #allModels(ResourceLocation, ResourceLocation...)
     */
    public PipeModelBuilder<T> allModels(ModelFile centerModel, ModelFile... connectionModels) {
        centerModels(centerModel);
        connectionModels(connectionModels);
        return this;
    }

    /**
     * Set the models for all pipe elements at the same time
     *
     * @param centerModel      The model to use for the center part of the pipe
     * @param connectionModels The models to use for all the connection elements
     * @return {@code this}
     * @see #allModels(ConfiguredModel, ConfiguredModel...)
     * @see #allModels(ModelFile, ModelFile...)
     */
    public PipeModelBuilder<T> allModels(ResourceLocation centerModel, ResourceLocation... connectionModels) {
        centerModels(centerModel);
        connectionModels(connectionModels);
        return this;
    }

    /**
     * Set the models for all connection elements
     *
     * @param connectionModels The models to use for all the connection elements
     * @return {@code this}
     * @see #connectionModels(ModelFile...)
     * @see #connectionModels(ResourceLocation...)
     */
    public PipeModelBuilder<T> connectionModels(ConfiguredModel... connectionModels) {
        for (Direction dir : GTUtil.DIRECTIONS) {
            ConfiguredModel[] rotatedModels = Arrays.stream(connectionModels)
                    .map(model -> ConfiguredModel.builder()
                            .modelFile(model.model).uvLock(model.uvLock).weight(model.weight)
                            .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? 270 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : ((int) dir.toYRot() + 180) % 360)
                            .buildLast())
                    .toArray(ConfiguredModel[]::new);
            modelsForDirection(dir, rotatedModels);
        }
        return this;
    }

    /**
     * Set the models for all connection elements
     *
     * @param connectionModels The models to use for all the connection elements
     * @return {@code this}
     * @see #connectionModels(ConfiguredModel...)
     * @see #connectionModels(ResourceLocation...)
     */
    public PipeModelBuilder<T> connectionModels(ModelFile... connectionModels) {
        for (Direction dir : GTUtil.DIRECTIONS) {
            ConfiguredModel[] rotatedModels = Arrays.stream(connectionModels)
                    .map(model -> ConfiguredModel.builder()
                            .modelFile(model)
                            .rotationX(dir == Direction.DOWN ? 0 : dir == Direction.UP ? 180 : 90)
                            .rotationY(dir.getAxis().isVertical() ? 0 : (int) dir.toYRot())
                            .buildLast())
                    .toArray(ConfiguredModel[]::new);
            modelsForDirection(dir, rotatedModels);
        }
        return this;
    }

    /**
     * Set the models for all connection elements
     *
     * @param connectionModels The models to use for all the connection elements
     * @return {@code this}
     * @see #connectionModels(ConfiguredModel...)
     * @see #connectionModels(ModelFile...)
     */
    public PipeModelBuilder<T> connectionModels(ResourceLocation... connectionModels) {
        return connectionModels(Arrays.stream(connectionModels)
                .map(loc -> new ModelFile.ExistingModelFile(loc, this.existingFileHelper))
                .toArray(ModelFile[]::new));
    }

    /**
     * Set the models for all connection elements with a builder
     *
     * @return A model builder
     * @see #connectionModels(ConfiguredModel...)
     * @see #connectionModels(ModelFile...)
     * @see #connectionModels(ResourceLocation...)
     */
    public ConfiguredModel.Builder<PipeModelBuilder<T>> connectionModels() {
        return ConfiguredModelBuilderAccessor.builder(this::connectionModels, ImmutableList.of());
    }

    /**
     * Set the models for the center element
     *
     * @param centerModels The model to use for the center part of the pipe
     * @return {@code this}
     * @see #centerModels(ModelFile...)
     * @see #centerModels(ResourceLocation...)
     */
    public PipeModelBuilder<T> centerModels(ConfiguredModel... centerModels) {
        return modelsForDirection(null, centerModels);
    }

    /**
     * Set the models for the center element
     *
     * @param centerModels The model to use for the center part of the pipe
     * @return {@code this}
     * @see #centerModels(ConfiguredModel...)
     * @see #centerModels(ResourceLocation...)
     */
    public PipeModelBuilder<T> centerModels(ModelFile... centerModels) {
        return modelsForDirection(null, centerModels);
    }

    /**
     * Set the models for the center element
     *
     * @param centerModels The model to use for the center part of the pipe
     * @return {@code this}
     * @see #centerModels(ConfiguredModel...)
     * @see #centerModels(ModelFile...)
     */
    public PipeModelBuilder<T> centerModels(ResourceLocation... centerModels) {
        return modelsForDirection(null, centerModels);
    }

    /**
     * Set the models for the center element with a builder
     *
     * @return A model builder
     * @see #centerModels(ConfiguredModel...)
     * @see #centerModels(ModelFile...)
     * @see #centerModels(ResourceLocation...)
     */
    public ConfiguredModel.Builder<PipeModelBuilder<T>> centerModel() {
        return ConfiguredModelBuilderAccessor.builder(this::centerModels, ImmutableList.of());
    }

    /**
     * Set the models for the given direction
     *
     * @param direction The direction that'll use the model(s)
     * @param models    The models to set for the direction.
     * @return {@code this}
     * @see #modelsForDirection(Direction, ModelFile...)
     * @see #modelsForDirection(Direction, ResourceLocation...)
     */
    public PipeModelBuilder<T> modelsForDirection(@Nullable Direction direction, ConfiguredModel... models) {
        parts.put(direction, new ConfiguredModelList(models));
        return this;
    }

    /**
     * Set the models for the given direction
     *
     * @param direction The direction that'll use the model(s)
     * @param models    The models to set for the direction.
     * @return {@code this}
     * @see #modelsForDirection(Direction, ConfiguredModel...)
     * @see #modelsForDirection(Direction, ResourceLocation...)
     */
    public PipeModelBuilder<T> modelsForDirection(@Nullable Direction direction, ModelFile... models) {
        return modelsForDirection(direction, Arrays.stream(models)
                .map(model -> ConfiguredModel.builder().modelFile(model).buildLast())
                .toArray(ConfiguredModel[]::new));
    }

    /**
     * Set the models for the given direction
     *
     * @param direction The direction that'll use the model(s)
     * @param models    The models to set for the direction.
     * @return {@code this}
     * @see #modelsForDirection(Direction, ConfiguredModel...)
     * @see #modelsForDirection(Direction, ModelFile...)
     */
    public PipeModelBuilder<T> modelsForDirection(@Nullable Direction direction, ResourceLocation... models) {
        return modelsForDirection(direction, Arrays.stream(models)
                .map(model -> ConfiguredModel.builder()
                        .modelFile(new ModelFile.ExistingModelFile(model, this.existingFileHelper))
                        .buildLast())
                .toArray(ConfiguredModel[]::new));
    }

    /**
     * Set the models for the given direction with a builder
     *
     * @return A model builder
     * @see #modelsForDirection(Direction, ConfiguredModel...)
     * @see #modelsForDirection(Direction, ModelFile...)
     * @see #modelsForDirection(Direction, ResourceLocation...)
     */
    public ConfiguredModel.Builder<PipeModelBuilder<T>> modelsForDirection(@Nullable Direction direction) {
        return ConfiguredModelBuilderAccessor.builder(models -> this.modelsForDirection(direction, models),
                ImmutableList.of());
    }

    @Override
    public T end() {
        this.restrictors = getOrCreateRestrictorModels(this.provider.models(), this.thickness);
        return super.end();
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        if (!getParts().isEmpty()) {
            final JsonObject parts = new JsonObject();
            getParts().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.nullsFirst(Direction::compareTo)))
                    .forEach(entry -> {
                        String key = entry.getKey() != null ? entry.getKey().getName() :
                                PipeModelLoader.PRIMARY_CENTER_KEY;
                        parts.add(key, configuredModelListToJSON(entry.getValue()));
                    });

            json.add("parts", parts);
        }

        if (this.restrictors != null) {
            final JsonObject restrictors = new JsonObject();
            for (int i = 0; i < GTUtil.DIRECTIONS.length; i++) {
                Direction dir = GTUtil.DIRECTIONS[i];
                restrictors.add(dir.getName(),
                        configuredModelToJSON(ConfiguredModel.builder()
                                .modelFile(new ModelFile.UncheckedModelFile(this.restrictors[i].getLocation()))
                                .buildLast(), false));
            }
            json.add("restrictors", restrictors);
        }

        return json;
    }

    private static final ResourceLocation PIPE_BLOCKED_OVERLAY = GTCEu.id("block/pipe/blocked/pipe_blocked");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_UP = GTCEu.id("block/pipe/blocked/pipe_blocked_up");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_DOWN = GTCEu.id("block/pipe/blocked/pipe_blocked_down");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_LEFT = GTCEu.id("block/pipe/blocked/pipe_blocked_left");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_RIGHT = GTCEu
            .id("block/pipe/blocked/pipe_blocked_right");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_NU = GTCEu.id("block/pipe/blocked/pipe_blocked_nu");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_ND = GTCEu.id("block/pipe/blocked/pipe_blocked_nd");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_NL = GTCEu.id("block/pipe/blocked/pipe_blocked_nl");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_NR = GTCEu.id("block/pipe/blocked/pipe_blocked_nr");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_UD = GTCEu.id("block/pipe/blocked/pipe_blocked_ud");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_UL = GTCEu.id("block/pipe/blocked/pipe_blocked_ul");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_UR = GTCEu.id("block/pipe/blocked/pipe_blocked_ur");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_DL = GTCEu.id("block/pipe/blocked/pipe_blocked_dl");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_DR = GTCEu.id("block/pipe/blocked/pipe_blocked_dr");
    private static final ResourceLocation PIPE_BLOCKED_OVERLAY_LR = GTCEu.id("block/pipe/blocked/pipe_blocked_lr");

    private static final Int2ObjectMap<ResourceLocation> RESTRICTOR_MAP = Util.make(() -> {
        Int2ObjectMap<ResourceLocation> map = new Int2ObjectOpenHashMap<>();

        addRestrictor(map, PIPE_BLOCKED_OVERLAY_UP, Border.TOP);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_DOWN, Border.BOTTOM);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_UD, Border.TOP, Border.BOTTOM);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_LEFT, Border.LEFT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_UL, Border.TOP, Border.LEFT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_DL, Border.BOTTOM, Border.LEFT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_NR, Border.TOP, Border.BOTTOM, Border.LEFT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_RIGHT, Border.RIGHT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_UR, Border.TOP, Border.RIGHT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_DR, Border.BOTTOM, Border.RIGHT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_NL, Border.TOP, Border.BOTTOM, Border.RIGHT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_LR, Border.LEFT, Border.RIGHT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_ND, Border.TOP, Border.LEFT, Border.RIGHT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY_NU, Border.BOTTOM, Border.LEFT, Border.RIGHT);
        addRestrictor(map, PIPE_BLOCKED_OVERLAY, Border.TOP, Border.BOTTOM, Border.LEFT, Border.RIGHT);

        return map;
    });

    private static BlockModelBuilder[] getOrCreateRestrictorModels(BlockModelProvider provider, float thickness) {
        return RESTRICTOR_MODEL_CACHE.apply(provider, thickness);
    }

    private static final MemoizedBiFunction<BlockModelProvider, Float, BlockModelBuilder[]> RESTRICTOR_MODEL_CACHE = GTMemoizer
            .memoizeFunctionWeakIdent(PipeModelBuilder::makeRestrictorModels);

    private static BlockModelBuilder[] makeRestrictorModels(BlockModelProvider provider, float thickness) {
        BlockModelBuilder[] models = new BlockModelBuilder[GTUtil.DIRECTIONS.length];

        float min = (16.0f - thickness) / 2.0f - 0.003f;
        float max = min + thickness + 0.006f; // offset by 0.003 * 2
        for (Direction dir : GTUtil.DIRECTIONS) {
            String modelPath = "block/pipe/restrictor/" + dir.getName() + "/thickness_" + thickness;
            ResourceLocation modelName = GTCEu.id(modelPath);
            if (provider.generatedModels.containsKey(modelName)) {
                models[dir.ordinal()] = provider.generatedModels.get(modelName);
                continue;
            }

            var coords = GTMath.getCoordinates(dir, min, max);
            Vector3f minPos = coords.getLeft();
            Vector3f maxPos = coords.getRight();
            BlockModelBuilder model = provider.getBuilder(modelPath);
            model.texture("restrictor", PIPE_BLOCKED_OVERLAY)
                    .element()
                    .from(minPos.x, minPos.y, minPos.z)
                    .to(maxPos.x, maxPos.y, maxPos.z)
                    .face(getSideAtBorder(dir, Border.BOTTOM)).end()
                    .face(getSideAtBorder(dir, Border.TOP)).end()
                    .face(getSideAtBorder(dir, Border.LEFT)).end()
                    .face(getSideAtBorder(dir, Border.RIGHT)).end()
                    .faces((face, builder) -> builder.texture("#restrictor"))
                    .end();
            models[dir.ordinal()] = model;
        }
        return models;
    }

    @ApiStatus.Internal
    public static void clearRestrictorModelCache() {
        RESTRICTOR_MODEL_CACHE.getCache().clear();
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

    private static EnumMap<Border, Direction> borderMap(Direction topSide, Direction bottomSide,
                                                        Direction leftSide, Direction rightSide) {
        EnumMap<Border, Direction> sideMap = new EnumMap<>(Border.class);
        sideMap.put(Border.TOP, topSide);
        sideMap.put(Border.BOTTOM, bottomSide);
        sideMap.put(Border.LEFT, leftSide);
        sideMap.put(Border.RIGHT, rightSide);
        return sideMap;
    }

    private static void addRestrictor(Int2ObjectMap<ResourceLocation> map, ResourceLocation texture,
                                      Border... borders) {
        int mask = 0;
        for (Border border : borders) {
            mask |= border.mask;
        }
        map.put(mask, texture);
    }

    private static Direction getSideAtBorder(Direction side, Border border) {
        return FACE_BORDER_MAP.get(side).get(border);
    }

    private static int computeBorderMask(int blockedConnections, int connections, Direction side) {
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

    private enum Border {

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

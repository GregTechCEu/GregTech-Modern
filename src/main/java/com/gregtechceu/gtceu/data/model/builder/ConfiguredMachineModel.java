package com.gregtechceu.gtceu.data.model.builder;

import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Represents a model with blockstate configurations, e.g. rotation, uvlock, and
 * random weight.
 * <p>
 * Can be manually constructed, created by static factory such as
 * {@link #allYRotations(ModelFile, int, boolean)}, or created by builder via
 * {@link #builder()}.
 */
public final class ConfiguredMachineModel {

    /**
     * The default random weight of configured models, used by convenience
     * overloads.
     */
    public static final int DEFAULT_WEIGHT = 1;

    public final ModelFile model;
    public final int rotationX;
    public final int rotationY;
    public final boolean uvLock;
    public final int weight;

    private static IntStream validRotations() {
        return IntStream.range(0, 4).map(i -> i * 90);
    }

    public static ConfiguredMachineModel[] allYRotations(ModelFile model, int x, boolean uvlock) {
        return allYRotations(model, x, uvlock, DEFAULT_WEIGHT);
    }

    public static ConfiguredMachineModel[] allYRotations(ModelFile model, int x, boolean uvlock, int weight) {
        return validRotations()
                .mapToObj(y -> new ConfiguredMachineModel(model, x, y, uvlock, weight))
                .toArray(ConfiguredMachineModel[]::new);
    }

    public static ConfiguredMachineModel[] allRotations(ModelFile model, boolean uvlock) {
        return allRotations(model, uvlock, DEFAULT_WEIGHT);
    }

    public static ConfiguredMachineModel[] allRotations(ModelFile model, boolean uvlock, int weight) {
        return validRotations()
                .mapToObj(x -> allYRotations(model, x, uvlock, weight))
                .flatMap(Arrays::stream)
                .toArray(ConfiguredMachineModel[]::new);
    }

    /**
     * Construct a new {@link ConfiguredMachineModel}.
     *
     * @param model     the underlying model
     * @param rotationX x-rotation to apply to the model
     * @param rotationY y-rotation to apply to the model
     * @param uvLock    if uvlock should be enabled
     * @param weight    the random weight of the model
     *
     * @throws NullPointerException     if {@code model} is {@code null}
     * @throws IllegalArgumentException if x and/or y rotation are not valid (see
     *                                  {@link BlockModelRotation})
     * @throws IllegalArgumentException if weight is less than or equal to zero
     */
    public ConfiguredMachineModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
        Preconditions.checkNotNull(model);
        this.model = model;
        checkRotation(rotationX, rotationY);
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.uvLock = uvLock;
        checkWeight(weight);
        this.weight = weight;
    }

    /**
     * Construct a new {@link ConfiguredMachineModel} with the {@link #DEFAULT_WEIGHT
     * default random weight}.
     *
     * @param model     the underlying model
     * @param rotationX x-rotation to apply to the model
     * @param rotationY y-rotation to apply to the model
     * @param uvLock    if uvlock should be enabled
     *
     * @throws NullPointerException     if {@code model} is {@code null}
     * @throws IllegalArgumentException if x and/or y rotation are not valid (see
     *                                  {@link BlockModelRotation})
     */
    public ConfiguredMachineModel(ModelFile model, int rotationX, int rotationY, boolean uvLock) {
        this(model, rotationX, rotationY, uvLock, DEFAULT_WEIGHT);
    }

    /**
     * Construct a new {@link ConfiguredMachineModel} with the default rotation (0, 0),
     * uvlock (false), and {@link #DEFAULT_WEIGHT default random weight}.
     *
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public ConfiguredMachineModel(ModelFile model) {
        this(model, 0, 0, false);
    }

    @SuppressWarnings("ConstantValue")
    static void checkRotation(int rotationX, int rotationY) {
        Preconditions.checkArgument(BlockModelRotation.by(rotationX, rotationY) != null,
                "Invalid model rotation x=%d, y=%d", rotationX, rotationY);
    }

    static void checkWeight(int weight) {
        Preconditions.checkArgument(weight >= 1, "Model weight must be greater than or equal to 1. Found: %d", weight);
    }

    JsonObject toJSON(boolean includeWeight) {
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("model", model.getLocation().toString());
        if (rotationX != 0)
            modelJson.addProperty("x", rotationX);
        if (rotationY != 0)
            modelJson.addProperty("y", rotationY);
        if (uvLock)
            modelJson.addProperty("uvlock", uvLock);
        if (includeWeight && weight != DEFAULT_WEIGHT)
            modelJson.addProperty("weight", weight);
        return modelJson;
    }

    /**
     * Create a new unowned {@link Builder}.
     *
     * @return the builder
     * @see Builder
     */
    public static Builder<?> builder() {
        return new Builder<>();
    }

    static <T extends ModelBuilder<T>> Builder<MachineModelBuilder<T>> builder(MachineModelBuilder<T> outer,
                                                                               MachineModelBuilder.PartialState<T> state) {
        return new Builder<>(models -> outer.setModels(state, models), ImmutableList.of());
    }

    /**
     * A builder for {@link ConfiguredMachineModel}s, which can contain a callback for
     * processing the finished result. If no callback is available (e.g. in the case
     * of {@link ConfiguredMachineModel#builder()}), some methods will not be available.
     * <p>
     * Multiple models can be configured at once through the use of
     * {@link #nextModel()}.
     *
     * @param <T> the type of the owning builder, which supplied the callback, and
     *            will be returned upon completion.
     */
    public static class Builder<T> {

        private ModelFile model;
        @Nullable
        private final Function<ConfiguredMachineModel[], T> callback;
        private final List<ConfiguredMachineModel> otherModels;
        private int rotationX;
        private int rotationY;
        private boolean uvLock;
        private int weight = DEFAULT_WEIGHT;

        Builder() {
            this(null, ImmutableList.of());
        }

        Builder(@Nullable Function<ConfiguredMachineModel[], T> callback, List<ConfiguredMachineModel> otherModels) {
            this.callback = callback;
            this.otherModels = otherModels;
        }

        /**
         * Set the underlying model object for this configured model.
         *
         * @param model the model
         * @return this builder
         * @throws NullPointerException if {@code model} is {@code null}
         */
        public Builder<T> modelFile(ModelFile model) {
            Preconditions.checkNotNull(model, "Model must not be null");
            this.model = model;
            return this;
        }

        /**
         * Set the x-rotation for this model.
         *
         * @param value the x-rotation value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is not a valid x-rotation
         *                                  (see {@link BlockModelRotation})
         */
        public Builder<T> rotationX(int value) {
            checkRotation(value, rotationY);
            rotationX = value;
            return this;
        }

        /**
         * Set the y-rotation for this model.
         *
         * @param value the y-rotation value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is not a valid y-rotation
         *                                  (see {@link BlockModelRotation})
         */
        public Builder<T> rotationY(int value) {
            checkRotation(rotationX, value);
            rotationY = value;
            return this;
        }

        public Builder<T> uvLock(boolean value) {
            uvLock = value;
            return this;
        }

        /**
         * Set the random weight for this model.
         *
         * @param value the weight value
         * @return this builder
         * @throws IllegalArgumentException if {@code value} is less than or equal to
         *                                  zero
         */
        public Builder<T> weight(int value) {
            checkWeight(value);
            weight = value;
            return this;
        }

        /**
         * Build the most recent model, as if {@link #nextModel()} was never called.
         * Useful for single-model builders.
         *
         * @return the most recently configured model
         */
        public ConfiguredMachineModel buildLast() {
            return new ConfiguredMachineModel(model, rotationX, rotationY, uvLock, weight);
        }

        /**
         * Build all configured models and return them as an array.
         *
         * @return the array of built models.
         */
        public ConfiguredMachineModel[] build() {
            return ObjectArrays.concat(otherModels.toArray(new ConfiguredMachineModel[0]), buildLast());
        }

        /**
         * Apply the contained callback and return the owning builder object. What the
         * callback does is not defined by this class, but most likely it adds the built
         * models to the current variant being configured.
         * <p>
         * Known callbacks include:
         * <ul>
         * <li>{@link MachineModelBuilder.PartialState#modelForState()}</li>
         * <li>{@link MultiPartBlockStateBuilder#part()}</li>
         * </ul>
         *
         * @return the owning builder object
         * @throws NullPointerException if there is no owning builder (and thus no callback)
         */
        public T addModel() {
            Preconditions.checkNotNull(callback, "Cannot use addModel() without an owning builder present");
            return callback.apply(build());
        }

        /**
         * Complete the current model and return a new builder instance with the same
         * callback, and storing all previously built models.
         *
         * @return a new builder for configuring the next model
         */
        public Builder<T> nextModel() {
            return new Builder<>(callback, Arrays.asList(build()));
        }
    }
}

package net.neoforged.neoforge.client.model.generators;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public final class ConfiguredModel {

    public static final int DEFAULT_WEIGHT = 1;

    public final ModelFile model;
    public final int rotationX;
    public final int rotationY;
    public final boolean uvLock;
    public final int weight;

    public ConfiguredModel(ModelFile model) {
        this(model, 0, 0, false);
    }

    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock) {
        this(model, rotationX, rotationY, uvLock, DEFAULT_WEIGHT);
    }

    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
        this.model = Preconditions.checkNotNull(model);
        checkRotation(rotationX, rotationY);
        checkWeight(weight);
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.uvLock = uvLock;
        this.weight = weight;
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvLock) {
        return allYRotations(model, x, uvLock, DEFAULT_WEIGHT);
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvLock, int weight) {
        return validRotations()
                .mapToObj(y -> new ConfiguredModel(model, x, y, uvLock, weight))
                .toArray(ConfiguredModel[]::new);
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvLock) {
        return allRotations(model, uvLock, DEFAULT_WEIGHT);
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvLock, int weight) {
        return validRotations()
                .mapToObj(x -> allYRotations(model, x, uvLock, weight))
                .flatMap(Arrays::stream)
                .toArray(ConfiguredModel[]::new);
    }

    private static IntStream validRotations() {
        return IntStream.range(0, 4).map(i -> i * 90);
    }

    static void checkRotation(int rotationX, int rotationY) {
        Preconditions.checkArgument(isValidRotation(rotationX), "Invalid model rotation x=%s", rotationX);
        Preconditions.checkArgument(isValidRotation(rotationY), "Invalid model rotation y=%s", rotationY);
    }

    private static boolean isValidRotation(int rotation) {
        return rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270;
    }

    static void checkWeight(int weight) {
        Preconditions.checkArgument(weight >= 1, "Model weight must be greater than or equal to 1. Found: %s", weight);
    }

    JsonObject toJSON(boolean includeWeight) {
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("model", model.getLocation().toString());
        if (rotationX != 0) modelJson.addProperty("x", rotationX);
        if (rotationY != 0) modelJson.addProperty("y", rotationY);
        if (uvLock) modelJson.addProperty("uvlock", true);
        if (includeWeight && weight != DEFAULT_WEIGHT) modelJson.addProperty("weight", weight);
        return modelJson;
    }

    public static Builder<?> builder() {
        return new Builder<>();
    }

    static Builder<VariantBlockStateBuilder> builder(VariantBlockStateBuilder outer,
                                                     VariantBlockStateBuilder.PartialBlockstate state) {
        return new Builder<>(models -> outer.setModels(state, models), ImmutableList.of());
    }

    static Builder<MultiPartBlockStateBuilder.PartBuilder> builder(MultiPartBlockStateBuilder outer) {
        return new Builder<>(models -> {
            MultiPartBlockStateBuilder.PartBuilder part = outer.new PartBuilder(
                    new BlockStateProvider.ConfiguredModelList(models));
            outer.addPart(part);
            return part;
        }, ImmutableList.of());
    }

    public static <T> Builder<T> builder(@Nullable Function<ConfiguredModel[], T> callback,
                                         List<ConfiguredModel> otherModels) {
        return new Builder<>(callback, otherModels);
    }

    public static class Builder<T> {

        private ModelFile model;
        @Nullable
        private final Function<ConfiguredModel[], T> callback;
        private final List<ConfiguredModel> otherModels;
        private int rotationX;
        private int rotationY;
        private boolean uvLock;
        private int weight = DEFAULT_WEIGHT;

        public Builder() {
            this(null, ImmutableList.of());
        }

        public Builder(@Nullable Function<ConfiguredModel[], T> callback, List<ConfiguredModel> otherModels) {
            this.callback = callback;
            this.otherModels = otherModels;
        }

        public Builder<T> modelFile(ModelFile model) {
            this.model = Preconditions.checkNotNull(model, "Model must not be null");
            return this;
        }

        public Builder<T> rotationX(int value) {
            checkRotation(value, rotationY);
            rotationX = value;
            return this;
        }

        public Builder<T> rotationY(int value) {
            checkRotation(rotationX, value);
            rotationY = value;
            return this;
        }

        public Builder<T> uvLock(boolean value) {
            uvLock = value;
            return this;
        }

        public Builder<T> weight(int value) {
            checkWeight(value);
            weight = value;
            return this;
        }

        public Builder<T> nextModel() {
            List<ConfiguredModel> nextModels = new ArrayList<>(otherModels);
            nextModels.add(buildLast());
            return new Builder<>(callback, nextModels);
        }

        public ConfiguredModel buildLast() {
            Preconditions.checkNotNull(model, "Model must be set before building");
            return new ConfiguredModel(model, rotationX, rotationY, uvLock, weight);
        }

        public ConfiguredModel[] build() {
            List<ConfiguredModel> models = new ArrayList<>(otherModels);
            models.add(buildLast());
            return models.toArray(ConfiguredModel[]::new);
        }

        public T addModel() {
            Preconditions.checkNotNull(callback, "Cannot add model without an owning builder");
            return callback.apply(build());
        }
    }
}

package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GTLayerPattern {

    public static final Codec<GTLayerPattern> CODEC = Codec.list(Layer.CODEC)
            .xmap(GTLayerPattern::new, pattern -> pattern.layers);

    public final List<Layer> layers;

    public GTLayerPattern(List<Layer> layers) {
        this.layers = layers;
    }

    public @Nullable Layer rollNext(@Nullable Layer previous, RandomSource random) {
        if (layers.isEmpty()) return null;
        if (layers.size() == 1) return layers.get(0);

        int totalWeight = 0;
        for (Layer layer : layers) {
            if (layer != previous) totalWeight += layer.weight;
        }
        // totalWeight should be >0 here but better be safe than sorry
        if (totalWeight <= 0) return null;

        int rolled = random.nextInt(totalWeight);

        for (Layer layer : layers) {
            if (layer == previous) continue;

            rolled -= layer.weight;
            if (rolled < 0) return layer;
        }
        return null;
    }

    public static Builder builder(RuleTest... rules) {
        return new Builder(rules);
    }

    public static class Builder {

        private final List<Layer> layers = new ArrayList<>();
        private final RuleTest[] rules;

        protected Builder(RuleTest... rules) {
            this.rules = rules;
        }

        public Builder layer(Consumer<Layer.Builder> builder) {
            Layer.Builder layerBuilder = new Layer.Builder(rules);
            builder.accept(layerBuilder);
            layers.add(layerBuilder.build());
            return this;
        }

        public GTLayerPattern build() {
            return new GTLayerPattern(layers);
        }
    }

    public static class Layer {

        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(Codec.either(TargetBlockState.CODEC.listOf(), GTCEuAPI.materialManager.codec()))
                        .fieldOf("targets")
                        .forGetter(layer -> layer.targets),
                Codec.intRange(0, Integer.MAX_VALUE)
                        .fieldOf("min_size")
                        .forGetter(layer -> layer.minSize),
                Codec.intRange(0, Integer.MAX_VALUE)
                        .fieldOf("max_size")
                        .forGetter(layer -> layer.maxSize),
                Codec.intRange(0, Integer.MAX_VALUE)
                        .fieldOf("weight")
                        .forGetter(layer -> layer.weight))
                .apply(instance, Layer::new));

        public final List<Either<List<TargetBlockState>, Material>> targets;
        public final int minSize;
        public final int maxSize;
        public final int weight;

        public Layer(List<Either<List<TargetBlockState>, Material>> targets, int minSize, int maxSize, int weight) {
            if (minSize > maxSize) {
                StringBuilder materialList = new StringBuilder().append(" with materials: ");
                for (var target : targets) {
                    if (target.right().isPresent()) {
                        materialList.append(target.right().get().getName()).append(",");
                    }
                }
                if (!materialList.isEmpty()) {
                    materialList.deleteCharAt(materialList.length() - 1);
                }

                throw new IllegalArgumentException(
                        "Layer must have minSize (%s) be lower than maxSize (%s)%s".formatted(minSize, maxSize,
                                materialList.toString()));
            }
            this.targets = targets;
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.weight = weight;
        }

        public Stream<VeinGenerator.VeinEntry> asVeinEntries() {
            return targets.stream()
                    .flatMap(target -> VeinGenerator.mapTarget(target, weight));
        }

        public Either<List<TargetBlockState>, Material> rollBlock(RandomSource random) {
            if (targets.size() == 1)
                return targets.get(0);
            return targets.get(random.nextInt(targets.size()));
        }

        public static class Builder {

            private final List<Either<List<TargetBlockState>, Material>> targets = new ArrayList<>();
            private int minSize = 1;
            private int maxSize = 1;
            private int weight = 1;
            private final RuleTest[] rules;

            protected Builder(RuleTest... rules) {
                this.rules = rules;
            }

            public GTLayerPattern.Layer.Builder block(Supplier<? extends Block> block) {
                return state(block.get().defaultBlockState());
            }

            public GTLayerPattern.Layer.Builder state(Supplier<? extends BlockState> state) {
                return state(state.get());
            }

            public GTLayerPattern.Layer.Builder state(BlockState state) {
                this.targets.add(Either
                        .left(Arrays.stream(this.rules).map(rule -> OreConfiguration.target(rule, state)).toList()));
                return this;
            }

            public GTLayerPattern.Layer.Builder mat(Material material) {
                this.targets.add(Either.right(material));
                return this;
            }

            public GTLayerPattern.Layer.Builder weight(int weight) {
                this.weight = weight;
                return this;
            }

            public GTLayerPattern.Layer.Builder size(int min, int max) {
                this.minSize = min;
                this.maxSize = max;
                return this;
            }

            public Layer build() {
                return new Layer(targets, minSize, maxSize, weight);
            }
        }
    }
}

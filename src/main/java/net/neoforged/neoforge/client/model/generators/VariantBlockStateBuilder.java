package net.neoforged.neoforge.client.model.generators;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VariantBlockStateBuilder implements IGeneratedBlockState {

    private final Block owner;
    private final Map<PartialBlockstate, BlockStateProvider.ConfiguredModelList> models = new LinkedHashMap<>();
    private final Set<BlockState> coveredStates = new HashSet<>();

    VariantBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    public Map<PartialBlockstate, BlockStateProvider.ConfiguredModelList> getModels() {
        return models;
    }

    @Override
    public JsonObject toJson() {
        List<BlockState> missingStates = Lists.newArrayList(owner.getStateDefinition().getPossibleStates());
        missingStates.removeAll(coveredStates);
        Preconditions.checkState(missingStates.isEmpty(),
                "Blockstate for block %s does not cover all states. Missing: %s",
                owner, missingStates);
        JsonObject variants = new JsonObject();
        models.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(PartialBlockstate.comparingByProperties()))
                .forEach(entry -> variants.add(entry.getKey().toString(), entry.getValue().toJSON()));
        JsonObject main = new JsonObject();
        main.add("variants", variants);
        return main;
    }

    public VariantBlockStateBuilder addModels(PartialBlockstate state, ConfiguredModel... models) {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkArgument(models.length > 0, "Cannot set models to empty array");
        Preconditions.checkArgument(state.owner == owner, "Cannot set models for a different block");
        if (!this.models.containsKey(state)) {
            Preconditions.checkArgument(disjointToAll(state),
                    "Cannot set models for a state for which a partial match has already been configured");
            this.models.put(state, new BlockStateProvider.ConfiguredModelList(models));
            for (BlockState fullState : owner.getStateDefinition().getPossibleStates()) {
                if (state.test(fullState)) coveredStates.add(fullState);
            }
        } else {
            this.models.get(state).append(models);
        }
        return this;
    }

    public VariantBlockStateBuilder setModels(PartialBlockstate state, ConfiguredModel... models) {
        Preconditions.checkArgument(!this.models.containsKey(state),
                "Cannot set models for a state that has already been configured: %s", state);
        return addModels(state, models);
    }

    private boolean disjointToAll(PartialBlockstate newState) {
        return coveredStates.stream().noneMatch(newState);
    }

    public PartialBlockstate partialState() {
        return new PartialBlockstate(owner, this);
    }

    public VariantBlockStateBuilder forAllStates(Function<BlockState, ConfiguredModel[]> mapper) {
        return forAllStatesExcept(mapper);
    }

    public VariantBlockStateBuilder forAllStatesExcept(Function<BlockState, ConfiguredModel[]> mapper,
                                                       Property<?>... ignored) {
        Set<PartialBlockstate> seen = new HashSet<>();
        for (BlockState fullState : owner.getStateDefinition().getPossibleStates()) {
            Map<Property<?>, Comparable<?>> values = fullState.getValues()
                    .collect(Collectors.toMap(Property.Value::property, Property.Value::value, (a, b) -> b,
                            Maps::newLinkedHashMap));
            for (Property<?> property : ignored) values.remove(property);
            PartialBlockstate partialState = new PartialBlockstate(owner, values, this);
            if (seen.add(partialState)) setModels(partialState, mapper.apply(fullState));
        }
        return this;
    }

    public static class PartialBlockstate implements Predicate<BlockState> {

        private final Block owner;
        private final SortedMap<Property<?>, Comparable<?>> setStates;
        @Nullable
        private final VariantBlockStateBuilder outerBuilder;

        PartialBlockstate(Block owner, @Nullable VariantBlockStateBuilder outerBuilder) {
            this(owner, ImmutableMap.of(), outerBuilder);
        }

        PartialBlockstate(Block owner, Map<Property<?>, Comparable<?>> setStates,
                          @Nullable VariantBlockStateBuilder outerBuilder) {
            this.owner = owner;
            this.outerBuilder = outerBuilder;
            this.setStates = Maps.newTreeMap(Comparator.comparing(Property::getName));
            this.setStates.putAll(setStates);
        }

        public <T extends Comparable<T>> PartialBlockstate with(Property<T> prop, T value) {
            Map<Property<?>, Comparable<?>> copy = new HashMap<>(setStates);
            copy.put(prop, value);
            return new PartialBlockstate(owner, copy, outerBuilder);
        }

        public ConfiguredModel.Builder<VariantBlockStateBuilder> modelForState() {
            Preconditions.checkNotNull(outerBuilder, "Partial blockstate must have an owner");
            return ConfiguredModel.builder(outerBuilder, this);
        }

        public PartialBlockstate addModels(ConfiguredModel... models) {
            Preconditions.checkNotNull(outerBuilder, "Partial blockstate must have an owner");
            outerBuilder.addModels(this, models);
            return this;
        }

        public VariantBlockStateBuilder setModels(ConfiguredModel... models) {
            Preconditions.checkNotNull(outerBuilder, "Partial blockstate must have an owner");
            return outerBuilder.setModels(this, models);
        }

        public PartialBlockstate partialState() {
            Preconditions.checkNotNull(outerBuilder, "Partial blockstate must have an owner");
            return outerBuilder.partialState();
        }

        @Override
        public boolean test(BlockState state) {
            if (!state.is(owner)) return false;
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                if (state.getValue(entry.getKey()) != entry.getValue()) return false;
            }
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PartialBlockstate that = (PartialBlockstate) obj;
            return owner.equals(that.owner) && setStates.equals(that.setStates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, setStates);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                if (!ret.isEmpty()) ret.append(',');
                ret.append(entry.getKey().getName())
                        .append('=')
                        .append(((Property) entry.getKey()).getName(entry.getValue()));
            }
            return ret.toString();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        static Comparator<PartialBlockstate> comparingByProperties() {
            return (left, right) -> {
                SortedSet<Property<?>> properties = new TreeSet<>(left.setStates.comparator().reversed());
                properties.addAll(left.setStates.keySet());
                properties.addAll(right.setStates.keySet());
                int total = 0;
                for (Property<?> property : properties) {
                    Comparable l = left.setStates.get(property);
                    Comparable r = right.setStates.get(property);
                    if (l == r) continue;
                    if (l == null) total -= 1;
                    else if (r == null) total += 1;
                    else total += l.compareTo(r);
                }
                return total;
            };
        }
    }
}

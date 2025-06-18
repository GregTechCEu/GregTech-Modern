package com.gregtechceu.gtceu.data.model.builder;

import com.google.gson.JsonPrimitive;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;

import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class MachineModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    // spotless:off
    public static <T extends ModelBuilder<T>> BiFunction<T, ExistingFileHelper, MachineModelBuilder<T>> begin(MachineDefinition owner) {
        return (parent, existingFileHelper) -> new MachineModelBuilder<>(parent, existingFileHelper, owner);
    }
    // spotless:on

    @Getter
    private final MachineDefinition owner;
    private final List<DynamicRender<?, ?>> dynamicRenders = new ArrayList<>();
    @Getter
    private final Map<PartialState<T>, ModelFile> models = new LinkedHashMap<>();
    private final Set<MachineRenderState> coveredStates = new HashSet<>();

    protected MachineModelBuilder(T parent, ExistingFileHelper existingFileHelper, MachineDefinition owner) {
        super(GTMachineModels.MACHINE_MODEL_LOADER, parent, existingFileHelper);
        this.owner = owner;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json.addProperty("machine", owner.getId().toString());

        List<MachineRenderState> missingStates = Lists.newArrayList(owner.getStateDefinition().getPossibleStates());
        missingStates.removeAll(coveredStates);
        Preconditions.checkState(missingStates.isEmpty(),
                "Render state for machine %s does not cover all states. Missing: %s", owner, missingStates);
        JsonObject variants = new JsonObject();
        getModels().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(PartialState.comparingByProperties()))
                .forEach(entry -> variants.add(entry.getKey().toString(), modelToJson(entry.getValue())));

        json.add("variants", variants);

        JsonArray dynamicRenders = new JsonArray();
        for (DynamicRender<?, ?> render : this.dynamicRenders) {
            JsonElement serialized = DynamicRender.CODEC.encodeStart(JsonOps.INSTANCE, render)
                    .getOrThrow(false, GTCEu.LOGGER::error);
            dynamicRenders.add(serialized);
        }
        json.add("dynamic_renders", dynamicRenders);

        return json;
    }

    public static JsonElement modelToJson(ModelFile model) {
        if (model instanceof ModelBuilder<?> builder) {
            return builder.toJson();
        } else {
            return new JsonPrimitive(model.getLocation().toString());
        }
    }

    /**
     * Add a {@link DynamicRender dynamic render} to this model.
     *
     * @param render  The {@link DynamicRender dynamic render} to add
     */
    public MachineModelBuilder<T> addDynamicRenderer(DynamicRender<?, ?> render) {
        this.dynamicRenders.add(render);
        return this;
    }

    /**
     * Assign some models to a given {@link PartialState partial state}.
     *
     * @param state  The {@link PartialState partial state} for which to add
     *               the models
     * @param model A set of models to add to this state
     * @return this builder
     * @throws NullPointerException     if {@code state} is {@code null}
     * @throws IllegalArgumentException if {@code model} is {@code null}
     * @throws IllegalArgumentException if {@code state}'s owning block differs from
     *                                  the builder's
     * @throws IllegalArgumentException if {@code state} partially matches another
     *                                  state which has already been configured
     */
    public MachineModelBuilder<T> addModel(PartialState<T> state, ModelFile model) {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkNotNull(model, "model must not be null");
        Preconditions.checkArgument(state.getOwner() == owner,
                "Cannot set model for a different block. Found: %s, Current: %s", state.getOwner(), owner);
        Preconditions.checkArgument(disjointToAll(state) && !this.models.containsKey(state),
                "Cannot set model for a state for which a partial match has already been configured");
        this.models.put(state, model);
        for (MachineRenderState fullState : owner.getStateDefinition().getPossibleStates()) {
            if (state.test(fullState)) {
                coveredStates.add(fullState);
            }
        }
        return this;
    }

    /**
     * Assign some models to a given {@link PartialState partial state},
     * throwing an exception if the state has already been configured. Otherwise,
     * simply calls {@link #addModel(PartialState, ModelFile)}.
     *
     * @param state The {@link PartialState partial state} for which to set
     *              the models
     * @param model A set of models to assign to this state
     * @return this builder
     * @throws IllegalArgumentException if {@code state} has already been configured
     * @see #addModel(PartialState, ModelFile)
     */
    public MachineModelBuilder<T> setModels(PartialState<T> state, ModelFile model) {
        Preconditions.checkArgument(!models.containsKey(state),
                "Cannot set model for a state that has already been configured: %s", state);
        addModel(state, model);
        return this;
    }

    private boolean disjointToAll(PartialState<T> newState) {
        return coveredStates.stream().noneMatch(newState);
    }

    public PartialState<T> partialState() {
        return new PartialState<>(owner, this);
    }

    public MachineModelBuilder<T> forAllStates(Function<MachineRenderState, ModelFile> mapper) {
        return forAllStatesExcept(mapper);
    }

    public MachineModelBuilder<T> forAllStatesExcept(Function<MachineRenderState, ModelFile> mapper,
                                                     Property<?>... ignored) {
        Set<PartialState<T>> seen = new HashSet<>();
        for (MachineRenderState fullState : owner.getStateDefinition().getPossibleStates()) {
            Map<Property<?>, Comparable<?>> propertyValues = Maps.newLinkedHashMap(fullState.getValues());
            for (Property<?> p : ignored) {
                propertyValues.remove(p);
            }
            PartialState<T> partialState = new PartialState<>(owner, propertyValues, this);
            if (seen.add(partialState)) {
                setModels(partialState, mapper.apply(fullState));
            }
        }
        return this;
    }

    public static class PartialState<B extends ModelBuilder<B>> implements Predicate<MachineRenderState> {

        @Getter
        private final MachineDefinition owner;
        @Getter
        private final SortedMap<Property<?>, Comparable<?>> setStates;
        @Nullable
        private final MachineModelBuilder<B> outerBuilder;

        PartialState(MachineDefinition owner, @Nullable MachineModelBuilder<B> outerBuilder) {
            this(owner, ImmutableMap.of(), outerBuilder);
        }

        PartialState(MachineDefinition owner, Map<Property<?>, Comparable<?>> setStates,
                     @Nullable MachineModelBuilder<B> outerBuilder) {
            this.owner = owner;
            this.outerBuilder = outerBuilder;
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                Property<?> prop = entry.getKey();
                Comparable<?> value = entry.getValue();
                Preconditions.checkArgument(owner.getStateDefinition().getProperties().contains(prop),
                        "Property %s not found on machine %s", entry, this.owner);
                Preconditions.checkArgument(prop.getPossibleValues().contains(value),
                        "%s is not a valid value for %s", value, prop);
            }
            this.setStates = Maps.newTreeMap(Comparator.comparing(Property::getName));
            this.setStates.putAll(setStates);
        }

        public <T extends Comparable<T>> PartialState<B> with(Property<T> prop, T value) {
            Preconditions.checkArgument(!setStates.containsKey(prop), "Property %s has already been set", prop);
            Map<Property<?>, Comparable<?>> newState = new HashMap<>(setStates);
            newState.put(prop, value);
            return new PartialState<>(owner, newState, outerBuilder);
        }

        private void checkValidOwner() {
            Preconditions.checkNotNull(outerBuilder,
                    "Partial MachineRenderState must have a valid owner to perform this action");
        }

        /**
         * Set this variant's model, and return the parent builder.
         *
         * @param model The model to set
         * @return The parent builder instance
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public MachineModelBuilder<B> setModel(ModelFile model) {
            checkValidOwner();
            return outerBuilder.setModels(this, model);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartialState<?> that = (PartialState<?>) o;
            return owner.equals(that.owner) &&
                    setStates.equals(that.setStates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, setStates);
        }

        @Override
        public boolean test(MachineRenderState state) {
            if (state.getDefinition() != getOwner()) {
                return false;
            }
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                if (state.getValue(entry.getKey()) != entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                if (!ret.isEmpty()) {
                    ret.append(',');
                }
                ret.append(entry.getKey().getName())
                        .append('=')
                        .append(((Property) entry.getKey()).getName(entry.getValue()));
            }
            return ret.toString();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static Comparator<PartialState<?>> comparingByProperties() {
            // Sort variants inversely by property values, to approximate vanilla style
            return (s1, s2) -> {
                SortedSet<Property<?>> propUniverse = new TreeSet<>(s1.getSetStates().comparator().reversed());
                propUniverse.addAll(s1.getSetStates().keySet());
                propUniverse.addAll(s2.getSetStates().keySet());
                for (Property<?> prop : propUniverse) {
                    Comparable val1 = s1.getSetStates().get(prop);
                    Comparable val2 = s2.getSetStates().get(prop);
                    if (val1 == null && val2 != null) {
                        return -1;
                    } else if (val2 == null && val1 != null) {
                        return 1;
                    } else if (val1 != null && val2 != null) {
                        int cmp = val1.compareTo(val2);
                        if (cmp != 0) {
                            return cmp;
                        }
                    }
                }
                return 0;
            };
        }
    }

}

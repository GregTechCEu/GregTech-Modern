package com.gregtechceu.gtceu.data.model.builder;

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
import com.google.common.collect.ImmutableList;
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
    private final Map<PartialState<T>, ConfiguredModelList> models = new LinkedHashMap<>();
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
                .forEach(entry -> variants.add(entry.getKey().toString(), entry.getValue().toJSON()));

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
     * @param models A set of models to add to this state
     * @return this builder
     * @throws NullPointerException     if {@code state} is {@code null}
     * @throws IllegalArgumentException if {@code models} is empty
     * @throws IllegalArgumentException if {@code state}'s owning block differs from
     *                                  the builder's
     * @throws IllegalArgumentException if {@code state} partially matches another
     *                                  state which has already been configured
     */
    public MachineModelBuilder<T> addModels(PartialState<T> state, ConfiguredMachineModel... models) {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkArgument(models.length > 0, "Cannot set models to empty array");
        Preconditions.checkArgument(state.getOwner() == owner,
                "Cannot set models for a different block. Found: %s, Current: %s", state.getOwner(), owner);
        if (!this.models.containsKey(state)) {
            Preconditions.checkArgument(disjointToAll(state),
                    "Cannot set models for a state for which a partial match has already been configured");
            this.models.put(state, new ConfiguredModelList(models));
            for (MachineRenderState fullState : owner.getStateDefinition().getPossibleStates()) {
                if (state.test(fullState)) {
                    coveredStates.add(fullState);
                }
            }
        } else {
            // noinspection DataFlowIssue we check if it exists right above.
            this.models.compute(state, ($, cml) -> cml.append(models));
        }
        return this;
    }

    /**
     * Assign some models to a given {@link PartialState partial state},
     * throwing an exception if the state has already been configured. Otherwise,
     * simply calls {@link #addModels(PartialState, ConfiguredMachineModel...)}.
     *
     * @param state The {@link PartialState partial state} for which to set
     *              the models
     * @param model A set of models to assign to this state
     * @return this builder
     * @throws IllegalArgumentException if {@code state} has already been configured
     * @see #addModels(PartialState, ConfiguredMachineModel...)
     */
    public MachineModelBuilder<T> setModels(PartialState<T> state, ConfiguredMachineModel... model) {
        Preconditions.checkArgument(!models.containsKey(state),
                "Cannot set models for a state that has already been configured: %s", state);
        addModels(state, model);
        return this;
    }

    private boolean disjointToAll(PartialState<T> newState) {
        return coveredStates.stream().noneMatch(newState);
    }

    public PartialState<T> partialState() {
        return new PartialState<>(owner, this);
    }

    public MachineModelBuilder<T> forAllStates(Function<MachineRenderState, ConfiguredMachineModel[]> mapper) {
        return forAllStatesExcept(mapper);
    }

    public MachineModelBuilder<T> forAllStatesExcept(Function<MachineRenderState, ConfiguredMachineModel[]> mapper,
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
         * Creates a builder for models to assign to this state, which when completed
         * via {@link ConfiguredMachineModel.Builder#addModel()} will assign the resultant set
         * of models to this state.
         *
         * @return the model builder
         * @see ConfiguredMachineModel.Builder
         */
        public ConfiguredMachineModel.Builder<MachineModelBuilder<B>> modelForState() {
            checkValidOwner();
            return ConfiguredMachineModel.builder(outerBuilder, this);
        }

        /**
         * Add models to the current state's variant. For use when it is more convenient
         * to add multiple sets of models, as a replacement for
         * {@link #setModels(ConfiguredMachineModel...)}.
         *
         * @param models The models to add.
         * @return {@code this}
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public PartialState<B> addModels(ConfiguredMachineModel... models) {
            checkValidOwner();
            outerBuilder.addModels(this, models);
            return this;
        }

        /**
         * Set this variant's models, and return the parent builder.
         *
         * @param models The models to set
         * @return The parent builder instance
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public MachineModelBuilder<B> setModels(ConfiguredMachineModel... models) {
            checkValidOwner();
            return outerBuilder.setModels(this, models);
        }

        /**
         * Complete this state without adding any new models, and return a new partial
         * state via the parent builder. For use after calling
         * {@link #addModels(ConfiguredMachineModel...)}.
         *
         * @return A fresh partial state as specified by
         *         {@link MachineModelBuilder#partialState()}.
         * @throws NullPointerException If the parent builder is {@code null}
         */
        public PartialState<B> partialState() {
            checkValidOwner();
            return outerBuilder.partialState();
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

    public static class ConfiguredModelList {

        private final List<ConfiguredMachineModel> models;

        private ConfiguredModelList(List<ConfiguredMachineModel> models) {
            Preconditions.checkArgument(!models.isEmpty());
            this.models = models;
        }

        public ConfiguredModelList(ConfiguredMachineModel model) {
            this(ImmutableList.of(model));
        }

        public ConfiguredModelList(ConfiguredMachineModel... models) {
            this(Arrays.asList(models));
        }

        public JsonElement toJSON() {
            if (models.size() == 1) {
                return models.get(0).toJSON(false);
            } else {
                JsonArray ret = new JsonArray();
                for (ConfiguredMachineModel m : models) {
                    ret.add(m.toJSON(true));
                }
                return ret;
            }
        }

        public ConfiguredModelList append(ConfiguredMachineModel... models) {
            return new ConfiguredModelList(ImmutableList.<ConfiguredMachineModel>builder()
                    .addAll(this.models).add(models)
                    .build());
        }
    }
}

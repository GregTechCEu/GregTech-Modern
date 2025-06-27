package com.gregtechceu.gtceu.data.model.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
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
    @Getter
    private final List<PartBuilder> parts = new ArrayList<>();
    private final Set<MachineRenderState> coveredStates = new HashSet<>();
    private final List<String> replaceableTextures = new ArrayList<>();
    private final SortedMap<String, ResourceLocation> textureOverrides = new TreeMap<>();

    protected MachineModelBuilder(T parent, ExistingFileHelper existingFileHelper, MachineDefinition owner) {
        super(MachineModelLoader.ID, parent, existingFileHelper);
        this.owner = owner;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        json.addProperty("machine", owner.getId().toString());
        StateDefinition<MachineDefinition, MachineRenderState> stateDefinition = owner.getStateDefinition();
        if (getModels().isEmpty() && getParts().isEmpty()) {
            throw new IllegalStateException("A machine model must have either a variant or multipart model!");
        }

        if (!getModels().isEmpty()) {
            List<MachineRenderState> missingStates = Lists.newArrayList(stateDefinition.getPossibleStates());
            missingStates.removeAll(coveredStates);
            Preconditions.checkState(missingStates.isEmpty(),
                    "Render state for machine %s does not cover all states. Missing: %s", owner, missingStates);
            JsonObject variants = new JsonObject();
            getModels().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(PartialState.comparingByProperties()))
                    .forEach(entry -> variants.add(entry.getKey().toString(), modelToJson(entry.getValue())));
            json.add("variants", variants);
        }
        if (!getParts().isEmpty()) {
            JsonArray parts = new JsonArray();
            for (PartBuilder part : getParts()) {
                parts.add(part.toJson());
            }
            json.add("multipart", parts);
        }

        if (!this.dynamicRenders.isEmpty()) {
            JsonArray dynamicRenders = new JsonArray();
            for (DynamicRender<?, ?> render : this.dynamicRenders) {
                JsonElement serialized = DynamicRender.CODEC.encodeStart(JsonOps.INSTANCE, render)
                        .getOrThrow(false, GTCEu.LOGGER::error);
                dynamicRenders.add(serialized);
            }
            json.add("dynamic_renders", dynamicRenders);
        }

        if (!this.replaceableTextures.isEmpty()) {
            JsonArray replaceableTextures = new JsonArray();
            for (String material : this.replaceableTextures) {
                replaceableTextures.add(material);
            }
            json.add("replaceable_textures", replaceableTextures);
        }

        if (!this.textureOverrides.isEmpty()) {
            JsonObject overrides = new JsonObject();
            for (var entry : this.textureOverrides.entrySet()) {
                overrides.addProperty(entry.getKey(), entry.getValue().toString());
            }
            json.add("texture_overrides", overrides);
        }

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
     * @param render The {@link DynamicRender dynamic render} to add
     */
    public MachineModelBuilder<T> addDynamicRenderer(DynamicRender<?, ?> render) {
        this.dynamicRenders.add(render);
        return this;
    }

    /**
     * Marks the provided texture names as replaceable by multiblocks' casing textures.
     *
     * @param textureNames The texture names
     */
    public MachineModelBuilder<T> addReplaceableTextures(String... textureNames) {
        this.replaceableTextures.addAll(Arrays.asList(textureNames));
        return this;
    }

    /**
     * Adds a texture of this model as one that will replace multiblock parts in formed multiblocks.
     *
     * @param texture  The name of the texture in this model
     * @param material The texture to replace
     */
    public MachineModelBuilder<T> addTextureOverride(String material, ResourceLocation texture) {
        this.textureOverrides.put(material, texture);
        return this;
    }

    /**
     * Assign a model to a given {@link PartialState partial state}.
     *
     * @param state The {@link PartialState partial state} for which to add the models
     * @param model A model to add to this state
     * @return this builder
     * @throws NullPointerException     if {@code state} is {@code null}
     * @throws NullPointerException     if {@code model} is {@code null}
     * @throws IllegalArgumentException if {@code state}'s owning block differs from the builder's
     */
    public MachineModelBuilder<T> replaceModel(PartialState<T> state, ModelFile model) {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkNotNull(model, "model must not be null");
        Preconditions.checkArgument(state.getOwner() == owner,
                "Cannot set model for a different block. Found: %s, Current: %s", state.getOwner(), owner);
        this.models.put(state, model);
        for (MachineRenderState fullState : owner.getStateDefinition().getPossibleStates()) {
            if (state.test(fullState)) {
                coveredStates.add(fullState);
            }
        }
        return this;
    }

    /**
     * Assign a model to a given {@link PartialState partial state}.
     *
     * @param state The {@link PartialState partial state} for which to add the model
     * @param model A model to add to this state
     * @return this builder
     * @throws IllegalArgumentException if {@code state} partially matches another
     *                                  state which has already been configured
     * @see #replaceModel(PartialState, ModelFile)
     */
    public MachineModelBuilder<T> addModel(PartialState<T> state, ModelFile model) {
        Preconditions.checkArgument(disjointToAll(state),
                "Cannot set model for a state for which a partial match has already been configured");
        replaceModel(state, model);
        return this;
    }

    /**
     * Assign a model to a given {@link PartialState partial state},
     * throwing an exception if the state has already been configured. Otherwise,
     * simply calls {@link #addModel(PartialState, ModelFile)}.
     *
     * @param state The {@link PartialState partial state} for which to set the model
     * @param model A model to assign to this state
     * @return this builder
     * @throws IllegalArgumentException if {@code state} has already been configured
     * @see #addModel(PartialState, ModelFile)
     */
    public MachineModelBuilder<T> setModel(PartialState<T> state, ModelFile model) {
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

    /**
     * Creates a {@link PartBuilder} with the passed model and returns it for further processing.
     *
     * @param model the model to use
     * @return the model builder
     * @see MachineModelBuilder#part(ResourceLocation)
     */
    public PartBuilder part(ModelFile model) {
        PartBuilder part = new PartBuilder(model);
        this.parts.add(part);
        return part;
    }

    /**
     * Creates a {@link PartBuilder} with an existing model and returns it for further processing.
     *
     * @param model an existing model's name
     * @return the model builder
     * @see MachineModelBuilder#part(ModelFile)
     */
    public PartBuilder part(ResourceLocation model) {
        return part(new ModelFile.ExistingModelFile(model, existingFileHelper));
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
                setModel(partialState, mapper.apply(fullState));
            }
        }
        return this;
    }

    // spotless:off
    public MachineModelBuilder<T> replaceForAllStates(BiFunction<MachineRenderState, @Nullable ModelFile, ModelFile> mapper) {
        return replaceForAllStatesExcept(mapper);
    }

    public MachineModelBuilder<T> replaceForAllStatesExcept(BiFunction<MachineRenderState, @Nullable ModelFile, ModelFile> mapper,
                                                            Property<?>... ignored) {
        Set<PartialState<T>> seen = new HashSet<>();
        for (MachineRenderState fullState : owner.getStateDefinition().getPossibleStates()) {
            Map<Property<?>, Comparable<?>> propertyValues = Maps.newLinkedHashMap(fullState.getValues());
            for (Property<?> p : ignored) {
                propertyValues.remove(p);
            }
            PartialState<T> partialState = new PartialState<>(owner, propertyValues, this);
            if (seen.add(partialState)) {
                replaceModel(partialState, mapper.apply(fullState, getModels().get(partialState)));
            }
        }
        return this;
    }
    // spotless:on

    public static class PartialState<B extends ModelBuilder<B>> implements Predicate<MachineRenderState> {

        @Getter
        private final MachineDefinition owner;
        @Getter
        private final SortedMap<Property<?>, Comparable<?>> setStates;
        @Nullable
        private final MachineModelBuilder<B> outerBuilder;

        private PartialState(MachineDefinition owner, @Nullable MachineModelBuilder<B> outerBuilder) {
            this(owner, ImmutableMap.of(), outerBuilder);
        }

        private PartialState(MachineDefinition owner, Map<Property<?>, Comparable<?>> setStates,
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
            return outerBuilder.setModel(this, model);
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

    public class PartBuilder {

        public ModelFile model;
        public boolean useOr;
        public final Multimap<Property<?>, Comparable<?>> conditions = MultimapBuilder.linkedHashKeys()
                .arrayListValues().build();
        public final List<ConditionGroup> nestedConditionGroups = new ArrayList<>();

        private PartBuilder(ModelFile model) {
            this.model = model;
        }

        /**
         * Makes this part get applied if any of the conditions/condition groups are true,
         * instead of all of them needing to be true.
         */
        public PartBuilder useOr() {
            this.useOr = true;
            return this;
        }

        /**
         * Set a condition for this part, which consists of a property and a set of
         * valid values. Can be called multiple times for multiple different properties.
         *
         * @param <T>    the type of the property value
         * @param prop   the property
         * @param values a set of valid values
         * @return this builder
         * @throws NullPointerException     if {@code prop} is {@code null}
         * @throws NullPointerException     if {@code values} is {@code null}
         * @throws IllegalArgumentException if {@code values} is empty
         * @throws IllegalArgumentException if {@code prop} is not applicable to the current machine's state
         */
        @SafeVarargs
        private final <T extends Comparable<T>> PartBuilder replaceWithCondition(Property<T> prop, T... values) {
            Preconditions.checkNotNull(prop, "Property must not be null");
            Preconditions.checkNotNull(values, "Value list must not be null");
            Preconditions.checkArgument(values.length > 0, "Value list must not be empty");
            Preconditions.checkArgument(canApplyTo(owner), "Property %s is not valid for machine %s", prop, owner);
            this.nestedConditionGroups.clear();
            this.conditions.putAll(prop, Arrays.asList(values));
            return this;
        }

        /**
         * Set a condition for this part, which consists of a property and a set of
         * valid values. Can be called multiple times for multiple different properties.
         *
         * @param <T>    the type of the property value
         * @param prop   the property
         * @param values a set of valid values
         * @return this builder
         * @throws IllegalArgumentException if {@code prop} has already been configured
         * @throws IllegalStateException    if {@code !nestedConditionGroups.isEmpty()}
         * @see PartBuilder#replaceWithCondition(Property, Comparable[])
         */
        @SafeVarargs
        public final <T extends Comparable<T>> PartBuilder condition(Property<T> prop, T... values) {
            Preconditions.checkArgument(!conditions.containsKey(prop),
                    "Cannot set condition for property \"%s\" more than once", prop.getName());
            Preconditions.checkState(nestedConditionGroups.isEmpty(),
                    "Can't have normal conditions if there are already nested condition groups");
            return this.replaceWithCondition(prop, values);
        }

        /**
         * Allows having nested groups of conditions.
         */
        private final ConditionGroup replaceWithNestedGroup() {
            this.conditions.clear();
            ConditionGroup group = new ConditionGroup();
            this.nestedConditionGroups.add(group);
            return group;
        }

        /**
         * Allows having nested groups of conditions if there are not any normal conditions.
         * 
         * @throws IllegalStateException if {@code !conditions.isEmpty()}
         * @see PartBuilder#replaceWithNestedGroup()
         */
        public final ConditionGroup nestedGroup() {
            Preconditions.checkState(conditions.isEmpty(),
                    "Can't have nested condition groups if there are already normal conditions");
            return replaceWithNestedGroup();
        }

        public MachineModelBuilder<T> end() {
            return MachineModelBuilder.this;
        }

        public JsonObject toJson() {
            JsonObject out = new JsonObject();
            if (!conditions.isEmpty()) {
                out.add("when", conditionsToJson(this.conditions, this.useOr));
            } else if (!nestedConditionGroups.isEmpty()) {
                out.add("when", groupsToJson(this.nestedConditionGroups, this.useOr));
            }
            out.add("apply", modelToJson(this.model));
            return out;
        }

        public boolean canApplyTo(MachineDefinition b) {
            return b.getStateDefinition().getProperties().containsAll(this.conditions.keySet());
        }

        private JsonObject groupsToJson(List<ConditionGroup> conditions, boolean useOr) {
            JsonObject groupJson = new JsonObject();
            JsonArray innerGroupJson = new JsonArray();
            groupJson.add(useOr ? "OR" : "AND", innerGroupJson);
            for (ConditionGroup group : conditions) {
                innerGroupJson.add(group.toJson());
            }
            return groupJson;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private JsonObject conditionsToJson(Multimap<Property<?>, Comparable<?>> conditions, boolean useOr) {
            JsonObject groupJson = new JsonObject();
            for (var entry : conditions.asMap().entrySet()) {
                StringBuilder activeString = new StringBuilder();
                for (Comparable<?> val : entry.getValue()) {
                    if (!activeString.isEmpty()) activeString.append("|");
                    activeString.append(((Property) entry.getKey()).getName(val));
                }
                groupJson.addProperty(entry.getKey().getName(), activeString.toString());
            }
            if (useOr) {
                JsonArray innerWhen = new JsonArray();
                for (var entry : groupJson.entrySet()) {
                    JsonObject obj = new JsonObject();
                    obj.add(entry.getKey(), entry.getValue());
                    innerWhen.add(obj);
                }
                groupJson = new JsonObject();
                groupJson.add("OR", innerWhen);
            }
            return groupJson;
        }

        public class ConditionGroup {

            public final Multimap<Property<?>, Comparable<?>> conditions = MultimapBuilder.linkedHashKeys()
                    .arrayListValues()
                    .build();
            public final List<ConditionGroup> nestedConditionGroups = new ArrayList<>();
            private ConditionGroup parent = null;
            public boolean useOr;

            /**
             * Set a condition for this part, which consists of a property and a set of
             * valid values. Can be called multiple times for multiple different properties.
             *
             * @param <T>    the type of the property value
             * @param prop   the property
             * @param values a set of valid values
             * @return this builder
             * @throws NullPointerException     if {@code prop} is {@code null}
             * @throws NullPointerException     if {@code values} is {@code null}
             * @throws IllegalArgumentException if {@code values} is empty
             * @throws IllegalArgumentException if {@code prop} is not applicable to the current machine's state
             */
            @SafeVarargs
            private final <T extends Comparable<T>> ConditionGroup replaceWithCondition(Property<T> prop, T... values) {
                Preconditions.checkNotNull(prop, "Property must not be null");
                Preconditions.checkNotNull(values, "Value list must not be null");
                Preconditions.checkArgument(values.length > 0, "Value list must not be empty");
                Preconditions.checkArgument(canApplyTo(owner),
                        "Property %s is not valid for machine %s", prop, owner);
                this.nestedConditionGroups.clear();
                this.conditions.putAll(prop, Arrays.asList(values));
                return this;
            }

            /**
             * Set a condition for this part, which consists of a property and a set of
             * valid values. Can be called multiple times for multiple different properties.
             *
             * @param <T>    the type of the property value
             * @param prop   the property
             * @param values a set of valid values
             * @return this builder
             * @throws IllegalArgumentException if {@code prop} has already been configured
             * @throws IllegalStateException    if {@code !nestedConditionGroups.isEmpty()}
             * @see ConditionGroup#replaceWithCondition(Property, Comparable[])
             */
            @SafeVarargs
            public final <T extends Comparable<T>> ConditionGroup condition(Property<T> prop, T... values) {
                Preconditions.checkArgument(!conditions.containsKey(prop),
                        "Cannot set condition for property \"%s\" more than once", prop.getName());
                Preconditions.checkState(nestedConditionGroups.isEmpty(),
                        "Can't have normal conditions if there are already nested condition groups");
                return this.replaceWithCondition(prop, values);
            }

            /**
             * Allows having nested groups of conditions.
             */
            private final ConditionGroup replaceWithNestedGroup() {
                this.conditions.clear();
                ConditionGroup group = new ConditionGroup();
                group.parent = this;
                this.nestedConditionGroups.add(group);
                return group;
            }

            /**
             * Allows having nested groups of conditions if there are not any normal conditions.
             * 
             * @throws IllegalStateException if {@code !conditions.isEmpty()}
             * @see ConditionGroup#replaceWithNestedGroup()
             */
            public final ConditionGroup nestedGroup() {
                Preconditions.checkState(conditions.isEmpty(),
                        "Can't have nested condition groups if there are already normal conditions");
                return replaceWithNestedGroup();
            }

            /**
             * Ends this nested condition group and returns the parent condition group
             *
             * @throws IllegalStateException If this is not a nested condition group
             */
            public ConditionGroup endNestedGroup() {
                if (parent == null)
                    throw new IllegalStateException("This condition group is not nested, use end() instead");
                return parent;
            }

            /**
             * Ends this condition group and returns the part builder
             *
             * @throws IllegalStateException If this is a nested condition group
             */
            public PartBuilder end() {
                if (this.parent != null)
                    throw new IllegalStateException("This is a nested condition group, use endNestedGroup() instead");
                return PartBuilder.this;
            }

            /**
             * Makes this part get applied if any of the conditions/condition groups are true, instead of all of them
             * needing to be true.
             */
            public ConditionGroup useOr() {
                this.useOr = true;
                return this;
            }

            public JsonObject toJson() {
                if (!this.conditions.isEmpty()) {
                    return conditionsToJson(this.conditions, this.useOr);
                } else if (!this.nestedConditionGroups.isEmpty()) {
                    return groupsToJson(this.nestedConditionGroups, this.useOr);
                }
                return new JsonObject();
            }
        }
    }
}

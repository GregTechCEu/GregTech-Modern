package net.neoforged.neoforge.client.model.generators;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public final class MultiPartBlockStateBuilder implements IGeneratedBlockState {

    private final List<PartBuilder> parts = new ArrayList<>();
    private final Block owner;

    public MultiPartBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    public ConfiguredModel.Builder<PartBuilder> part() {
        return ConfiguredModel.builder(this);
    }

    MultiPartBlockStateBuilder addPart(PartBuilder part) {
        parts.add(part);
        return this;
    }

    @Override
    public JsonObject toJson() {
        JsonArray variants = new JsonArray();
        for (PartBuilder part : parts) variants.add(part.toJson());
        JsonObject main = new JsonObject();
        main.add("multipart", variants);
        return main;
    }

    public class PartBuilder {

        public BlockStateProvider.ConfiguredModelList models;
        public boolean useOr;
        public final Multimap<Property<?>, Comparable<?>> conditions = MultimapBuilder.linkedHashKeys()
                .arrayListValues().build();
        public final List<ConditionGroup> nestedConditionGroups = new ArrayList<>();

        PartBuilder(BlockStateProvider.ConfiguredModelList models) {
            this.models = models;
        }

        public PartBuilder useOr() {
            this.useOr = true;
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> PartBuilder condition(Property<T> prop, T... values) {
            Preconditions.checkArgument(owner.getStateDefinition().getProperties().contains(prop),
                    "Property %s is not valid for block %s", prop, owner);
            conditions.putAll(prop, Arrays.asList(values));
            return this;
        }

        public final ConditionGroup nestedGroup() {
            Preconditions.checkState(conditions.isEmpty(),
                    "Can't have nested condition groups if there are already normal conditions");
            ConditionGroup group = new ConditionGroup();
            nestedConditionGroups.add(group);
            return group;
        }

        public MultiPartBlockStateBuilder end() {
            return MultiPartBlockStateBuilder.this;
        }

        JsonObject toJson() {
            JsonObject out = new JsonObject();
            if (!conditions.isEmpty()) {
                out.add("when", conditionsToJson(conditions, useOr));
            } else if (!nestedConditionGroups.isEmpty()) {
                out.add("when", groupsToJson(nestedConditionGroups, useOr));
            }
            out.add("apply", models.toJSON());
            return out;
        }

        public class ConditionGroup {

            public final Multimap<Property<?>, Comparable<?>> conditions = MultimapBuilder.linkedHashKeys()
                    .arrayListValues().build();
            public final List<ConditionGroup> nestedConditionGroups = new ArrayList<>();
            private ConditionGroup parent = null;
            public boolean useOr;

            @SafeVarargs
            public final <T extends Comparable<T>> ConditionGroup condition(Property<T> prop, T... values) {
                conditions.putAll(prop, Arrays.asList(values));
                return this;
            }

            public ConditionGroup nestedGroup() {
                Preconditions.checkState(conditions.isEmpty(),
                        "Can't have nested condition groups if there are already normal conditions");
                ConditionGroup group = new ConditionGroup();
                group.parent = this;
                nestedConditionGroups.add(group);
                return group;
            }

            public ConditionGroup endNestedGroup() {
                if (parent == null) throw new IllegalStateException("This condition group is not nested");
                return parent;
            }

            public PartBuilder end() {
                if (parent != null) throw new IllegalStateException("This is a nested condition group");
                return PartBuilder.this;
            }

            public ConditionGroup useOr() {
                this.useOr = true;
                return this;
            }

            JsonObject toJson() {
                if (!conditions.isEmpty()) return conditionsToJson(conditions, useOr);
                if (!nestedConditionGroups.isEmpty()) return groupsToJson(nestedConditionGroups, useOr);
                return new JsonObject();
            }
        }
    }

    private static JsonObject groupsToJson(List<PartBuilder.ConditionGroup> conditions, boolean useOr) {
        JsonObject groupJson = new JsonObject();
        JsonArray inner = new JsonArray();
        groupJson.add(useOr ? "OR" : "AND", inner);
        for (PartBuilder.ConditionGroup group : conditions) inner.add(group.toJson());
        return groupJson;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static JsonObject conditionsToJson(Multimap<Property<?>, Comparable<?>> conditions, boolean useOr) {
        JsonObject groupJson = new JsonObject();
        for (Map.Entry<Property<?>, Collection<Comparable<?>>> entry : conditions.asMap().entrySet()) {
            StringBuilder values = new StringBuilder();
            for (Comparable value : entry.getValue()) {
                if (!values.isEmpty()) values.append('|');
                values.append(((Property) entry.getKey()).getName(value));
            }
            groupJson.addProperty(entry.getKey().getName(), values.toString());
        }
        if (useOr) {
            JsonArray inner = new JsonArray();
            for (Map.Entry<String, JsonElement> entry : groupJson.entrySet()) {
                JsonObject obj = new JsonObject();
                obj.add(entry.getKey(), entry.getValue());
                inner.add(obj);
            }
            groupJson = new JsonObject();
            groupJson.add("OR", inner);
        }
        return groupJson;
    }
}

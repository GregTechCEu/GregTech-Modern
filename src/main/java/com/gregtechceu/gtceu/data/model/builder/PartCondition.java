package com.gregtechceu.gtceu.data.model.builder;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PartCondition extends Supplier<JsonElement> {

    void validate(StateDefinition<?, ?> stateDefinition);

    static TerminalPartCondition condition() {
        return new TerminalPartCondition();
    }

    static PartCondition and(PartCondition... conditions) {
        return new CompositePartCondition(PartCondition.Operation.AND, Arrays.asList(conditions));
    }

    static PartCondition or(PartCondition... conditions) {
        return new CompositePartCondition(PartCondition.Operation.OR, Arrays.asList(conditions));
    }

    public static class CompositePartCondition implements PartCondition {

        private final PartCondition.Operation operation;
        private final List<PartCondition> subConditions;

        CompositePartCondition(PartCondition.Operation operation, List<PartCondition> subConditions) {
            this.operation = operation;
            this.subConditions = subConditions;
        }

        public void validate(StateDefinition<?, ?> stateDefinition) {
            this.subConditions.forEach((condition) -> condition.validate(stateDefinition));
        }

        public JsonElement get() {
            JsonArray subConditionsList = new JsonArray();
            this.subConditions.stream().map(Supplier::get).forEach(subConditionsList::add);
            JsonObject json = new JsonObject();
            json.add(this.operation.id, subConditionsList);
            return json;
        }
    }

    public static enum Operation {

        AND("AND"),
        OR("OR");

        final String id;

        Operation(String id) {
            this.id = id;
        }
    }

    public static class TerminalPartCondition implements PartCondition {

        private final Map<Property<?>, String> terms = Maps.newHashMap();

        private static <T extends Comparable<T>> String joinValues(Property<T> property, Stream<T> valueStream) {
            return valueStream.map(property::getName).collect(Collectors.joining("|"));
        }

        private static <T extends Comparable<T>> String getTerm(Property<T> property,
                                                                T firstValue, T[] additionalValues) {
            return joinValues(property, Stream.concat(Stream.of(firstValue), Stream.of(additionalValues)));
        }

        private <T extends Comparable<T>> void putValue(Property<T> property, String value) {
            String s = this.terms.put(property, value);
            if (s != null) {
                throw new IllegalStateException("Tried to replace " + property + " value from " + s + " to " + value);
            }
        }

        public final <T extends Comparable<T>> TerminalPartCondition term(Property<T> property, T value) {
            this.putValue(property, property.getName(value));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> TerminalPartCondition term(Property<T> property,
                                                                          T firstValue, T... additionalValues) {
            this.putValue(property, getTerm(property, firstValue, additionalValues));
            return this;
        }

        public final <T extends Comparable<T>> TerminalPartCondition negatedTerm(Property<T> property, T value) {
            this.putValue(property, "!" + property.getName(value));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> TerminalPartCondition negatedTerm(Property<T> property,
                                                                                 T firstValue, T... additionalValues) {
            this.putValue(property, "!" + getTerm(property, firstValue, additionalValues));
            return this;
        }

        public JsonElement get() {
            JsonObject json = new JsonObject();
            this.terms.forEach((p, v) -> json.addProperty(p.getName(), v));
            return json;
        }

        public void validate(StateDefinition<?, ?> definition) {
            List<Property<?>> missing = this.terms.keySet().stream()
                    .filter((property) -> definition.getProperty(property.getName()) != property)
                    .toList();
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Properties " + missing + " are missing from " + definition);
            }
        }
    }
}

package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public class KeyValuePartCondition implements PartCondition {

    private static final Splitter PIPE_SPLITTER = Splitter.on('|').omitEmptyStrings();
    private final String key;
    private final String value;

    public KeyValuePartCondition(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> def) {
        Property<?> property = def.getProperty(this.key);
        if (property == null) {
            throw new RuntimeException(
                    String.format(Locale.ROOT, "Unknown property '%s' on machine '%s'", this.key, def.getOwner()));
        } else {
            String value = this.value;
            boolean invert = !value.isEmpty() && value.charAt(0) == '!';
            if (invert) {
                value = value.substring(1);
            }

            List<String> unparsedPredicates = PIPE_SPLITTER.splitToList(value);
            if (unparsedPredicates.isEmpty()) {
                throw new RuntimeException(
                        String.format(Locale.ROOT, "Empty value '%s' for property '%s' on machine '%s'",
                                this.value, this.key, def.getOwner()));
            } else {
                Predicate<MachineRenderState> predicate;
                if (unparsedPredicates.size() == 1) {
                    predicate = this.getStatePredicate(def, property, value);
                } else {
                    List<Predicate<MachineRenderState>> parsed = unparsedPredicates.stream()
                            .map((string) -> this.getStatePredicate(def, property, string))
                            .toList();
                    predicate = (state) -> parsed.stream().anyMatch((p) -> p.test(state));
                }

                return invert ? predicate.negate() : predicate;
            }
        }
    }

    private Predicate<MachineRenderState> getStatePredicate(StateDefinition<MachineDefinition, MachineRenderState> def,
                                                            Property<?> property, String value) {
        Optional<?> optional = property.getValue(value);
        if (optional.isEmpty()) {
            throw new RuntimeException(
                    String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s' in '%s'",
                            value, this.key, def.getOwner(), this.value));
        } else {
            return (state) -> state.getValue(property).equals(optional.get());
        }
    }

    @Override
    public String toString() {
        return "KeyValueCondition{" + "key='" + key + "', value='" + value + "'}";
    }
}

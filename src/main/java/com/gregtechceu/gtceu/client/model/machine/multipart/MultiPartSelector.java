package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.model.machine.variant.MultiVariantModel;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;

import com.google.common.collect.Streams;
import com.google.gson.*;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MultiPartSelector implements ModelState {

    private final PartCondition condition;
    @Getter
    private final MultiVariantModel variant;

    public MultiPartSelector(PartCondition condition, MultiVariantModel variant) {
        this.condition = condition;
        this.variant = variant;
    }

    public Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> definition) {
        return this.condition.getPredicate(definition);
    }

    public static class Deserializer implements JsonDeserializer<MultiPartSelector> {

        public MultiPartSelector deserialize(JsonElement json,
                                             Type type, JsonDeserializationContext context) throws JsonParseException {
            return fromJson(json, context);
        }

        public static MultiPartSelector fromJson(JsonElement json,
                                                 JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonobject = json.getAsJsonObject();
            return new MultiPartSelector(getSelector(jsonobject),
                    context.deserialize(jsonobject.get("apply"), MultiVariantModel.class));
        }

        private static PartCondition getSelector(JsonObject json) {
            return json.has("when") ?
                    getCondition(GsonHelper.getAsJsonObject(json, "when")) :
                    PartCondition.TRUE;
        }

        private static PartCondition getCondition(JsonObject json) {
            Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
            if (entries.isEmpty()) {
                throw new JsonParseException("No elements found in selector");
            } else if (entries.size() == 1) {
                if (json.has(OrPartCondition.TOKEN)) {
                    List<PartCondition> conditions = Streams
                            .stream(GsonHelper.getAsJsonArray(json, OrPartCondition.TOKEN))
                            .map((e) -> getCondition(e.getAsJsonObject()))
                            .toList();
                    return new OrPartCondition(conditions);
                } else if (json.has(AndPartCondition.TOKEN)) {
                    List<PartCondition> conditions = Streams
                            .stream(GsonHelper.getAsJsonArray(json, AndPartCondition.TOKEN))
                            .map((e) -> getCondition(e.getAsJsonObject()))
                            .toList();
                    return new AndPartCondition(conditions);
                } else {
                    return getKeyValueCondition(entries.iterator().next());
                }
            } else {
                return new AndPartCondition(entries.stream()
                        .map(MultiPartSelector.Deserializer::getKeyValueCondition)
                        .collect(Collectors.toList()));
            }
        }

        private static PartCondition getKeyValueCondition(Map.Entry<String, JsonElement> entry) {
            return new KeyValuePartCondition(entry.getKey(), entry.getValue().getAsString());
        }
    }
}

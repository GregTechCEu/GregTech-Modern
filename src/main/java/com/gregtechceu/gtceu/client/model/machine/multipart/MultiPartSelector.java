package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.google.common.collect.Streams;
import com.google.gson.*;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.mojang.datafixers.util.Either;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MultiPartSelector {
   private final PartCondition condition;
   @Getter
   private final Either<ResourceLocation, UnbakedModel> model;
   @Getter
   @Setter
   private UnbakedModel resolvedModel;

   public MultiPartSelector(PartCondition condition, Either<ResourceLocation, UnbakedModel> model) {
      this.condition = condition;
      this.model = model;
   }

   public Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> definition) {
      return this.condition.getPredicate(definition);
   }

   public static class Deserializer implements JsonDeserializer<MultiPartSelector> {

      public MultiPartSelector deserialize(JsonElement json,
                                           Type type, JsonDeserializationContext context) throws JsonParseException {
         JsonObject jsonobject = json.getAsJsonObject();
         return new MultiPartSelector(this.getSelector(jsonobject),
                 MachineModelLoader.parseVariant(jsonobject.get("apply")));
      }

      private PartCondition getSelector(JsonObject json) {
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
               List<PartCondition> conditions = Streams.stream(GsonHelper.getAsJsonArray(json, OrPartCondition.TOKEN))
                       .map((e) -> getCondition(e.getAsJsonObject()))
                       .toList();
               return new OrPartCondition(conditions);
            } else if (json.has(AndPartCondition.TOKEN)) {
               List<PartCondition> conditions = Streams.stream(GsonHelper.getAsJsonArray(json, AndPartCondition.TOKEN))
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

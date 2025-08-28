package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientAction;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GTRecipeSerializer implements RecipeSerializer<GTRecipe> {

    public static final Codec<GTRecipe> CODEC = makeCodec(GTCEu.Mods.isKubeJSLoaded());

    public static final GTRecipeSerializer SERIALIZER = new GTRecipeSerializer();

    public Map<RecipeCapability<?>, List<Content>> capabilitiesFromJson(JsonObject json) {
        Map<RecipeCapability<?>, List<Content>> capabilities = new IdentityHashMap<>();
        for (String key : json.keySet()) {
            JsonArray contentsJson = json.getAsJsonArray(key);
            RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(key);
            if (capability != null) {
                List<Content> contents = new ArrayList<>();
                for (JsonElement contentJson : contentsJson) {
                    contents.add(capability.serializer.fromJsonContent(contentJson));
                }
                capabilities.put(capability, contents);
            }
        }
        return capabilities;
    }

    public Map<RecipeCapability<?>, ChanceLogic> chanceLogicsFromJson(JsonObject json) {
        Map<RecipeCapability<?>, ChanceLogic> chanceLogics = new IdentityHashMap<>();
        for (String key : json.keySet()) {
            String value = json.get(key).getAsString();
            chanceLogics.put(GTRegistries.RECIPE_CAPABILITIES.get(key), GTRegistries.CHANCE_LOGICS.get(value));
        }
        return chanceLogics;
    }

    @Override
    public @NotNull GTRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        GTRecipe recipe = CODEC.parse(ops, json).getOrThrow(false, GTCEu.LOGGER::error);
        recipe.setId(id);
        return recipe;
    }

    public static Tuple<RecipeCapability<?>, List<Content>> entryReader(FriendlyByteBuf buf) {
        RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(buf.readUtf());
        List<Content> contents = buf.readList(capability.serializer::fromNetworkContent);
        return new Tuple<>(capability, contents);
    }

    public static void entryWriter(FriendlyByteBuf buf, Map.Entry<RecipeCapability<?>, ? extends List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        List<Content> contents = entry.getValue();
        buf.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        buf.writeCollection(contents, capability.serializer::toNetworkContent);
    }

    public static RecipeCondition conditionReader(FriendlyByteBuf buf) {
        // Consume the condition key that's set in conditionWriter
        buf.readUtf();
        return RecipeCondition.fromNetwork(buf);
    }

    public static void conditionWriter(FriendlyByteBuf buf, RecipeCondition condition) {
        buf.writeUtf(GTRegistries.RECIPE_CONDITIONS.getKey(condition.getType()));
        condition.toNetwork(buf);
    }

    public static Map<RecipeCapability<?>, List<Content>> tuplesToMap(List<Tuple<RecipeCapability<?>, List<Content>>> entries) {
        Map<RecipeCapability<?>, List<Content>> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    @Override
    @NotNull
    public GTRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
        ResourceLocation recipeType = buf.readResourceLocation();
        int duration = buf.readVarInt();
        Map<RecipeCapability<?>, List<Content>> inputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickInputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> outputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickOutputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));

        Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readUtf()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readUtf()));
        Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readUtf()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readUtf()));
        Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readUtf()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readUtf()));
        Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readUtf()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readUtf()));

        List<RecipeCondition> conditions = buf.readCollection(c -> new ArrayList<>(),
                GTRecipeSerializer::conditionReader);
        List<?> ingredientActions = new ArrayList<>();
        if (GTCEu.Mods.isKubeJSLoaded()) {
            ingredientActions = KJSCallWrapper.getIngredientActions(buf);
        }
        CompoundTag data = buf.readNbt();
        if (data == null) {
            data = new CompoundTag();
        }
        ResourceLocation categoryLoc = buf.readResourceLocation();

        GTRecipeType type = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(recipeType);
        GTRecipeCategory category = GTRegistries.RECIPE_CATEGORIES.get(categoryLoc);

        GTRecipe recipe = new GTRecipe(type, id,
                inputs, outputs, tickInputs, tickOutputs,
                inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                conditions, ingredientActions, data, duration, category);

        recipe.recipeCategory.addRecipe(recipe);

        // a little special piece of code for loading all the research entries into the recipe type's list on the
        // client.
        ResearchCondition researchCondition = conditions.stream().filter(ResearchCondition.class::isInstance).findAny()
                .map(ResearchCondition.class::cast).orElse(null);
        if (researchCondition != null) {
            for (ResearchData.ResearchEntry entry : researchCondition.data) {
                type.addDataStickEntry(entry.getResearchId(), recipe);
            }
        }
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, GTRecipe recipe) {
        buf.writeResourceLocation(recipe.recipeType.registryName);
        buf.writeVarInt(recipe.duration);
        buf.writeCollection(recipe.inputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.tickInputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.outputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.tickOutputs.entrySet(), GTRecipeSerializer::entryWriter);

        buf.writeMap(recipe.inputChanceLogics,
                (buf1, cap) -> buf1.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeUtf(GTRegistries.CHANCE_LOGICS.getKey(logic)));
        buf.writeMap(recipe.outputChanceLogics,
                (buf1, cap) -> buf1.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeUtf(GTRegistries.CHANCE_LOGICS.getKey(logic)));
        buf.writeMap(recipe.tickInputChanceLogics,
                (buf1, cap) -> buf1.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeUtf(GTRegistries.CHANCE_LOGICS.getKey(logic)));
        buf.writeMap(recipe.tickOutputChanceLogics,
                (buf1, cap) -> buf1.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeUtf(GTRegistries.CHANCE_LOGICS.getKey(logic)));

        buf.writeCollection(recipe.conditions, GTRecipeSerializer::conditionWriter);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.writeIngredientActions(recipe.ingredientActions, buf);
        }
        buf.writeNbt(recipe.data);
        buf.writeResourceLocation(recipe.recipeCategory.registryKey);
    }

    private static Codec<GTRecipe> makeCodec(boolean isKubeLoaded) {
        // spotless:off
        if (!isKubeLoaded) {
            return RecordCodecBuilder.create(instance -> instance.group(
                            GTRegistries.RECIPE_TYPES.codec().fieldOf("type").forGetter(val -> val.recipeType),
                            RecipeCapability.CODEC.optionalFieldOf("inputs", Map.of()).forGetter(val -> val.inputs),
                            RecipeCapability.CODEC.optionalFieldOf("outputs", Map.of()).forGetter(val -> val.outputs),
                            RecipeCapability.CODEC.optionalFieldOf("tickInputs", Map.of()).forGetter(val -> val.tickInputs),
                            RecipeCapability.CODEC.optionalFieldOf("tickOutputs", Map.of()).forGetter(val -> val.tickOutputs),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("inputChanceLogics", Map.of()).forGetter(val -> val.inputChanceLogics),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("outputChanceLogics", Map.of()).forGetter(val -> val.outputChanceLogics),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("tickInputChanceLogics", Map.of()).forGetter(val -> val.tickInputChanceLogics),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("tickOutputChanceLogics", Map.of()).forGetter(val -> val.tickOutputChanceLogics),
                            RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(val -> val.conditions),
                            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
                            GTRegistries.RECIPE_CATEGORIES.codec().optionalFieldOf("category", GTRecipeCategory.DEFAULT).forGetter(val -> val.recipeCategory))
                    .apply(instance, (type,
                                      inputs, outputs, tickInputs, tickOutputs,
                                      inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                                      conditions, data, duration, recipeCategory) ->
                            new GTRecipe(type, inputs, outputs, tickInputs, tickOutputs,
                                    inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                                    conditions, List.of(), data, duration, recipeCategory)));
        } else {
            return RecordCodecBuilder.create(instance -> instance.group(
                            GTRegistries.RECIPE_TYPES.codec().fieldOf("type").forGetter(val -> val.recipeType),
                            RecipeCapability.CODEC.optionalFieldOf("inputs", Map.of()).forGetter(val -> val.inputs),
                            RecipeCapability.CODEC.optionalFieldOf("outputs", Map.of()).forGetter(val -> val.outputs),
                            RecipeCapability.CODEC.optionalFieldOf("tickInputs", Map.of()).forGetter(val -> val.tickInputs),
                            RecipeCapability.CODEC.optionalFieldOf("tickOutputs", Map.of()).forGetter(val -> val.tickOutputs),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("inputChanceLogics", Map.of()).forGetter(val -> val.inputChanceLogics),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("outputChanceLogics", Map.of()).forGetter(val -> val.outputChanceLogics),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("tickInputChanceLogics", Map.of()).forGetter(val -> val.tickInputChanceLogics),
                            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                                    .optionalFieldOf("tickOutputChanceLogics", Map.of()).forGetter(val -> val.tickOutputChanceLogics),
                            RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(val -> val.conditions),
                            KJSCallWrapper.INGREDIENT_ACTION_CODEC.optionalFieldOf("kubejs:actions", List.of()).forGetter(val -> (List<IngredientAction>) val.ingredientActions),
                            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
                            GTRegistries.RECIPE_CATEGORIES.codec().optionalFieldOf("category", GTRecipeCategory.DEFAULT).forGetter(val -> val.recipeCategory))
                    .apply(instance, GTRecipe::new));
        }
        // spotless:on
    }

    public static class KJSCallWrapper {

        public static final Codec<List<IngredientAction>> INGREDIENT_ACTION_CODEC = ExtraCodecs.JSON.xmap(
                IngredientAction::parseList,
                list -> {
                    JsonArray value = new JsonArray();
                    for (IngredientAction action : list) {
                        value.add(action.toJson());
                    }
                    return value;
                });

        public static List<?> getIngredientActions(JsonObject json) {
            return IngredientAction.parseList(json.get("kubejs:actions"));
        }

        public static List<?> getIngredientActions(FriendlyByteBuf buf) {
            return IngredientAction.readList(buf);
        }

        public static void writeIngredientActions(List<?> ingredientActions, FriendlyByteBuf buf) {
            // noinspection unchecked must be List<?> to be able to load without KJS.
            IngredientAction.writeList(buf, (List<IngredientAction>) ingredientActions);
        }
    }
}

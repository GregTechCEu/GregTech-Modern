package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.ResearchCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipeSerializer
 */
public class GTRecipeSerializer implements RecipeSerializer<GTRecipe> {

    public static final GTRecipeSerializer SERIALIZER = new GTRecipeSerializer();

    public Map<RecipeCapability<?>, List<Content>> capabilitiesFromJson(JsonObject json) {
        Map<RecipeCapability<?>, List<Content>> capabilities = new HashMap<>();
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

    @Override
    public @NotNull GTRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        String recipeType = GsonHelper.getAsString(json, "type");
        int duration = json.has("duration") ? GsonHelper.getAsInt(json, "duration") : 100;
        CompoundTag data = new CompoundTag();
        if (json.has("data"))
            data = CraftingHelper.getNBT(json.get("data"));
        Map<RecipeCapability<?>, List<Content>> inputs = capabilitiesFromJson(
                json.has("inputs") ? json.getAsJsonObject("inputs") : new JsonObject());
        Map<RecipeCapability<?>, List<Content>> tickInputs = capabilitiesFromJson(
                json.has("tickInputs") ? json.getAsJsonObject("tickInputs") : new JsonObject());
        Map<RecipeCapability<?>, List<Content>> outputs = capabilitiesFromJson(
                json.has("outputs") ? json.getAsJsonObject("outputs") : new JsonObject());
        Map<RecipeCapability<?>, List<Content>> tickOutputs = capabilitiesFromJson(
                json.has("tickOutputs") ? json.getAsJsonObject("tickOutputs") : new JsonObject());
        List<RecipeCondition> conditions = new ArrayList<>();
        JsonArray conditionsJson = json.has("recipeConditions") ? json.getAsJsonArray("recipeConditions") :
                new JsonArray();
        for (JsonElement jsonElement : conditionsJson) {
            if (jsonElement instanceof JsonObject jsonObject) {
                var conditionKey = GsonHelper.getAsString(jsonObject, "type", "");
                var clazz = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (clazz != null) {
                    RecipeCondition condition = RecipeCondition.create(clazz);
                    if (condition != null) {
                        conditions.add(condition
                                .deserialize(GsonHelper.getAsJsonObject(jsonObject, "data", new JsonObject())));
                    }
                }
            }
        }
        List<?> ingredientActions = new ArrayList<>();
        if (GTCEu.isKubeJSLoaded()) {
            ingredientActions = KJSCallWrapper.getIngredientActions(json);
        }
        boolean isFuel = GsonHelper.getAsBoolean(json, "isFuel", false);
        return new GTRecipe((GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(new ResourceLocation(recipeType)), id,
                inputs, outputs, tickInputs, tickOutputs, conditions, ingredientActions, data, duration, isFuel);
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
        RecipeCondition condition = RecipeCondition.create(GTRegistries.RECIPE_CONDITIONS.get(buf.readUtf()));
        return condition.fromNetwork(buf);
    }

    public static void conditionWriter(FriendlyByteBuf buf, RecipeCondition condition) {
        buf.writeUtf(GTRegistries.RECIPE_CONDITIONS.getKey(condition.getClass()));
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
        List<RecipeCondition> conditions = buf.readCollection(c -> new ArrayList<>(),
                GTRecipeSerializer::conditionReader);
        List<?> ingredientActions = new ArrayList<>();
        if (GTCEu.isKubeJSLoaded()) {
            ingredientActions = KJSCallWrapper.getIngredientActions(buf);
        }
        CompoundTag data = buf.readNbt();
        if (data == null) {
            data = new CompoundTag();
        }
        boolean isFuel = buf.readBoolean();
        GTRecipeType type = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(recipeType);
        GTRecipe recipe = new GTRecipe(type, id, inputs, outputs, tickInputs, tickOutputs, conditions,
                ingredientActions, data, duration, isFuel);

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
        buf.writeCollection(recipe.conditions, GTRecipeSerializer::conditionWriter);
        if (GTCEu.isKubeJSLoaded()) {
            KJSCallWrapper.writeIngredientActions(recipe.ingredientActions, buf);
        }
        buf.writeNbt(recipe.data);
        buf.writeBoolean(recipe.isFuel);
    }

    public static class KJSCallWrapper {

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

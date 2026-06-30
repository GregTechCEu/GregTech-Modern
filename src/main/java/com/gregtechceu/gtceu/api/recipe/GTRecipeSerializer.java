package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GTRecipeSerializer implements RecipeSerializer<GTRecipeDefinition> {

    public static final GTRecipeSerializer SERIALIZER = new GTRecipeSerializer();
    private static final String TYPE = "type";
    private static final String DURATION = "duration";
    private static final String INPUTS = "inputs";
    private static final String OUTPUTS = "outputs";
    private static final String TICK_INPUTS = "tickInputs";
    private static final String TICK_OUTPUTS = "tickOutputs";
    private static final String RECIPE_CONDITIONS = "recipeConditions";
    private static final String DATA = "data";
    private static final String CATEGORY = "category";
    private static final String TIER = "tier";

    @Override
    public @NotNull GTRecipeDefinition fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        ResourceLocation typeLoc = new ResourceLocation(GsonHelper.getAsString(json, TYPE));
        GTRecipeType recipeType = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(typeLoc);

        int tier = GsonHelper.getAsInt(json, TIER, 0);

        int duration = GsonHelper.getAsInt(json, DURATION, 0);

        ContentListMap inputs = readContentMap(json, INPUTS);
        ContentListMap outputs = readContentMap(json, OUTPUTS);
        ContentListMap tickInputs = readContentMap(json, TICK_INPUTS);
        ContentListMap tickOutputs = readContentMap(json, TICK_OUTPUTS);

        List<RecipeCondition<?>> conditions = readConditions(json);
        CompoundTag data = readData(json);
        GTRecipeCategory category = readCategory(json, recipeType);


        return new GTRecipeDefinition(id, recipeType, category, inputs, outputs, tickInputs, tickOutputs,
                duration, conditions, data, tier);
    }

    public JsonObject toJson(GTRecipeDefinition recipe) {
        JsonObject json = new JsonObject();
        json.addProperty(TYPE, recipe.recipeType.registryName.toString());
        json.addProperty(TIER, recipe.tier);
        json.addProperty(DURATION, recipe.duration);
        writeContentMap(json, INPUTS, recipe.inputs);
        writeContentMap(json, OUTPUTS, recipe.outputs);
        writeContentMap(json, TICK_INPUTS, recipe.tickInputs);
        writeContentMap(json, TICK_OUTPUTS, recipe.tickOutputs);
        writeConditions(json, recipe.conditions);
        writeData(json, recipe.data);
        if (recipe.category != null && recipe.category != recipe.recipeType.getCategory()) {
            json.addProperty(CATEGORY, recipe.category.registryKey.toString());
        }
        return json;
    }

    @Override
    @NotNull
    public GTRecipeDefinition fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
        GTRecipeType recipeType = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(buf.readResourceLocation());
        int tier = buf.readVarInt();
        int duration = buf.readVarInt();
        ContentListMap inputs = ContentListMap.fromNetwork(buf);
        ContentListMap outputs = ContentListMap.fromNetwork(buf);
        ContentListMap tickInputs = ContentListMap.fromNetwork(buf);
        ContentListMap tickOutputs = ContentListMap.fromNetwork(buf);
        List<RecipeCondition<?>> conditions = buf.readList(RecipeCondition::fromNetwork);
        CompoundTag data = buf.readNbt();
        if (data == null) {
            data = new CompoundTag();
        }
        GTRecipeCategory category = GTRegistries.RECIPE_CATEGORIES.get(buf.readResourceLocation());


        return new GTRecipeDefinition(id, recipeType, category, inputs, outputs, tickInputs, tickOutputs,
                duration, conditions, data, tier);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, GTRecipeDefinition recipe) {
        buf.writeResourceLocation(recipe.recipeType.registryName);
        buf.writeVarInt(recipe.tier);
        buf.writeVarInt(recipe.duration);
        recipe.inputs.toNetwork(buf);
        recipe.outputs.toNetwork(buf);
        recipe.tickInputs.toNetwork(buf);
        recipe.tickOutputs.toNetwork(buf);
        buf.writeCollection(recipe.conditions, (buffer, condition) -> condition.toNetwork(buffer));
        buf.writeNbt(recipe.data);
        buf.writeResourceLocation(recipe.category.registryKey);

    }

    private static ContentListMap readContentMap(JsonObject json, String key) {
        if (!GsonHelper.isObjectNode(json, key)) {
            return new ContentListMap();
        }
        return ContentListMap.CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(json, key))
                .getOrThrow(false, GTCEu.LOGGER::error);
    }

    private static void writeContentMap(JsonObject json, String key, ContentListMap contents) {
        if (!contents.isEmpty()) {
            json.add(key, ContentListMap.CODEC.encodeStart(JsonOps.INSTANCE, contents)
                    .getOrThrow(false, GTCEu.LOGGER::error));
        }
    }

    private static List<RecipeCondition<?>> readConditions(JsonObject json) {
        if (!GsonHelper.isArrayNode(json, RECIPE_CONDITIONS)) {
            return new ArrayList<>();
        }
        JsonArray array = GsonHelper.getAsJsonArray(json, RECIPE_CONDITIONS);
        List<RecipeCondition<?>> conditions = new ArrayList<>(array.size());
        for (var element : array) {
            conditions.add(RecipeCondition.deserialize(element.getAsJsonObject()));
        }
        return conditions;
    }

    private static void writeConditions(JsonObject json, List<RecipeCondition<?>> conditions) {
        if (conditions.isEmpty()) return;
        JsonArray array = new JsonArray();
        for (RecipeCondition<?> condition : conditions) {
            array.add(condition.serialize());
        }
        json.add(RECIPE_CONDITIONS, array);
    }

    private static CompoundTag readData(JsonObject json) {
        if (!json.has(DATA)) {
            return new CompoundTag();
        }
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return (CompoundTag) ops.convertTo(NbtOps.INSTANCE, json.get(DATA));
    }

    private static void writeData(JsonObject json, CompoundTag data) {
        if (data == null || data.isEmpty()) return;
        var ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        json.add(DATA, ops.convertTo(JsonOps.INSTANCE, data));
    }

    private static GTRecipeCategory readCategory(JsonObject json, GTRecipeType recipeType) {
        if (!json.has(CATEGORY)) {
            return recipeType.getCategory();
        }
        ResourceLocation categoryLoc = new ResourceLocation(GsonHelper.getAsString(json, CATEGORY));
        GTRecipeCategory category = GTRegistries.RECIPE_CATEGORIES.get(categoryLoc);
        return category == null ? recipeType.getCategory() : category;
    }
}

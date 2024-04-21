package com.gregtechceu.gtceu.api.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.gregtechceu.gtceu.common.recipe.ResearchCondition;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientAction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
    public static final Codec<GTRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        GTRegistries.RECIPE_TYPES.codec().fieldOf("type").forGetter(val -> val.recipeType),
        RecipeCapability.CODEC.optionalFieldOf("inputs", Map.of()).forGetter(val -> val.inputs),
        RecipeCapability.CODEC.optionalFieldOf("outputs", Map.of()).forGetter(val -> val.outputs),
        RecipeCapability.CODEC.optionalFieldOf("tickInputs", Map.of()).forGetter(val -> val.tickInputs),
        RecipeCapability.CODEC.optionalFieldOf("tickOutputs", Map.of()).forGetter(val -> val.tickOutputs),
        RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(val -> val.conditions),
        CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
        Codec.BOOL.optionalFieldOf("isFuel", false).forGetter(val -> val.isFuel)
    ).apply(instance, (type, inputs, outputs, tickInputs, tickOutputs, conditions, data, duration, isFuel) ->
        new GTRecipe(type, inputs, outputs, tickInputs, tickOutputs, conditions, List.of(), data, duration, isFuel)));

    public static final GTRecipeSerializer SERIALIZER = new GTRecipeSerializer();

    @Override
    public Codec<GTRecipe> codec() {
        return CODEC;
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
        RecipeCondition condition = GTRegistries.RECIPE_CONDITIONS.get(buf.readUtf()).factory.createDefault();
        return condition.fromNetwork(buf);
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
    public GTRecipe fromNetwork(@NotNull FriendlyByteBuf buf) {
        ResourceLocation recipeType = buf.readResourceLocation();
        int duration = buf.readVarInt();
        Map<RecipeCapability<?>, List<Content>> inputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickInputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> outputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickOutputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        List<RecipeCondition> conditions = buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::conditionReader);
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
        GTRecipe recipe = new GTRecipe(type, inputs, outputs, tickInputs, tickOutputs, conditions, ingredientActions, data, duration, isFuel);

        // a little special piece of code for loading all the research entries into the recipe type's list on the client.
        ResearchCondition researchCondition = conditions.stream().filter(ResearchCondition.class::isInstance).findAny().map(ResearchCondition.class::cast).orElse(null);
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
            //noinspection unchecked must be List<?> to be able to load without KJS.
            IngredientAction.writeList(buf, (List<IngredientAction>) ingredientActions);
        }
    }
}

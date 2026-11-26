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
import com.mojang.serialization.MapCodec;
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
            RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(GTCEu.id(key));
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
            chanceLogics.put(GTRegistries.RECIPE_CAPABILITIES.get(GTCEu.id(value)),
                    GTRegistries.CHANCE_LOGICS.get(GTCEu.id(value)));
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
        RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(buf.readResourceLocation());
        List<Content> contents = buf.readList(capability.serializer::fromNetworkContent);
        return new Tuple<>(capability, contents);
    }

    public static void entryWriter(FriendlyByteBuf buf, Map.Entry<RecipeCapability<?>, ? extends List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        List<Content> contents = entry.getValue();
        buf.writeResourceLocation(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        buf.writeCollection(contents, capability.serializer::toNetworkContent);
    }

    public static RecipeCondition<?> conditionReader(FriendlyByteBuf buf) {
        return RecipeCondition.fromNetwork(buf);
    }

    public static void conditionWriter(FriendlyByteBuf buf, RecipeCondition<?> condition) {
        condition.toNetwork(buf);
    }

    public static Map<RecipeCapability<?>, List<Content>> tuplesToMap(List<Tuple<RecipeCapability<?>, List<Content>>> entries) {
        Map<RecipeCapability<?>, List<Content>> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    public static GTRecipe fromNetworkWithoutDatapackSync(@NotNull FriendlyByteBuf buf) {
        ResourceLocation recipeType = buf.readResourceLocation();
        ResourceLocation id = buf.readResourceLocation();
        Map<RecipeCapability<?>, List<Content>> inputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickInputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> outputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickOutputs = tuplesToMap(
                buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));

        Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readResourceLocation()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readResourceLocation()));
        Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readResourceLocation()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readResourceLocation()));
        Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readResourceLocation()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readResourceLocation()));
        Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics = buf.readMap(
                buf1 -> GTRegistries.RECIPE_CAPABILITIES.get(buf1.readResourceLocation()),
                buf1 -> GTRegistries.CHANCE_LOGICS.get(buf1.readResourceLocation()));

        List<RecipeCondition<?>> conditions = buf.readCollection(c -> new ArrayList<>(),
                GTRecipeSerializer::conditionReader);
        List<?> ingredientActions = new ArrayList<>();
        if (GTCEu.Mods.isKubeJSLoaded()) {
            ingredientActions = KJSCallWrapper.getIngredientActions(buf);
        }
        CompoundTag data = buf.readNbt();
        if (data == null) {
            data = new CompoundTag();
        }
        int duration = buf.readVarInt();
        int parallels = buf.readVarInt();
        int subtickParallels = buf.readVarInt();
        int batchParallels = buf.readVarInt();

        int groupColor = buf.readInt();
        ResourceLocation categoryLoc = buf.readResourceLocation();

        GTRecipeType type = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(recipeType);
        GTRecipeCategory category = GTRegistries.RECIPE_CATEGORIES.get(categoryLoc);

        boolean keepSpoilingProgress = buf.readBoolean();

        return new GTRecipe(type, id,
                inputs, outputs, tickInputs, tickOutputs,
                inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                conditions, ingredientActions, data, duration, parallels, subtickParallels, batchParallels, category,
                groupColor, keepSpoilingProgress);
    }

    /**
     * Do not call when reading a recipe from the network manually, use
     * {@link #fromNetworkWithoutDatapackSync(FriendlyByteBuf)} instead
     */
    @Override
    @NotNull
    public GTRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
        GTRecipe recipe = fromNetworkWithoutDatapackSync(buf);

        recipe.recipeCategory.addRecipe(recipe);

        // a little special piece of code for loading all the research entries into the recipe type's list on the
        // client.
        ResearchCondition researchCondition = recipe.conditions.stream().filter(ResearchCondition.class::isInstance)
                .findAny()
                .map(ResearchCondition.class::cast).orElse(null);
        if (researchCondition != null) {
            for (ResearchData.ResearchEntry entry : researchCondition.data) {
                recipe.recipeType.addDataStickEntry(entry.getResearchId(), recipe);
            }
        }
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, GTRecipe recipe) {
        buf.writeResourceLocation(recipe.recipeType.registryName);
        buf.writeResourceLocation(recipe.id);
        buf.writeCollection(recipe.inputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.tickInputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.outputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.tickOutputs.entrySet(), GTRecipeSerializer::entryWriter);

        buf.writeMap(recipe.inputChanceLogics,
                (buf1, cap) -> buf1.writeResourceLocation(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeResourceLocation(GTRegistries.CHANCE_LOGICS.getKey(logic)));
        buf.writeMap(recipe.outputChanceLogics,
                (buf1, cap) -> buf1.writeResourceLocation(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeResourceLocation(GTRegistries.CHANCE_LOGICS.getKey(logic)));
        buf.writeMap(recipe.tickInputChanceLogics,
                (buf1, cap) -> buf1.writeResourceLocation(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeResourceLocation(GTRegistries.CHANCE_LOGICS.getKey(logic)));
        buf.writeMap(recipe.tickOutputChanceLogics,
                (buf1, cap) -> buf1.writeResourceLocation(GTRegistries.RECIPE_CAPABILITIES.getKey(cap)),
                (buf1, logic) -> buf1.writeResourceLocation(GTRegistries.CHANCE_LOGICS.getKey(logic)));

        buf.writeCollection(recipe.conditions, GTRecipeSerializer::conditionWriter);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.writeIngredientActions(recipe.ingredientActions, buf);
        }
        buf.writeNbt(recipe.data);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.parallels);
        buf.writeVarInt(recipe.subtickParallels);
        buf.writeVarInt(recipe.batchParallels);
        buf.writeInt(recipe.groupColor);
        buf.writeResourceLocation(recipe.recipeCategory.registryKey);
        buf.writeBoolean(recipe.keepSpoilingProgress);
    }

    /**
     * Codecs can only have up to 16 inputs. This is at 15 now, so the three recipe Parallel/Batch values are
     * condensed to a List.
     */
    private static Codec<GTRecipe> makeCodec(boolean isKubeLoaded) {
        // spotless:off
        if (!isKubeLoaded) {
            return RecordCodecBuilder.create(instance -> instance.group(
                            GTRegistries.RECIPE_TYPES.codec().fieldOf("type").forGetter(val -> val.recipeType),
                            RecipeIO.CODEC.forGetter(GTRecipe::getRecipeIO),
                            RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(val -> val.conditions),
                            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
                            RecipeParallels.CODEC.optionalFieldOf("all_parallels", new RecipeParallels(1, 1, 1)).forGetter(val -> new RecipeParallels(val.parallels, val.subtickParallels, val.batchParallels)),
                            GTRegistries.RECIPE_CATEGORIES.codec().optionalFieldOf("category", GTRecipeCategory.DEFAULT).forGetter(val -> val.recipeCategory),
                            Codec.INT.optionalFieldOf("groupColor", -1).forGetter(val -> val.groupColor),
                            Codec.BOOL.optionalFieldOf("keepSpoilingProgress", true).forGetter(val -> val.keepSpoilingProgress))
                    .apply(instance, (type,
                                      recipeIO,
                                      conditions, data, duration, allParallels, recipeCategory, groupColor, keepSpoilingProgress) ->
                            new GTRecipe(type, recipeIO,
                                    conditions, List.of(), data, duration, allParallels, recipeCategory, groupColor, keepSpoilingProgress)));
        } else {
            return RecordCodecBuilder.create(instance -> instance.group(
                            GTRegistries.RECIPE_TYPES.codec().fieldOf("type").forGetter(val -> val.recipeType),
                            RecipeIO.CODEC.forGetter(GTRecipe::getRecipeIO),
                            RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(val -> val.conditions),
                            KJSCallWrapper.INGREDIENT_ACTION_CODEC.optionalFieldOf("kubejs:actions", List.of()).forGetter(val -> (List<IngredientAction>) val.ingredientActions),
                            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
                            RecipeParallels.CODEC.optionalFieldOf("all_parallels", new RecipeParallels(1, 1, 1)).forGetter(val -> new RecipeParallels(val.parallels, val.subtickParallels, val.batchParallels)),
                            GTRegistries.RECIPE_CATEGORIES.codec().optionalFieldOf("category", GTRecipeCategory.DEFAULT).forGetter(val -> val.recipeCategory),
                            Codec.INT.optionalFieldOf("groupColor", -1).forGetter(val -> val.groupColor),
                            Codec.BOOL.optionalFieldOf("keepSpoilingProgress", true).forGetter(val -> val.keepSpoilingProgress))
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

    public record RecipeIO(
                           Map<RecipeCapability<?>, List<Content>> inputs,
                           Map<RecipeCapability<?>, List<Content>> outputs,
                           Map<RecipeCapability<?>, List<Content>> tickInputs,
                           Map<RecipeCapability<?>, List<Content>> tickOutputs,
                           Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                           Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                           Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                           Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics) {

        public static final MapCodec<RecipeIO> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
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
                        .optionalFieldOf("tickOutputChanceLogics", Map.of())
                        .forGetter(val -> val.tickOutputChanceLogics))
                .apply(instance, RecipeIO::new));
    }

    public record RecipeParallels(
                                  int parallels,
                                  int subtickParallels,
                                  int batchParallels) {

        public static final Codec<RecipeParallels> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("parallels").forGetter(RecipeParallels::parallels),
                Codec.INT.fieldOf("subtickParallels").forGetter(RecipeParallels::subtickParallels),
                Codec.INT.fieldOf("batchParallels").forGetter(RecipeParallels::batchParallels))
                .apply(instance, RecipeParallels::new));
    }
}

package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.ResearchCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipeSerializer
 */
public class GTRecipeSerializer implements RecipeSerializer<GTRecipe> {

    public static final MapCodec<GTRecipe> CODEC = makeCodec(GTCEu.isKubeJSLoaded());
    public static final StreamCodec<RegistryFriendlyByteBuf, GTRecipe> STREAM_CODEC = StreamCodec
            .of(GTRecipeSerializer::toNetwork, GTRecipeSerializer::fromNetwork);

    @Override
    public MapCodec<GTRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GTRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    public static Tuple<RecipeCapability<?>, List<Content>> entryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(buf.readUtf());
        List<Content> contents = readCollection(buf, capability.serializer::fromNetworkContent);
        return new Tuple<>(capability, contents);
    }

    public static Tuple<RecipeCapability<?>, ChanceLogic> changeLogicEntryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(buf.readUtf());
        ChanceLogic logic = GTRegistries.CHANCE_LOGICS.get(buf.readUtf());
        return new Tuple<>(capability, logic);
    }

    public static void entryWriter(RegistryFriendlyByteBuf buf,
                                   Map.Entry<RecipeCapability<?>, ? extends List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        List<Content> contents = entry.getValue();
        buf.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        writeCollection(contents, buf, capability.serializer::toNetworkContent);
    }

    public static void changeLogicEntryWriter(RegistryFriendlyByteBuf buf,
                                              Map.Entry<RecipeCapability<?>, ChanceLogic> entry) {
        RecipeCapability<?> capability = entry.getKey();
        ChanceLogic logic = entry.getValue();
        buf.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        buf.writeUtf(GTRegistries.CHANCE_LOGICS.getKey(logic));
    }

    public static RecipeCondition conditionReader(RegistryFriendlyByteBuf buf) {
        RecipeCondition condition = GTRegistries.RECIPE_CONDITIONS.get(buf.readUtf()).factory.createDefault();
        return condition.fromNetwork(buf);
    }

    public static void conditionWriter(RegistryFriendlyByteBuf buf, RecipeCondition condition) {
        buf.writeUtf(GTRegistries.RECIPE_CONDITIONS.getKey(condition.getType()));
        condition.toNetwork(buf);
    }

    public static Map<RecipeCapability<?>, List<Content>> tuplesToMap(List<Tuple<RecipeCapability<?>, List<Content>>> entries) {
        Map<RecipeCapability<?>, List<Content>> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    public static Map<RecipeCapability<?>, ChanceLogic> logicTuplesToMap(List<Tuple<RecipeCapability<?>, ChanceLogic>> entries) {
        Map<RecipeCapability<?>, ChanceLogic> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    @NotNull
    public static GTRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
        ResourceLocation recipeType = buf.readResourceLocation();
        ResourceLocation id = buf.readResourceLocation();
        int duration = buf.readVarInt();
        Map<RecipeCapability<?>, List<Content>> inputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickInputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> outputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickOutputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));

        List<RecipeCondition> conditions = readCollection(buf, GTRecipeSerializer::conditionReader);

        Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));

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
        GTRecipe recipe = new GTRecipe(type, id,
                inputs, outputs, tickInputs, tickOutputs,
                inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                conditions, ingredientActions, data, duration, isFuel);

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

    public static void toNetwork(RegistryFriendlyByteBuf buf, GTRecipe recipe) {
        buf.writeResourceLocation(recipe.recipeType.registryName);
        buf.writeResourceLocation(recipe.id);
        buf.writeVarInt(recipe.duration);
        GTRecipeSerializer.writeCollection(recipe.inputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        GTRecipeSerializer.writeCollection(recipe.tickInputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        GTRecipeSerializer.writeCollection(recipe.outputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        GTRecipeSerializer.writeCollection(recipe.tickOutputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        GTRecipeSerializer.writeCollection(recipe.conditions, buf, GTRecipeSerializer::conditionWriter);

        GTRecipeSerializer.writeCollection(recipe.inputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        GTRecipeSerializer.writeCollection(recipe.outputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        GTRecipeSerializer.writeCollection(recipe.tickInputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        GTRecipeSerializer.writeCollection(recipe.tickOutputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);

        if (GTCEu.isKubeJSLoaded()) {
            GTRecipeSerializer.KJSCallWrapper.writeIngredientActions(recipe.ingredientActions, buf);
        }
        buf.writeNbt(recipe.data);
        buf.writeBoolean(recipe.isFuel);
    }

    public static <T> ArrayList<T> readCollection(RegistryFriendlyByteBuf buf,
                                                  StreamDecoder<? super RegistryFriendlyByteBuf, T> decoder) {
        int i = buf.readVarInt();
        var list = new ArrayList<T>(i);

        for (int j = 0; j < i; j++) {
            list.add(decoder.decode(buf));
        }

        return list;
    }

    public static <T> void writeCollection(Collection<T> collection, RegistryFriendlyByteBuf buf,
                                           StreamEncoder<? super RegistryFriendlyByteBuf, T> encoder) {
        buf.writeVarInt(collection.size());

        for (T t : collection) {
            encoder.encode(buf, t);
        }
    }

    private static MapCodec<GTRecipe> makeCodec(boolean isKubeLoaded) {
        // @formatter:off
        if (!isKubeLoaded) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
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
                            Codec.BOOL.optionalFieldOf("isFuel", false).forGetter(val -> val.isFuel))
                    .apply(instance, (type,
                                      inputs, outputs, tickInputs, tickOutputs,
                                      inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                                      conditions, data, duration, isFuel) ->
                            new GTRecipe(type, inputs, outputs, tickInputs, tickOutputs,
                                    inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                                    conditions, List.of(), data, duration, isFuel)));
        } else {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
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
                            IngredientActionHolder.LIST_CODEC.optionalFieldOf("ingredient_actions", List.of()).forGetter(val -> (List<IngredientActionHolder>) val.ingredientActions),
                            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
                            Codec.BOOL.optionalFieldOf("isFuel", false).forGetter(val -> val.isFuel))
                    .apply(instance, GTRecipe::new));
        }
        // @formatter:on
    }

    public static class KJSCallWrapper {

        public static List<?> getIngredientActions(RegistryFriendlyByteBuf buf) {
            return IngredientActionHolder.LIST_STREAM_CODEC.decode(buf);
        }

        public static void writeIngredientActions(List<?> ingredientActions, RegistryFriendlyByteBuf buf) {
            // noinspection unchecked must be List<?> to be able to load without KJS.
            IngredientActionHolder.LIST_STREAM_CODEC.encode(buf, (List<IngredientActionHolder>) ingredientActions);
        }
    }
}

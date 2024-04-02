package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    ).apply(instance, GTRecipe::new));

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
        String recipeType = buf.readUtf();
        int duration = buf.readVarInt();
        Map<RecipeCapability<?>, List<Content>> inputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickInputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> outputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickOutputs = tuplesToMap(buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::entryReader));
        List<RecipeCondition> conditions = buf.readCollection(c -> new ArrayList<>(), GTRecipeSerializer::conditionReader);
        CompoundTag data = buf.readNbt();
        boolean isFuel = buf.readBoolean();
        return new GTRecipe((GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(new ResourceLocation(recipeType)), inputs, outputs, tickInputs, tickOutputs, conditions, data, duration, isFuel);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, GTRecipe recipe) {
        buf.writeUtf(recipe.recipeType == null ? "dummy" : recipe.recipeType.toString());
        buf.writeVarInt(recipe.duration);
        buf.writeCollection(recipe.inputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.tickInputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.outputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.tickOutputs.entrySet(), GTRecipeSerializer::entryWriter);
        buf.writeCollection(recipe.conditions, GTRecipeSerializer::conditionWriter);
        buf.writeNbt(recipe.data);
        buf.writeBoolean(recipe.isFuel);
    }

}

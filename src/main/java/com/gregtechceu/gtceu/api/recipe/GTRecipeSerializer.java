package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionHolder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("DataFlowIssue")
public class GTRecipeSerializer implements RecipeSerializer<GTRecipe> {

    // spotless:off
    public static final Codec<GTRecipeType> GT_RECIPE_TYPE_CODEC = BuiltInRegistries.RECIPE_TYPE.byNameCodec()
            .comapFlatMap(recipeType -> {
                if (recipeType instanceof GTRecipeType gtRecipeType) {
                    return DataResult.success(gtRecipeType);
                } else {
                    return DataResult.error(() -> "Recipe type " + recipeType + " is not a GTRecipeType");
                }
            }, Function.identity());
    public static final StreamCodec<ByteBuf, GTRecipeType> GT_RECIPE_TYPE_STREAM_CODEC = new StreamCodec<>() {

        private static final StreamCodec<ByteBuf, RecipeType<?>> STREAM_CODEC = ResourceLocation.STREAM_CODEC
                .map(BuiltInRegistries.RECIPE_TYPE::get, BuiltInRegistries.RECIPE_TYPE::getKey);

        @Override
        public @NotNull GTRecipeType decode(@NotNull ByteBuf buffer) {
            RecipeType<?> recipeType = STREAM_CODEC.decode(buffer);
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                throw new DecoderException("Recipe type " + recipeType + " is not a GTRecipeType");
            }
            return gtRecipeType;
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull GTRecipeType value) {
            STREAM_CODEC.encode(buffer, value);
        }
    };

    public static final MapCodec<GTRecipe> CODEC = makeCodec(GTCEu.Mods.isKubeJSLoaded());
    public static final StreamCodec<RegistryFriendlyByteBuf, GTRecipe> STREAM_CODEC = StreamCodec
            .of(GTRecipeSerializer::toNetwork, GTRecipeSerializer::fromNetwork);
    // spotless:on

    @Override
    public @NotNull MapCodec<GTRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GTRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    public static Tuple<RecipeCapability<?>, List<Content>> entryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(buf.readResourceLocation());
        List<Content> contents = readCollection(buf, capability.serializer::fromNetworkContent);
        return new Tuple<>(capability, contents);
    }

    public static Tuple<RecipeCapability<?>, ChanceLogic> changeLogicEntryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(buf.readResourceLocation());
        ChanceLogic logic = GTRegistries.CHANCE_LOGICS.get(buf.readResourceLocation());
        return new Tuple<>(capability, logic);
    }

    public static void entryWriter(RegistryFriendlyByteBuf buf,
                                   Map.Entry<RecipeCapability<?>, ? extends List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        List<Content> contents = entry.getValue();
        buf.writeResourceLocation(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        writeCollection(contents, buf, capability.serializer::toNetworkContent);
    }

    public static void changeLogicEntryWriter(RegistryFriendlyByteBuf buf,
                                              Map.Entry<RecipeCapability<?>, ChanceLogic> entry) {
        RecipeCapability<?> capability = entry.getKey();
        ChanceLogic logic = entry.getValue();
        buf.writeResourceLocation(GTRegistries.RECIPE_CAPABILITIES.getKey(capability));
        buf.writeResourceLocation(GTRegistries.CHANCE_LOGICS.getKey(logic));
    }

    public static RecipeCondition<?> conditionReader(RegistryFriendlyByteBuf buf) {
        RecipeCondition<?> condition = GTRegistries.RECIPE_CONDITIONS.get(buf.readResourceLocation()).factory
                .createDefault();
        return condition.fromNetwork(buf);
    }

    public static void conditionWriter(RegistryFriendlyByteBuf buf, RecipeCondition<?> condition) {
        buf.writeResourceLocation(GTRegistries.RECIPE_CONDITIONS.getKey(condition.getType()));
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

        List<RecipeCondition<?>> conditions = readCollection(buf, GTRecipeSerializer::conditionReader);

        Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics = logicTuplesToMap(
                readCollection(buf, GTRecipeSerializer::changeLogicEntryReader));

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
                type.addDataStickEntry(entry.researchId(), recipe);
            }
        }
        return recipe;
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, GTRecipe recipe) {
        buf.writeResourceLocation(recipe.recipeType.registryName);
        buf.writeResourceLocation(recipe.id);
        buf.writeVarInt(recipe.duration);
        writeCollection(recipe.inputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.tickInputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.outputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.tickOutputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.conditions, buf, GTRecipeSerializer::conditionWriter);

        writeCollection(recipe.inputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        writeCollection(recipe.outputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        writeCollection(recipe.tickInputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);
        writeCollection(recipe.tickOutputChanceLogics.entrySet(), buf,
                GTRecipeSerializer::changeLogicEntryWriter);

        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.writeIngredientActions(recipe.ingredientActions, buf);
        }
        buf.writeNbt(recipe.data);
        buf.writeResourceLocation(recipe.recipeCategory.registryKey);
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
        // spotless::off
        if (!isKubeLoaded) {
            // I'll admit, it's not great.
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    GT_RECIPE_TYPE_CODEC.fieldOf("type").forGetter(val -> val.recipeType),
                    RecipeCapability.CODEC.optionalFieldOf("inputs", Map.of()).forGetter(val -> val.inputs),
                    RecipeCapability.CODEC.optionalFieldOf("outputs", Map.of()).forGetter(val -> val.outputs),
                    RecipeCapability.CODEC.optionalFieldOf("tickInputs", Map.of()).forGetter(val -> val.tickInputs),
                    RecipeCapability.CODEC.optionalFieldOf("tickOutputs", Map.of()).forGetter(val -> val.tickOutputs),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("inputChanceLogics", Map.of()).forGetter(val -> val.inputChanceLogics),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("outputChanceLogics", Map.of()).forGetter(val -> val.outputChanceLogics),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("tickInputChanceLogics", Map.of())
                            .forGetter(val -> val.tickInputChanceLogics),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("tickOutputChanceLogics", Map.of())
                            .forGetter(val -> val.tickOutputChanceLogics),
                    RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of())
                            .forGetter(val -> val.conditions),
                    CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
                    GTRegistries.RECIPE_CATEGORIES.byNameCodec().optionalFieldOf("category", GTRecipeCategory.DEFAULT)
                            .forGetter(val -> val.recipeCategory))
                    .apply(instance, (type,
                                      inputs, outputs, tickInputs, tickOutputs,
                                      inputChanceLogics, outputChanceLogics, tickInputChanceLogics,
                                      tickOutputChanceLogics,
                                      conditions, data, duration, recipeCategory) -> new GTRecipe(type, inputs, outputs,
                                              tickInputs, tickOutputs,
                                              inputChanceLogics, outputChanceLogics, tickInputChanceLogics,
                                              tickOutputChanceLogics,
                                              conditions, List.of(), data, duration, recipeCategory)));
        } else {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    GT_RECIPE_TYPE_CODEC.fieldOf("type").forGetter(val -> val.recipeType),
                    RecipeCapability.CODEC.optionalFieldOf("inputs", Map.of()).forGetter(val -> val.inputs),
                    RecipeCapability.CODEC.optionalFieldOf("outputs", Map.of()).forGetter(val -> val.outputs),
                    RecipeCapability.CODEC.optionalFieldOf("tickInputs", Map.of()).forGetter(val -> val.tickInputs),
                    RecipeCapability.CODEC.optionalFieldOf("tickOutputs", Map.of()).forGetter(val -> val.tickOutputs),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("inputChanceLogics", Map.of()).forGetter(val -> val.inputChanceLogics),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("outputChanceLogics", Map.of()).forGetter(val -> val.outputChanceLogics),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("tickInputChanceLogics", Map.of())
                            .forGetter(val -> val.tickInputChanceLogics),
                    Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.byNameCodec())
                            .optionalFieldOf("tickOutputChanceLogics", Map.of())
                            .forGetter(val -> val.tickOutputChanceLogics),
                    RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of())
                            .forGetter(val -> val.conditions),
                    IngredientActionHolder.CODEC.listOf().optionalFieldOf("kubejs:actions", List.of())
                            .forGetter(val -> (List<IngredientActionHolder>) val.ingredientActions),
                    CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(val -> val.duration),
                    GTRegistries.RECIPE_CATEGORIES.byNameCodec().optionalFieldOf("category", GTRecipeCategory.DEFAULT)
                            .forGetter(val -> val.recipeCategory))
                    .apply(instance, GTRecipe::new));
        }
        // spotless::on
    }

    public static class KJSCallWrapper {

        public static final StreamCodec<RegistryFriendlyByteBuf, List<IngredientActionHolder>> STREAM_CODEC = IngredientActionHolder.STREAM_CODEC
                .apply(ByteBufCodecs.list());

        public static List<?> getIngredientActions(RegistryFriendlyByteBuf buf) {
            return STREAM_CODEC.decode(buf);
        }

        public static void writeIngredientActions(List<?> ingredientActions, RegistryFriendlyByteBuf buf) {
            // noinspection unchecked must be List<?> to be able to load without KJS.
            STREAM_CODEC.encode(buf, (List<IngredientActionHolder>) ingredientActions);
        }
    }
}

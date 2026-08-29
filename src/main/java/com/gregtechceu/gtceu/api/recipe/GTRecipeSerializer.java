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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.gregtechceu.gtceu.utils.codec.GTCodecUtils.quietExceptionCodec;

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

    public static final Codec<Map<RecipeCapability<?>, ChanceLogic>> CHANCE_LOGIC_MAP_CODEC = Codec.
            unboundedMap(GTRegistries.RECIPE_CAPABILITIES.byNameCodec(), GTRegistries.CHANCE_LOGICS.byNameCodec());

    public static final MapCodec<GTRecipe> CODEC = makeCodec(GTCEu.Mods.isKubeJSLoaded());
    public static final StreamCodec<RegistryFriendlyByteBuf, GTRecipe> STREAM_CODEC = StreamCodec
            .of(GTRecipeSerializer::toNetwork, GTRecipeSerializer::fromNetwork);
    public static final StreamCodec<RegistryFriendlyByteBuf, GTRecipe> DATAPACK_SYNC_STREAM_CODEC = StreamCodec
            .of(GTRecipeSerializer::toNetwork, GTRecipeSerializer::datapackSyncFromNetwork);
    // spotless:on

    @Override
    public @NotNull MapCodec<GTRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GTRecipe> streamCodec() {
        return DATAPACK_SYNC_STREAM_CODEC;
    }

    public static Tuple<RecipeCapability<?>, List<Content>> entryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = buf.registryAccess()
                .holderOrThrow(buf.readResourceKey(GTRegistries.Keys.RECIPE_CAPABILITY)).value();
        List<Content> contents = readCollection(buf, capability.serializer::fromNetworkContent);
        return new Tuple<>(capability, contents);
    }

    public static Tuple<RecipeCapability<?>, ChanceLogic> changeLogicEntryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = buf.registryAccess()
                .holderOrThrow(buf.readResourceKey(GTRegistries.Keys.RECIPE_CAPABILITY)).value();
        ChanceLogic logic = buf.registryAccess().holderOrThrow(buf.readResourceKey(GTRegistries.Keys.CHANCE_LOGIC))
                .value();
        return new Tuple<>(capability, logic);
    }

    public static void entryWriter(RegistryFriendlyByteBuf buf,
                                   Map.Entry<RecipeCapability<?>, ? extends List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        List<Content> contents = entry.getValue();
        buf.writeResourceKey(buf.registryAccess().registryOrThrow(GTRegistries.Keys.RECIPE_CAPABILITY)
                .getResourceKey(capability).orElseThrow());
        writeCollection(contents, buf, capability.serializer::toNetworkContent);
    }

    public static void changeLogicEntryWriter(RegistryFriendlyByteBuf buf,
                                              Map.Entry<RecipeCapability<?>, ChanceLogic> entry) {
        RecipeCapability<?> capability = entry.getKey();
        ChanceLogic logic = entry.getValue();
        buf.writeResourceKey(buf.registryAccess().registryOrThrow(GTRegistries.Keys.RECIPE_CAPABILITY)
                .getResourceKey(capability).orElseThrow());
        buf.writeResourceKey(buf.registryAccess().registryOrThrow(GTRegistries.Keys.CHANCE_LOGIC).getResourceKey(logic)
                .orElseThrow());
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

    public static GTRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation recipeType = buf.readResourceLocation();
        ResourceLocation id = buf.readResourceLocation();
        Map<RecipeCapability<?>, List<Content>> inputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickInputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> outputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickOutputs = tuplesToMap(
                readCollection(buf, GTRecipeSerializer::entryReader));

        List<RecipeCondition<?>> conditions = readCollection(buf, RecipeCondition::fromNetwork);

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

    public static GTRecipe datapackSyncFromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
        GTRecipe recipe = fromNetwork(buf);

        recipe.recipeCategory.addRecipe(recipe);

        // a little special piece of code for loading all the research entries into the recipe type's list on the
        // client.
        ResearchCondition researchCondition = recipe.conditions.stream().filter(ResearchCondition.class::isInstance)
                .findAny()
                .map(ResearchCondition.class::cast).orElse(null);
        if (researchCondition != null) {
            for (ResearchData.ResearchEntry entry : researchCondition.data) {
                recipe.recipeType.addDataStickEntry(entry.researchId(), recipe);
            }
        }
        return recipe;
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, @Nullable GTRecipe recipe) {
        buf.writeResourceLocation(recipe.recipeType.registryName);
        buf.writeResourceLocation(recipe.id);
        writeCollection(recipe.inputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.tickInputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.outputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollection(recipe.tickOutputs.entrySet(), buf, GTRecipeSerializer::entryWriter);
        writeCollectionWithMember(recipe.conditions, buf, RecipeCondition::toNetwork);

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
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.parallels);
        buf.writeVarInt(recipe.subtickParallels);
        buf.writeVarInt(recipe.batchParallels);
        buf.writeInt(recipe.groupColor);
        buf.writeResourceLocation(recipe.recipeCategory.registryKey);
        buf.writeBoolean(recipe.keepSpoilingProgress);
    }

    public static <T> ArrayList<T> readCollection(RegistryFriendlyByteBuf buf,
                                                  StreamDecoder<? super RegistryFriendlyByteBuf, T> decoder) {
        int len = buf.readVarInt();
        var list = new ArrayList<T>(len);

        for (int i = 0; i < len; i++) {
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

    public static <T> void writeCollectionWithMember(Collection<T> collection, RegistryFriendlyByteBuf buf,
                                                     StreamMemberEncoder<? super RegistryFriendlyByteBuf, T> encoder) {
        buf.writeVarInt(collection.size());

        for (T t : collection) {
            encoder.encode(t, buf);
        }
    }

    /**
     * Codecs can only have up to 16 inputs. This is at 16 now, so the three recipe Parallel/Batch values are
     * condensed to a List.
     */
    @SuppressWarnings("unchecked")
    private static MapCodec<GTRecipe> makeCodec(boolean isKubeLoaded) {
        // spotless:off
        if (!isKubeLoaded) {
            // I'll admit, it's not great.
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            GT_RECIPE_TYPE_CODEC.fieldOf("type").forGetter(val -> val.recipeType),
                            RecipeIO.CODEC.forGetter(GTRecipe::getRecipeIO),
                            RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(val -> val.conditions),
                            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                            quietExceptionCodec(ExtraCodecs.NON_NEGATIVE_INT, "duration", false).forGetter(val -> val.duration),
                            RecipeParallels.CODEC.optionalFieldOf("all_parallels", new RecipeParallels(1, 1, 1)).forGetter(val -> new RecipeParallels(val.parallels, val.subtickParallels, val.batchParallels)),
                            GTRegistries.RECIPE_CATEGORIES.byNameCodec().optionalFieldOf("category", GTRecipeCategory.DEFAULT).forGetter(val -> val.recipeCategory),
                            Codec.INT.optionalFieldOf("groupColor", -1).forGetter(val -> val.groupColor),
                            Codec.BOOL.optionalFieldOf("keepSpoilingProgress", true).forGetter(val -> val.keepSpoilingProgress))
                    .apply(instance, (type,
                                      recipeIO,
                                      conditions, data, duration, allParallels, recipeCategory, groupColor, keepSpoilingProgress) ->
                            new GTRecipe(type, recipeIO,
                                    conditions, List.of(), data, duration, allParallels, recipeCategory, groupColor, keepSpoilingProgress)));
        } else {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    GT_RECIPE_TYPE_CODEC.fieldOf("type").forGetter(val -> val.recipeType),
                            RecipeIO.CODEC.forGetter(GTRecipe::getRecipeIO),
                    RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(val -> val.conditions),
                    IngredientActionHolder.CODEC.listOf().optionalFieldOf("kubejs:actions", List.of()).forGetter(val -> (List<IngredientActionHolder>) val.ingredientActions),
                    CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(val -> val.data),
                    quietExceptionCodec(ExtraCodecs.NON_NEGATIVE_INT, "duration", true).forGetter(val -> val.duration),
                    RecipeParallels.CODEC.optionalFieldOf("all_parallels", new RecipeParallels(1, 1, 1)).forGetter(val -> new RecipeParallels(val.parallels, val.subtickParallels, val.batchParallels)),
                    GTRegistries.RECIPE_CATEGORIES.byNameCodec().optionalFieldOf("category", GTRecipeCategory.DEFAULT).forGetter(val -> val.recipeCategory),
                    Codec.INT.optionalFieldOf("groupColor", -1).forGetter(val -> val.groupColor),
            Codec.BOOL.optionalFieldOf("keepSpoilingProgress", true).forGetter(val -> val.keepSpoilingProgress))
                    .apply(instance, GTRecipe::new));
        }
        // spotless:on
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

    // spotless:off
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
                CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("inputChanceLogics", Map.of()).forGetter(val -> val.inputChanceLogics),
                CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("outputChanceLogics", Map.of()).forGetter(val -> val.outputChanceLogics),
                CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("tickInputChanceLogics", Map.of()).forGetter(val -> val.tickInputChanceLogics),
                CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("tickOutputChanceLogics", Map.of()).forGetter(val -> val.tickOutputChanceLogics))
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
    // spotless:on
}

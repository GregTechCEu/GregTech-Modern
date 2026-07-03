package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTRecipe {

    public final GTRecipeType recipeType;
    @Getter
    @Setter
    public ResourceLocation id;
    public final ContentListMap inputs;
    public final ContentListMap outputs;
    public final ContentListMap tickInputs;
    public final ContentListMap tickOutputs;

    public final List<RecipeCondition<?>> conditions;
    @NotNull
    public CompoundTag data;
    public int tier;
    public int duration;
    public int parallels = 1;
    public int subtickParallels = 1;
    public int batchParallels = 1;
    public int ocLevel = 0;
    public final GTRecipeCategory recipeCategory;

    public long getInputEUt() {
        return RecipeHelper.calculateEUt(tickInputs);
    }

    public long getOutputEUt() {
        return RecipeHelper.calculateEUt(tickOutputs);
    }

    public GTRecipe(GTRecipeType recipeType,
                    @Nullable ResourceLocation id,
                    ContentListMap inputs,
                    ContentListMap outputs,
                    ContentListMap tickInputs,
                    ContentListMap tickOutputs,

                    List<RecipeCondition<?>> conditions,
                    CompoundTag data,
                    int tier,
                    int duration,
                    GTRecipeCategory recipeCategory) {
        this.recipeType = recipeType;
        this.id = id;

        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;

        this.conditions = conditions;
        this.data = data;
        this.tier = tier;
        this.duration = duration;
        this.recipeCategory = (recipeCategory != GTRecipeCategory.DEFAULT) ? recipeCategory : recipeType.getCategory();
    }

    public GTRecipe copy() {
        return copy(1, false);
    }

    public GTRecipe copy(int multiplier) {
        return copy(multiplier, true);
    }

    public GTRecipe copy(int multiplier, boolean modifyDuration) {
        var copied = new GTRecipe(recipeType, id,
                inputs.copyWithMultiplier(multiplier), outputs.copyWithMultiplier(multiplier),
                tickInputs.copyWithMultiplier(multiplier), tickOutputs.copyWithMultiplier(multiplier),
                new ArrayList<>(conditions), data.copy(), tier, duration, recipeCategory);
        if (modifyDuration) {
            copied.duration = duration * multiplier;
        }
        copied.ocLevel = ocLevel;
        copied.parallels = parallels;
        copied.batchParallels = batchParallels;
        copied.subtickParallels = subtickParallels;
        return copied;
    }

    public GTRecipe copy(int multiplier, boolean modifyTick, boolean modifyDuration) {
        if (modifyTick) {
            return copy(multiplier, modifyDuration);
        } else {
            var copied = new GTRecipe(recipeType, id,
                    inputs.copyWithMultiplier(multiplier), outputs.copyWithMultiplier(multiplier),
                    tickInputs.copy(), tickOutputs.copy(),
                    new ArrayList<>(conditions), data.copy(), tier, duration, recipeCategory);
            if (modifyDuration) {
                copied.duration = duration * multiplier;
            }
            copied.ocLevel = ocLevel;
            copied.parallels = parallels;
            copied.batchParallels = batchParallels;
            copied.subtickParallels = subtickParallels;
            return copied;
        }
    }

    public GTRecipeType getType() {
        return recipeType;
    }

    public <T> List<T> getInputContents(RecipeCapability<T> capability) {
        return inputs.getOrDefault(capability, Collections.emptyList());
    }

    public <T> List<T> getOutputContents(RecipeCapability<T> capability) {
        return outputs.getOrDefault(capability, Collections.emptyList());
    }

    public <T> List<T> getTickInputContents(RecipeCapability<T> capability) {
        return tickInputs.getOrDefault(capability, Collections.emptyList());
    }

    public <T> List<T> getTickOutputContents(RecipeCapability<T> capability) {
        return tickOutputs.getOrDefault(capability, Collections.emptyList());
    }

    public boolean hasTick() {
        return !tickInputs.isEmpty() || !tickOutputs.isEmpty();
    }

    public int getTotalRuns() {
        return parallels * subtickParallels * batchParallels;
    }

    public void multiplyInputs(int multiplier) {
        inputs.multiply(multiplier);
    }

    public void multiplyOutputs(int multiplier) {
        outputs.multiply(multiplier);
    }

    public void multiplyTickInputs(int multiplier) {
        tickInputs.multiply(multiplier);
    }

    public void multiplyTickOutputs(int multiplier) {
        tickOutputs.multiply(multiplier);
    }

    public void multiplyTickContents(int multiplier) {
        multiplyTickInputs(multiplier);
        multiplyTickOutputs(multiplier);
    }

    public void multiplyAllContents(int multiplier) {
        multiplyInputs(multiplier);
        multiplyOutputs(multiplier);
        multiplyTickInputs(multiplier);
        multiplyTickOutputs(multiplier);
    }

    public void multiplyDuration(double multiplier) {
        duration = Math.max(1, (int) (duration * multiplier));
    }

    public void multiplyEUt(double multiplier) {
        long eut = RecipeHelper.getRealEUtWithIO(this);
        if (eut == 0) return;
        long modified = (long) (eut * multiplier);
        if (modified != 0) {
            EURecipeCapability.putEUContent(modified > 0 ? tickInputs : tickOutputs, Math.abs(modified));
        }
    }

    public void addOCs(int amount) {
        ocLevel += amount;
        if (data.getBoolean("duration_is_total_cwu")) {
            duration = (int) Math.max(1, duration * (1f - 0.025f * amount));
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(recipeType.registryName);
        buf.writeResourceLocation(id);
        inputs.toNetwork(buf);
        outputs.toNetwork(buf);
        tickInputs.toNetwork(buf);
        tickOutputs.toNetwork(buf);
        buf.writeCollection(conditions, (buffer, condition) -> condition.toNetwork(buffer));
        buf.writeNbt(data);
        buf.writeResourceLocation(recipeCategory.registryKey);
        buf.writeVarInt(tier);
        buf.writeVarInt(duration);
        buf.writeVarInt(parallels);
        buf.writeVarInt(subtickParallels);
        buf.writeVarInt(batchParallels);
        buf.writeVarInt(ocLevel);
    }

    public static GTRecipe fromNetwork(FriendlyByteBuf buf) {
        GTRecipeType recipeType = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(buf.readResourceLocation());
        ResourceLocation id = buf.readResourceLocation();
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
        int tier = buf.readVarInt();
        int duration = buf.readVarInt();
        int parallels = buf.readVarInt();
        int subtickParallels = buf.readVarInt();
        int batchParallels = buf.readVarInt();
        int ocLevel = buf.readVarInt();

        GTRecipe recipe = new GTRecipe(recipeType, id, inputs, outputs, tickInputs, tickOutputs, conditions, data, tier,
                duration, category);
        recipe.parallels = parallels;
        recipe.subtickParallels = subtickParallels;
        recipe.batchParallels = batchParallels;
        recipe.ocLevel = ocLevel;
        return recipe;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        var ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        tag.putString("recipeType", recipeType.registryName.toString());
        tag.putString("id", id.toString());
        tag.putInt("tier", tier);
        tag.putInt("duration", duration);
        tag.put("inputs",
                ContentListMap.CODEC.encodeStart(NbtOps.INSTANCE, inputs).getOrThrow(false, GTCEu.LOGGER::error));
        tag.put("outputs",
                ContentListMap.CODEC.encodeStart(NbtOps.INSTANCE, outputs).getOrThrow(false, GTCEu.LOGGER::error));
        tag.put("tickInputs",
                ContentListMap.CODEC.encodeStart(NbtOps.INSTANCE, tickInputs).getOrThrow(false, GTCEu.LOGGER::error));
        tag.put("tickOutputs",
                ContentListMap.CODEC.encodeStart(NbtOps.INSTANCE, tickOutputs).getOrThrow(false, GTCEu.LOGGER::error));
        tag.put("conditions",
                Codec.list(RecipeCondition.CODEC).encodeStart(ops, conditions).getOrThrow(false, GTCEu.LOGGER::error));
        tag.put("data", data);
        tag.putString("category", recipeCategory.registryKey.toString());
        tag.putInt("parallels", parallels);
        tag.putInt("subtickParallels", subtickParallels);
        tag.putInt("batchParallels", batchParallels);
        tag.putInt("ocLevel", ocLevel);
        return tag;
    }

    public static GTRecipe fromNBT(CompoundTag tag) {
        var ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        GTRecipeType recipeType = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE
                .get(new ResourceLocation(tag.getString("recipeType")));
        ResourceLocation id = new ResourceLocation(tag.getString("id"));
        int tier = tag.getInt("tier");
        int duration = tag.getInt("duration");
        ContentListMap inputs = ContentListMap.CODEC.parse(NbtOps.INSTANCE, tag.get("inputs")).getOrThrow(false,
                GTCEu.LOGGER::error);
        ContentListMap outputs = ContentListMap.CODEC.parse(NbtOps.INSTANCE, tag.get("outputs")).getOrThrow(false,
                GTCEu.LOGGER::error);
        ContentListMap tickInputs = ContentListMap.CODEC.parse(NbtOps.INSTANCE, tag.get("tickInputs")).getOrThrow(false,
                GTCEu.LOGGER::error);
        ContentListMap tickOutputs = ContentListMap.CODEC.parse(NbtOps.INSTANCE, tag.get("tickOutputs"))
                .getOrThrow(false, GTCEu.LOGGER::error);
        List<RecipeCondition<?>> conditions = Codec.list(RecipeCondition.CODEC).parse(ops, tag.get("conditions"))
                .getOrThrow(false, GTCEu.LOGGER::error);
        CompoundTag data = tag.getCompound("data");
        GTRecipeCategory category = GTRegistries.RECIPE_CATEGORIES.get(new ResourceLocation(tag.getString("category")));
        int parallels = tag.getInt("parallels");
        int subtickParallels = tag.getInt("subtickParallels");
        int batchParallels = tag.getInt("batchParallels");
        int ocLevel = tag.getInt("ocLevel");

        GTRecipe recipe = new GTRecipe(recipeType, id, inputs, outputs, tickInputs, tickOutputs, conditions, data, tier,
                duration, category);
        recipe.parallels = parallels;
        recipe.subtickParallels = subtickParallels;
        recipe.batchParallels = batchParallels;
        recipe.ocLevel = ocLevel;
        return recipe;
    }
}

package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true)
public abstract class RecipeCondition {

    public static final Codec<RecipeCondition> CODEC = GTRegistries.RECIPE_CONDITIONS.codec()
            .dispatch(RecipeCondition::getType, RecipeConditionType::getCodec);

    public static <
            RC extends RecipeCondition> Products.P1<RecordCodecBuilder.Mu<RC>, Boolean> isReverse(RecordCodecBuilder.Instance<RC> instance) {
        return instance.group(Codec.BOOL.optionalFieldOf("reverse", false).forGetter(val -> val.isReverse));
    }

    @Getter
    @Setter
    protected boolean isReverse;

    public RecipeCondition() {
        this(false);
    }

    public RecipeCondition(boolean isReverse) {
        this.isReverse = isReverse;
    }

    public abstract RecipeConditionType<?> getType();

    public String getTranslationKey() {
        return "gtceu.recipe.condition." + getType();
    }

    public IGuiTexture getInValidTexture() {
        return new ResourceTexture("gtceu:textures/gui/condition/" + getType() + ".png").getSubTexture(0, 0, 1, 0.5f);
    }

    public IGuiTexture getValidTexture() {
        return new ResourceTexture("gtceu:textures/gui/condition/" + getType() + ".png").getSubTexture(0, 0.5f, 1,
                0.5f);
    }

    public boolean isOr() {
        return false;
    }

    public abstract Component getTooltips();

    public boolean check(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        boolean test = testCondition(recipe, recipeLogic);
        return test != isReverse;
    }

    protected abstract boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic);

    public abstract RecipeCondition createTemplate();

    @NotNull
    public final JsonObject serialize() {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return CODEC.encodeStart(ops, this).getOrThrow(false, GTCEu.LOGGER::error).getAsJsonObject();
    }

    public static RecipeCondition deserialize(@NotNull JsonObject config) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return CODEC.decode(ops, config).getOrThrow(false, GTCEu.LOGGER::error).getFirst();
    }

    public final void toNetwork(FriendlyByteBuf buf) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        // Code below was taken from buf.writeJsonWithCodec to include our RegistryOps
        DataResult<JsonElement> dataresult = CODEC.encodeStart(ops, this);
        buf.writeUtf(new Gson().toJson((JsonElement) Util.getOrThrow(dataresult,
                (p_261421_) -> new EncoderException("Failed to encode: " + p_261421_ + " " + String.valueOf(this)))));
    }

    public static RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        // Code below was taken from buf.readJsonWithCodec to include our RegistryOps
        JsonElement jsonelement = (JsonElement) GsonHelper.fromJson(new Gson(), buf.readUtf(), JsonElement.class);
        DataResult<RecipeCondition> dataresult = CODEC.parse(ops, jsonelement);
        return (RecipeCondition) Util.getOrThrow(dataresult,
                (p_272382_) -> new DecoderException("Failed to decode json: " + p_272382_));
    }
}

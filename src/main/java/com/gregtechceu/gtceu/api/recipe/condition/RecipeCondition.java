package com.gregtechceu.gtceu.api.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Accessors(chain = true)
public abstract class RecipeCondition<T extends RecipeCondition<T>> {

    public static final Codec<RecipeCondition<?>> CODEC = GTRegistries.RECIPE_CONDITIONS.byNameCodec()
            .dispatch(RecipeCondition::getType, RecipeConditionType::getCodec);

    // spotless:off
    public static <RC extends RecipeCondition<RC>> Products.P1<RecordCodecBuilder.Mu<RC>, Boolean> isReverse(RecordCodecBuilder.Instance<RC> instance) {
        return instance.group(Codec.BOOL.optionalFieldOf("reverse", false).forGetter(val -> val.isReverse));
    }
    // spotless:on

    public static <RC extends RecipeCondition<RC>> MapCodec<RC> simpleCodec(Function<Boolean, RC> function) {
        return RecordCodecBuilder.mapCodec(instance -> isReverse(instance).apply(instance, function));
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

    public abstract RecipeConditionType<T> getType();

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

    public abstract T createTemplate();

    @NotNull
    public final JsonObject serialize() {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return CODEC.encodeStart(ops, this).getOrThrow().getAsJsonObject();
    }

    public static RecipeCondition<?> deserialize(@NotNull JsonObject config) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return CODEC.decode(ops, config).getOrThrow().getFirst();
    }

    public final void toNetwork(FriendlyByteBuf buf) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        DataResult<JsonElement> dataresult = CODEC.encodeStart(ops, this);
        buf.writeUtf(new Gson().toJson(dataresult.getOrThrow(
                (s) -> new EncoderException("Failed to encode: " + s + " " + this))));
    }

    public static RecipeCondition<?> fromNetwork(FriendlyByteBuf buf) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        // Code below was taken from buf.readJsonWithCodec to include our RegistryOps
        JsonElement jsonelement = GsonHelper.fromJson(new Gson(), buf.readUtf(), JsonElement.class);
        DataResult<RecipeCondition<?>> dataresult = CODEC.parse(ops, jsonelement);
        return dataresult.getOrThrow(
                (s) -> new DecoderException("Failed to decode json: " + s));
    }
}

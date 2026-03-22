package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;

import com.google.gson.JsonObject;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Accessors(chain = true)
public abstract class RecipeCondition<T extends RecipeCondition<T>> {

    public static final Codec<RecipeCondition<?>> CODEC = GTRegistries.RECIPE_CONDITIONS.codec()
            .dispatch(RecipeCondition::getType, RecipeConditionType::getCodec);

    // spotless:off
    public static <RC extends RecipeCondition<RC>> Products.P1<RecordCodecBuilder.Mu<RC>, Boolean> isReverse(RecordCodecBuilder.Instance<RC> instance) {
        return instance.group(Codec.BOOL.optionalFieldOf("reverse", false).forGetter(val -> val.isReverse));
    }
    // spotless:on

    public static <RC extends RecipeCondition<RC>> Codec<RC> simpleCodec(Function<Boolean, RC> function) {
        return RecordCodecBuilder.create(instance -> isReverse(instance).apply(instance, function));
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
        return CODEC.encodeStart(ops, this).getOrThrow(false, GTCEu.LOGGER::error).getAsJsonObject();
    }

    public static RecipeCondition<?> deserialize(@NotNull JsonObject config) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return CODEC.parse(ops, config).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @SuppressWarnings("deprecation")
    public final void toNetwork(FriendlyByteBuf buf) {
        var ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        buf.writeWithCodec(ops, CODEC, this);
    }

    @SuppressWarnings("deprecation")
    public static RecipeCondition<?> fromNetwork(FriendlyByteBuf buf) {
        var ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        return buf.readWithCodec(ops, CODEC);
    }
}

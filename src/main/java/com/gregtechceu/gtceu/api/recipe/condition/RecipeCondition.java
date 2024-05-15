package com.gregtechceu.gtceu.api.recipe.condition;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote RecipeCondition, global conditions
 */
public abstract class RecipeCondition {

    public static final Codec<RecipeCondition> CODEC = GTRegistries.RECIPE_CONDITIONS.codec()
            .dispatch(RecipeCondition::getType, RecipeConditionType::getCodec);

    @Nullable
    public static RecipeCondition create(Class<? extends RecipeCondition> clazz) {
        if (clazz == null) return null;
        try {
            return clazz.newInstance();
        } catch (Exception ignored) {
            GTCEu.LOGGER.error("condition {} has no NonArgsConstructor", clazz);
            return null;
        }
    }

    public static <
            RC extends RecipeCondition> Products.P1<RecordCodecBuilder.Mu<RC>, Boolean> isReverse(RecordCodecBuilder.Instance<RC> instance) {
        return instance.group(Codec.BOOL.fieldOf("reverse").forGetter(val -> val.isReverse));
    }

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

    public boolean isReverse() {
        return isReverse;
    }

    public boolean isOr() {
        return false;
    }

    public RecipeCondition setReverse(boolean reverse) {
        isReverse = reverse;
        return this;
    }

    public abstract Component getTooltips();

    public abstract boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic);

    public abstract RecipeCondition createTemplate();

    @NotNull
    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        if (isReverse) {
            jsonObject.addProperty("reverse", true);
        }
        return jsonObject;
    }

    public RecipeCondition deserialize(@NotNull JsonObject config) {
        isReverse = GsonHelper.getAsBoolean(config, "reverse", false);
        return this;
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(isReverse);
    }

    public RecipeCondition fromNetwork(RegistryFriendlyByteBuf buf) {
        isReverse = buf.readBoolean();
        return this;
    }
}

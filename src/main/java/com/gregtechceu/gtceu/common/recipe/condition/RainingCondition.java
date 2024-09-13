package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;

import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote WhetherCondition, specific whether
 */
@NoArgsConstructor
public class RainingCondition extends RecipeCondition {

    public static final Codec<RainingCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.FLOAT.fieldOf("level").forGetter(val -> val.level))
                    .apply(instance, RainingCondition::new));

    public final static RainingCondition INSTANCE = new RainingCondition();
    private float level;

    public RainingCondition(boolean isReverse, float level) {
        super(isReverse);
        this.level = level;
    }

    public RainingCondition(float level) {
        this.level = level;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.RAINING;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.rain.tooltip", level);
    }

    public float getLevel() {
        return level;
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && level.getRainLevel(1) >= this.level;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new RainingCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("level", level);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        level = GsonHelper.getAsFloat(config, "level", 0);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        level = buf.readFloat();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeFloat(level);
    }
}

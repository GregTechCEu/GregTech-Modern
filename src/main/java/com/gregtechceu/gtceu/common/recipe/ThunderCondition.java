package com.gregtechceu.gtceu.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote WhetherCondition, specific whether
 */
@NoArgsConstructor
public class ThunderCondition extends RecipeCondition {

    public final static ThunderCondition INSTANCE = new ThunderCondition();
    private float level;

    public ThunderCondition(float level) {
        this.level = level;
    }

    @Override
    public String getType() {
        return "thunder";
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.thunder.tooltip", level);
    }

    public float getLevel() {
        return level;
    }

    @Override
    public boolean test(@Nonnull GTRecipe recipe, @Nonnull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && level.getThunderLevel(1) >= this.level;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new ThunderCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("level", level);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
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

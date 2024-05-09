package com.gregtechceu.gtceu.common.recipes;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipes.GTRecipe;
import com.gregtechceu.gtceu.api.recipes.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.machines.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipes.condition.RecipeConditionType;
import com.gregtechceu.gtceu.data.GTRecipeConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote WhetherCondition, specific whether
 */
@NoArgsConstructor
public class PositionYCondition extends RecipeCondition {
    public static final MapCodec<PositionYCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance)
        .and(instance.group(
            Codec.INT.fieldOf("min").forGetter(val -> val.min),
            Codec.INT.fieldOf("max").forGetter(val -> val.max)
        )).apply(instance, PositionYCondition::new));

    public final static PositionYCondition INSTANCE = new PositionYCondition();
    private int min;
    private int max;

    public PositionYCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public PositionYCondition(boolean isReverse, int min, int max) {
        super(isReverse);
        this.min = min;
        this.max = max;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.POSITION_Y;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.pos_y.tooltip", this.min, this.max);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        int y = recipeLogic.machine.self().getPos().getY();
        return y >= this.min && y <= this.max;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new PositionYCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("min", this.min);
        config.addProperty("max", this.max);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        min = GsonHelper.getAsInt(config, "min", Integer.MIN_VALUE);
        max = GsonHelper.getAsInt(config, "max", Integer.MAX_VALUE);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(RegistryFriendlyByteBuf buf) {
        super.fromNetwork(buf);
        min = buf.readVarInt();
        max = buf.readVarInt();
        return this;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeVarInt(min);
        buf.writeVarInt(max);
    }

}

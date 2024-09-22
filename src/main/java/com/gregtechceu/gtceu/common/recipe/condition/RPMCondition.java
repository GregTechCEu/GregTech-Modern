package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote WhetherCondition, specific whether
 */
@NoArgsConstructor
public class RPMCondition extends RecipeCondition {

    public static final Codec<RPMCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.FLOAT.fieldOf("rpm").forGetter(val -> val.rpm))
                    .apply(instance, RPMCondition::new));

    public final static RPMCondition INSTANCE = new RPMCondition();
    private float rpm;

    public RPMCondition(boolean isReverse, float rpm) {
        super(isReverse);
        this.rpm = rpm;
    }

    public RPMCondition(float rpm) {
        this.rpm = rpm;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.RPM;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.rpm.tooltip", rpm);
    }

    public float getRpm() {
        return rpm;
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (recipeLogic.machine instanceof IKineticMachine kineticMachine &&
                Math.abs(kineticMachine.getKineticHolder().getSpeed()) >= rpm) {
            return true;
        }
        if (recipeLogic.machine instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof IKineticMachine kineticMachine &&
                        Math.abs(kineticMachine.getKineticHolder().getSpeed()) >= rpm) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new RPMCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("rpm", rpm);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        rpm = GsonHelper.getAsFloat(config, "rpm", 0);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        rpm = buf.readFloat();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeFloat(rpm);
    }
}

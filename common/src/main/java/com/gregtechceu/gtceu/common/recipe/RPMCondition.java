package com.gregtechceu.gtceu.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote WhetherCondition, specific whether
 */
@NoArgsConstructor
public class RPMCondition extends RecipeCondition {

    public final static RPMCondition INSTANCE = new RPMCondition();
    private float rpm;

    public RPMCondition(float rpm) {
        this.rpm = rpm;
    }

    @Override
    public String getType() {
        return "rpm";
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.rpm.tooltip", rpm);
    }

    public float getRpm() {
        return rpm;
    }

    @Override
    public boolean test(@Nonnull GTRecipe recipe, @Nonnull RecipeLogic recipeLogic) {
        if (recipeLogic.machine instanceof IKineticMachine kineticMachine && Math.abs(kineticMachine.getKineticHolder().getSpeed()) >= rpm) {
            return true;
        }
        if (recipeLogic.machine instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof IKineticMachine kineticMachine && Math.abs(kineticMachine.getKineticHolder().getSpeed()) >= rpm) {
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

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("rpm", rpm);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
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

package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.ToolBoxBehavior;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ToolIngredient extends AbstractIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("tool_ingredient");
    private final GTToolType toolType;

    public ToolIngredient(GTToolType toolType) {
        super(Stream.of(
                new Ingredient.TagValue(toolType.craftingTags.get(0)),
                new Ingredient.ItemValue(GTItems.TOOL_BOX.asStack())));
        this.toolType = toolType;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        } else {
            if (input.is(toolType.craftingTags.get(0))) {
                return true;
            }
            if (input.is(GTItems.TOOL_BOX.asItem())) {
                if (ToolBoxBehavior.getAvailableTools(input).contains(toolType.craftingTags.get(0))) {
                    input.getOrCreateTagElement("last_used_tool").putString("type", toolType.name);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IIngredientSerializer<? extends Ingredient> getSerializer() {
        return ToolIngredient.Serializer.INSTANCE;
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("toolType", toolType.name);
        return json;
    }

    public static class Serializer implements IIngredientSerializer<ToolIngredient> {

        public static final ToolIngredient.Serializer INSTANCE = new ToolIngredient.Serializer();

        @Override
        public @NotNull ToolIngredient parse(FriendlyByteBuf buffer) {
            return new ToolIngredient(GTToolType.getTypes().get(buffer.readUtf()));
        }

        @Override
        public @NotNull ToolIngredient parse(@NotNull JsonObject json) {
            return new ToolIngredient(GTToolType.getTypes().get(json.get("toolType").getAsString()));
        }

        @Override
        public void write(FriendlyByteBuf buffer, ToolIngredient ingredient) {
            buffer.writeUtf(ingredient.toolType.name);
        }
    }
}

package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.common.item.ToolBoxBehavior.LAST_USED_TOOL_TAG;
import static com.gregtechceu.gtceu.common.item.ToolBoxBehavior.TOOL_TYPES;

public class ToolIngredient extends AbstractIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("tool");

    private final GTToolType toolType;
    private ItemStack[] cachedStacks;

    public ToolIngredient(GTToolType toolType) {
        super(Stream.of(new Ingredient.TagValue(toolType.craftingTags.get(0))));
        this.toolType = toolType;
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (cachedStacks == null) {
            List<ItemStack> stacks = new ArrayList<>();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(toolType.craftingTags.get(0))) {
                stacks.add(new ItemStack(holder));
            }
            stacks.add(GTItems.TOOL_BOX.asStack());
            cachedStacks = stacks.toArray(ItemStack[]::new);
        }
        return cachedStacks;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        if (input.is(toolType.craftingTags.get(0))) {
            return true;
        }
        if (input.is(GTItems.TOOL_BOX.asItem()) &&
                input.getOrCreateTag().getString(TOOL_TYPES).contains(toolType.name)) {
            input.getOrCreateTag().putString(LAST_USED_TOOL_TAG, toolType.name);
            return true;
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("toolType", toolType.name);
        return json;
    }

    public static class Serializer implements IIngredientSerializer<ToolIngredient> {

        public static final Serializer INSTANCE = new Serializer();

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

package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.NBTIngredient;
import com.gregtechceu.gtceu.data.recipe.misc.VanillaFluidCraft;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.Builder;
import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VanillaFluidCraftBuilder extends Builder<Ingredient, VanillaFluidCraftBuilder> {

    protected ItemStack output = ItemStack.EMPTY;
    protected ResourceLocation id;
    protected String group;
    protected boolean isStrict;

    // Could be practical to allow this recipe type to take in empty containers,
    // though those could easily just be defined in regular recipes. In this case, it
    // would likely help protect people from accidentally using full containers in
    // recipes where the container is consumed. Food for thought.
    private FluidStack fluid = FluidStack.empty();

    public VanillaFluidCraftBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public VanillaFluidCraftBuilder() {
        this(null);
    }

    public VanillaFluidCraftBuilder pattern(String slice) {
        return aisle(slice);
    }

    public VanillaFluidCraftBuilder define(char cha, TagKey<Item> itemStack) {
        return where(cha, Ingredient.of(itemStack));
    }

    public VanillaFluidCraftBuilder define(char cha, ItemStack itemStack) {
        if (itemStack.hasTag() || itemStack.getDamageValue() > 0) {
            return where(cha, NBTIngredient.createNBTIngredient(itemStack));
        } else {
            return where(cha, Ingredient.of(itemStack));
        }
    }

    public VanillaFluidCraftBuilder define(char cha, ItemLike itemLike) {
        return where(cha, Ingredient.of(itemLike));
    }

    public VanillaFluidCraftBuilder define(char cha, Ingredient ingredient) {
        return where(cha, ingredient);
    }

    public VanillaFluidCraftBuilder fluid(FluidStack fluid) {
        this.fluid = fluid.copy();
        return this;
    }

    public VanillaFluidCraftBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return (VanillaFluidCraftBuilder) this;
    }

    public VanillaFluidCraftBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return (VanillaFluidCraftBuilder) this;
    }

    public VanillaFluidCraftBuilder output(ItemStack itemStack, int count, CompoundTag nbt) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        this.output.setTag(nbt);
        return this;
    }

    public VanillaFluidCraftBuilder id(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public VanillaFluidCraftBuilder id(String id) {
        this.id = new ResourceLocation(id);
        return this;
    }

    public VanillaFluidCraftBuilder group(String group) {
        this.group = group;
        return this;
    }

    public VanillaFluidCraftBuilder isStrict(boolean isStrict) {
        this.isStrict = isStrict;
        return this;
    }

    @Override
    public VanillaFluidCraftBuilder shallowCopy() {
        var builder = super.shallowCopy();
        builder.output = output.copy();
        return builder;
    }

    public void toJson(JsonObject json) {
        if (group != null) {
            json.addProperty("group", group);
        }

        if (!shape.isEmpty()) {
            JsonArray pattern = new JsonArray();
            for (String[] strings : shape) {
                for (String string : strings) {
                    pattern.add(string);
                }
            }
            json.add("pattern", pattern);
        }

        if (!symbolMap.isEmpty()) {
            JsonObject key = new JsonObject();
            symbolMap.forEach((k, v) -> key.add(k.toString(), v.toJson()));
            json.add("key", key);
        }

        if (fluid.isEmpty()) {
            GTCEu.LOGGER.error("Fluid craft recipe cannot have no fluids.");
            throw new IllegalArgumentException(id + ": Fluid not defined");
        } else {
            JsonObject fluidJson = new JsonObject();
            fluidJson.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            fluidJson.addProperty("amount", fluid.getAmount());
            json.add("fluid_stack", fluidJson);
        }

        if (output.isEmpty()) {
            GTCEu.LOGGER.error("shaped recipe {} output is empty", id);
            throw new IllegalArgumentException(id + ": output items is empty");
        } else {
            JsonObject result = new JsonObject();
            result.addProperty("item", BuiltInRegistries.ITEM.getKey(output.getItem()).toString());
            if (output.getCount() > 1) {
                result.addProperty("count", output.getCount());
            }
            if (output.hasTag() && output.getTag() != null) {
                result.add("nbt", NBTToJsonConverter.getObject(output.getTag()));
            }
            json.add("result", result);
        }
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new FinishedRecipe() {

            @Override
            public void serializeRecipeData(JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public ResourceLocation getId() {
                var ID = id == null ? defaultId() : id;
                return new ResourceLocation(ID.getNamespace(), "fluid_craft" + "/" + ID.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return VanillaFluidCraft.SERIALIZER;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}

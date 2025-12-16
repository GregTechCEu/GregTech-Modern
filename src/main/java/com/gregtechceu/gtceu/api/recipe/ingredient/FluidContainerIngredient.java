package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.VoidFluidHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class FluidContainerIngredient extends Ingredient {

    public static final ResourceLocation TYPE = GTCEu.id("fluid_container");

    public static final Codec<FluidContainerIngredient> CODEC = FluidIngredient.CODEC.xmap(
            FluidContainerIngredient::new, FluidContainerIngredient::getFluid);

    @Getter
    private final FluidIngredient fluid;

    public FluidContainerIngredient(FluidIngredient fluid) {
        super(Stream.empty());
        this.fluid = fluid;
    }

    public FluidContainerIngredient(FluidStack fluidStack) {
        this(FluidIngredient.of(TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).getPath()),
                fluidStack.getAmount(), fluidStack.getTag()));
    }

    public FluidContainerIngredient(TagKey<Fluid> tag, int amount) {
        this(FluidIngredient.of(tag, amount, null));
    }

    private ItemStack[] cachedStacks;

    @Nonnull
    @Override
    public ItemStack[] getItems() {
        if (cachedStacks == null)
            cachedStacks = Arrays.stream(this.fluid.getStacks())
                    .map(FluidUtil::getFilledBucket)
                    .filter(s -> !s.isEmpty())
                    .toArray(ItemStack[]::new);
        return this.cachedStacks;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.add("fluid", fluid.toJson());
        return json;
    }

    @Override
    public boolean isEmpty() {
        return this.fluid.isEmpty();
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        return FluidUtil.getFluidContained(stack).map((s) -> fluid.test(s) && s.getAmount() >= fluid.getAmount())
                .orElse(false) &&
                FluidUtil.tryEmptyContainer(stack, VoidFluidHandler.INSTANCE, fluid.getAmount(), null, false)
                        .isSuccess();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public ItemStack getExtractedStack(ItemStack input) {
        FluidActionResult result = FluidUtil.tryEmptyContainer(input, VoidFluidHandler.INSTANCE, fluid.getAmount(),
                ForgeHooks.getCraftingPlayer(), true);
        if (result.isSuccess()) {
            return result.getResult();
        }
        return input;
    }

    @Override
    @NotNull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static FluidContainerIngredient fromJson(JsonObject json) {
        return SERIALIZER.parse(json);
    }

    public static final IIngredientSerializer<FluidContainerIngredient> SERIALIZER = new IIngredientSerializer<>() {

        @Override
        public @NotNull FluidContainerIngredient parse(FriendlyByteBuf buffer) {
            FluidIngredient fluid = FluidIngredient.fromNetwork(buffer);
            return new FluidContainerIngredient(fluid);
        }

        @Override
        public @NotNull FluidContainerIngredient parse(JsonObject json) {
            FluidIngredient fluid = FluidIngredient.fromJson(GsonHelper.getAsJsonObject(json, "fluid"));
            return new FluidContainerIngredient(fluid);
        }

        @Override
        public void write(FriendlyByteBuf buffer, FluidContainerIngredient ingredient) {
            ingredient.fluid.toNetwork(buffer);
        }
    };
}

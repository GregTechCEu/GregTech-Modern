package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.utils.InfiniteFluidTransfer;

import com.lowdragmc.lowdraglib.side.fluid.*;

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
                fluidStack.getAmount()));
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
                    .map(stack -> stack.getFluid().getBucket().getDefaultInstance())
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
        IFluidTransfer transfer = FluidTransferHelper.getFluidTransfer(stack);
        return transfer != null && this.extractFrom(transfer, true);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public ItemStack getExtractedStack(ItemStack input) {
        FluidActionResult result = FluidTransferHelper.tryEmptyContainer(input,
                new InfiniteFluidTransfer(1),
                (int) this.fluid.getAmount(),
                ForgeHooks.getCraftingPlayer(),
                true);
        if (result.success) {
            return result.result;
        }
        return input;
    }

    public boolean extractFrom(IFluidTransfer handler, boolean simulate) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack inTank = handler.getFluidInTank(tank);
            if (fluid.test(inTank)) {
                FluidStack toExtract = inTank.copy(fluid.getAmount());
                FluidStack extractedSim = handler.drain(toExtract, true);
                if (extractedSim.getAmount() >= fluid.getAmount()) {
                    if (!simulate)
                        handler.drain(toExtract, false);
                    return true;
                }
            }
        }
        return false;
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

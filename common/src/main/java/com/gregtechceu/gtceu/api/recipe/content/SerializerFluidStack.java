package com.gregtechceu.gtceu.api.recipe.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class SerializerFluidStack implements IContentSerializer<FluidStack> {

    public static SerializerFluidStack INSTANCE = new SerializerFluidStack();

    private SerializerFluidStack() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, FluidStack content) {
        content.writeToBuf(buf);
    }

    @Override
    public FluidStack fromNetwork(FriendlyByteBuf buf) {
        return FluidStack.readFromBuf(buf);
    }

    @Override
    public FluidStack fromJson(JsonElement json) {
        try {
            if (!json.isJsonObject()) {
                return FluidStack.loadFromTag(TagParser.parseTag(json.getAsString()));
            }
            var jObj = json.getAsJsonObject();
            var fluid = new ResourceLocation(jObj.get("fluid").getAsString());
            var amount = jObj.get("amount").getAsLong() * FluidHelper.getBucket() / 1000;
            var fluidStack = FluidStack.create(Objects.requireNonNull(Registry.FLUID.get(fluid)), amount);
            if (jObj.has("nbt")) {
                try {
                    fluidStack.setTag(TagParser.parseTag(jObj.get("nbt").getAsString()));
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
            return fluidStack;
        } catch (Exception e) {
            GTCEu.LOGGER.error("cant parse the fluid ingredient: {}", json.toString());
            return FluidStack.empty();
        }
    }

    @Override
    public JsonElement toJson(FluidStack content) {
        var json = new JsonObject();
        json.addProperty("fluid", Objects.requireNonNull(Registry.FLUID.getKey(content.getFluid())).toString());
        // TODO Fabric and Forge have their own magic number
        json.addProperty("amount", content.getAmount() * 1000 / FluidHelper.getBucket());
        if (content.hasTag())
            json.addProperty("nbt", content.getTag().toString());
        return json;
    }

    @Override
    public FluidStack of(Object o) {
        if (o instanceof FluidStack) {
            return ((FluidStack) o).copy();
        }
        return FluidStack.empty();
    }
}

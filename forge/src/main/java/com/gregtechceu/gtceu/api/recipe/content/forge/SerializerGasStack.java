package com.gregtechceu.gtceu.api.recipe.content.forge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class SerializerGasStack implements IContentSerializer<GasStack> {

    public static SerializerGasStack INSTANCE = new SerializerGasStack();

    private SerializerGasStack() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, GasStack content) {
        content.writeToPacket(buf);
    }

    @Override
    public GasStack fromNetwork(FriendlyByteBuf buf) {
        return GasStack.readFromPacket(buf);
    }

    @Override
    public GasStack fromJson(JsonElement json) {
        try {
            if (!json.isJsonObject()) {
                return GasStack.readFromNBT(TagParser.parseTag(json.getAsString()));
            }
            var jObj = json.getAsJsonObject();
            var gas = new ResourceLocation(jObj.get("gas").getAsString());
            var amount = jObj.get("amount").getAsLong();
            var fluidStack = new GasStack(Objects.requireNonNull(Gas.getFromRegistry(gas)), amount);
            return fluidStack;
        } catch (Exception e) {
            GTCEu.LOGGER.error("cant parse the fluid ingredient: {}", json.toString());
            return GasStack.EMPTY;
        }
    }

    @Override
    public JsonElement toJson(GasStack content) {
        var json = new JsonObject();
        json.addProperty("gas", Objects.requireNonNull(content.getType().getRegistryName()).toString());
        json.addProperty("amount", content.getAmount() * FluidHelper.getBucket() / 1000);
        return json;
    }

    @Override
    public GasStack of(Object o) {
        if (o instanceof GasStack) {
            return ((GasStack) o).copy();
        }
        return GasStack.EMPTY;
    }

    @Override
    public GasStack defaultValue() {
        return GasStack.EMPTY;
    }
}
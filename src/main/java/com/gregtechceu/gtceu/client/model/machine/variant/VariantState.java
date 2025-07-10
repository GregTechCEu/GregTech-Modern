package com.gregtechceu.gtceu.client.model.machine.variant;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.util.VariantRotationHelpers;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.Objects;

public class VariantState implements ModelState {

    @Getter
    private final Either<ResourceLocation, UnbakedModel> model;
    @Getter
    private final Transformation rotation;
    @Getter
    private final boolean uvLocked;
    @Getter
    private final int weight;
    @Getter
    @Setter
    private UnbakedModel resolvedModel;

    public VariantState(Either<ResourceLocation, UnbakedModel> model,
                        Transformation rotation, boolean uvLocked, int weight) {
        this.model = model;
        this.rotation = rotation;
        this.uvLocked = uvLocked;
        this.weight = weight;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof VariantState variantState)) {
            return false;
        } else {
            return this.model.equals(variantState.model) &&
                    Objects.equals(this.rotation, variantState.rotation) &&
                    this.uvLocked == variantState.uvLocked &&
                    this.weight == variantState.weight;
        }
    }

    public int hashCode() {
        int i = this.model.hashCode();
        i = 31 * i + this.rotation.hashCode();
        i = 31 * i + Boolean.valueOf(this.uvLocked).hashCode();
        return 31 * i + this.weight;
    }

    public static class Deserializer implements JsonDeserializer<VariantState> {

        public VariantState deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                                                                                                         throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            var model = MachineModelLoader.parseVariant(obj.get("model"), context);
            var rot = this.getBlockRotation(obj);
            boolean isUvLock = GsonHelper.getAsBoolean(obj, "uvlock", false);
            int weight = this.getWeight(obj);
            return new VariantState(model, rot, isUvLock, weight);
        }

        protected Transformation getBlockRotation(JsonObject json) {
            int x = GsonHelper.getAsInt(json, "x", 0);
            int y = GsonHelper.getAsInt(json, "y", 0);
            int z = GsonHelper.getAsInt(json, GTBlockstateProvider.Z_ROT_PROPERTY_NAME, 0);
            Transformation rotation = VariantRotationHelpers.getRotationTransform(x, y, z);
            if (rotation != null) return rotation;
            else throw new JsonParseException("Invalid ExtendedBlockModelRotation x: " + x + ", y: " + y + ", z: " + z);
        }

        protected int getWeight(JsonObject json) {
            int i = GsonHelper.getAsInt(json, "weight", 1);
            if (i < 1) {
                throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
            } else {
                return i;
            }
        }
    }
}

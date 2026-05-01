package com.gregtechceu.gtceu.client.model.compat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;

import java.lang.reflect.Type;

public class ItemTransform {

    public static final ItemTransform NO_TRANSFORM = new ItemTransform(
            net.minecraft.client.resources.model.cuboid.ItemTransform.NO_TRANSFORM);

    private final net.minecraft.client.resources.model.cuboid.ItemTransform delegate;

    public ItemTransform(net.minecraft.client.resources.model.cuboid.ItemTransform delegate) {
        this.delegate = delegate;
    }

    public net.minecraft.client.resources.model.cuboid.ItemTransform unwrap() {
        return delegate;
    }

    public void apply(boolean leftHand, PoseStack.Pose pose) {
        delegate.apply(leftHand, pose);
    }

    public static class Deserializer implements JsonDeserializer<ItemTransform> {

        private final net.minecraft.client.resources.model.cuboid.ItemTransform.Deserializer delegate = new net.minecraft.client.resources.model.cuboid.ItemTransform.Deserializer();

        @Override
        public ItemTransform deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                                                                                             throws JsonParseException {
            return new ItemTransform(delegate.deserialize(json, typeOfT, context));
        }
    }
}

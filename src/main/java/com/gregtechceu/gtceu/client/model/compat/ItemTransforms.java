package com.gregtechceu.gtceu.client.model.compat;

import net.minecraft.world.item.ItemDisplayContext;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class ItemTransforms {

    public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms(
            net.minecraft.client.resources.model.cuboid.ItemTransforms.NO_TRANSFORMS);
    public static final ItemTransforms BLOCK = createBlockTransforms();

    private final net.minecraft.client.resources.model.cuboid.ItemTransforms delegate;

    public ItemTransforms(net.minecraft.client.resources.model.cuboid.ItemTransforms delegate) {
        this.delegate = delegate;
    }

    public net.minecraft.client.resources.model.cuboid.ItemTransforms unwrap() {
        return delegate;
    }

    public ItemTransform getTransform(ItemDisplayContext context) {
        return new ItemTransform(delegate.getTransform(context));
    }

    private static ItemTransforms createBlockTransforms() {
        var gui = makeTransform(30.0f, 225.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.625f, 0.625f, 0.625f);
        var ground = makeTransform(0.0f, 0.0f, 0.0f, 0.0f, 3.0f, 0.0f, 0.25f, 0.25f, 0.25f);
        var fixed = makeTransform(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f);
        var thirdPersonRight = makeTransform(75.0f, 45.0f, 0.0f, 0.0f, 2.5f, 0.0f, 0.375f, 0.375f, 0.375f);
        var firstPersonRight = makeTransform(0.0f, 45.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.4f, 0.4f);
        var firstPersonLeft = makeTransform(0.0f, 225.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.4f, 0.4f);
        var none = net.minecraft.client.resources.model.cuboid.ItemTransform.NO_TRANSFORM;
        return new ItemTransforms(new net.minecraft.client.resources.model.cuboid.ItemTransforms(
                thirdPersonRight, thirdPersonRight, firstPersonLeft, firstPersonRight, none, gui, ground, fixed, none));
    }

    private static net.minecraft.client.resources.model.cuboid.ItemTransform makeTransform(float rotationX,
                                                                                           float rotationY,
                                                                                           float rotationZ,
                                                                                           float translationX,
                                                                                           float translationY,
                                                                                           float translationZ,
                                                                                           float scaleX,
                                                                                           float scaleY,
                                                                                           float scaleZ) {
        Vector3f translation = new Vector3f(translationX, translationY, translationZ).mul(0.0625f);
        return new net.minecraft.client.resources.model.cuboid.ItemTransform(
                new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
    }

    public static class Deserializer implements JsonDeserializer<ItemTransforms> {

        private final net.minecraft.client.resources.model.cuboid.ItemTransforms.Deserializer delegate = new net.minecraft.client.resources.model.cuboid.ItemTransforms.Deserializer();

        @Override
        public ItemTransforms deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                                                                                              throws JsonParseException {
            return new ItemTransforms(delegate.deserialize(json, typeOfT, context));
        }
    }
}

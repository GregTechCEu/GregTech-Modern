package net.minecraft.client.renderer.block.model;

import net.minecraft.world.item.ItemDisplayContext;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ItemTransforms {

    public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms(
            net.minecraft.client.resources.model.cuboid.ItemTransforms.NO_TRANSFORMS);

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

    public static class Deserializer implements JsonDeserializer<ItemTransforms> {

        private final net.minecraft.client.resources.model.cuboid.ItemTransforms.Deserializer delegate = new net.minecraft.client.resources.model.cuboid.ItemTransforms.Deserializer();

        @Override
        public ItemTransforms deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                                                                                              throws JsonParseException {
            return new ItemTransforms(delegate.deserialize(json, typeOfT, context));
        }
    }
}

package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.resources.Identifier;

import com.google.gson.JsonObject;

public class CustomLoaderBuilder<T extends ModelBuilder<T>> {

    protected final Identifier loaderId;
    protected final T parent;
    protected final ExistingFileHelper existingFileHelper;
    protected final boolean allowInlineElements;

    public CustomLoaderBuilder(Identifier loaderId, T parent, ExistingFileHelper existingFileHelper,
                               boolean allowInlineElements) {
        this.loaderId = loaderId;
        this.parent = parent;
        this.existingFileHelper = existingFileHelper;
        this.allowInlineElements = allowInlineElements;
    }

    public T end() {
        return parent;
    }

    public JsonObject toJson(JsonObject json) {
        json.addProperty("loader", loaderId.toString());
        return json;
    }
}

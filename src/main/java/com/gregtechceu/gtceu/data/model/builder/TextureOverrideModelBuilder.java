package com.gregtechceu.gtceu.data.model.builder;

import com.gregtechceu.gtceu.client.model.TextureOverrideModel;

import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public class TextureOverrideModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    // spotless:off
    public static <T extends ModelBuilder<T>> TextureOverrideModelBuilder<T> begin(T parent, ExistingFileHelper efh) {
        return new TextureOverrideModelBuilder<>(parent, efh);
    }
    // spotless:on

    @Setter
    private ModelFile childModel;

    protected TextureOverrideModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(TextureOverrideModel.Loader.ID, parent, existingFileHelper);
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        json.add("child", modelToJson(this.childModel));
        return json;
    }

    public static JsonElement modelToJson(ModelFile model) {
        if (model instanceof ModelBuilder<?> builder) {
            return builder.toJson();
        } else {
            return new JsonPrimitive(model.getLocation().toString());
        }
    }
}

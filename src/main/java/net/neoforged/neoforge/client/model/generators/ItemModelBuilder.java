package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.resources.Identifier;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {

    protected final List<OverrideBuilder> overrides = new ArrayList<>();

    public ItemModelBuilder(Identifier outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }

    public OverrideBuilder override() {
        OverrideBuilder ret = new OverrideBuilder();
        overrides.add(ret);
        return ret;
    }

    public OverrideBuilder override(int index) {
        return overrides.get(index);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        if (!overrides.isEmpty()) {
            JsonArray json = new JsonArray();
            overrides.stream().map(OverrideBuilder::toJson).forEach(json::add);
            root.add("overrides", json);
        }
        return root;
    }

    public class OverrideBuilder {

        private ModelFile model;
        private final Map<Identifier, Float> predicates = new LinkedHashMap<>();

        public OverrideBuilder model(ModelFile model) {
            this.model = Preconditions.checkNotNull(model, "Model must not be null");
            model.assertExistence();
            return this;
        }

        public OverrideBuilder predicate(Identifier key, float value) {
            predicates.put(key, value);
            return this;
        }

        public ItemModelBuilder end() {
            return ItemModelBuilder.this;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            JsonObject predicateJson = new JsonObject();
            predicates.forEach((key, value) -> predicateJson.addProperty(key.toString(), value));
            json.add("predicate", predicateJson);
            json.addProperty("model", model.getLocation().toString());
            return json;
        }
    }
}

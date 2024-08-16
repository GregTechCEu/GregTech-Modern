package com.gregtechceu.gtceu.integration.jsonthings.parsers;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;

import dev.gigaherz.jsonthings.util.parse.value.Any;
import org.apache.commons.lang3.mutable.MutableObject;

public class MaterialStackParser {

    public static MaterialStackWrapper of(Any any) {
        var ref = new MutableObject<MaterialStackWrapper>();
        any.ifString(stringValue -> {
            ref.setValue(MaterialStackWrapper.fromString(stringValue.getAsString()));
        }).ifObj(objValue -> {
            objValue.key("material", matId -> {
                String id = matId.string().getAsString();
                long amount = objValue.hasKey("amount") ? objValue.getAsJsonObject().get("amount").getAsLong() : 1;
                ref.setValue(new MaterialStackWrapper(() -> GTMaterials.get(id), amount));
            });
        });
        return ref.getValue();
    }
}

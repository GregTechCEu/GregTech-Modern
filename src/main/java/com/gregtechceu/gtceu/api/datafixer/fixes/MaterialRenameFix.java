package com.gregtechceu.gtceu.api.datafixer.fixes;

import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;

import java.util.Objects;
import java.util.function.Function;

import static com.mojang.datafixers.DSL.*;
import static net.minecraft.util.datafix.schemas.NamespacedSchema.*;

public abstract class MaterialRenameFix extends DataFix {

    private final String name;

    public MaterialRenameFix(Schema outputSchema, String name) {
        super(outputSchema, false);
        this.name = name;
    }

    public TypeRewriteRule makeRule() {
        Type<Pair<String, String>> type = named(GTReferences.MATERIAL_NAME.typeName(), namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(GTReferences.MATERIAL_NAME), type)) {
            throw new IllegalStateException("material name type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, type, ops -> value -> value.mapSecond(this::fixMaterial));
        }
    }

    protected abstract String fixMaterial(String item);

    public static DataFix create(Schema outputSchema, String name, final Function<String, String> fixer) {
        return new MaterialRenameFix(outputSchema, name) {
            protected String fixMaterial(String name) {
                return fixer.apply(name);
            }
        };
    }
}

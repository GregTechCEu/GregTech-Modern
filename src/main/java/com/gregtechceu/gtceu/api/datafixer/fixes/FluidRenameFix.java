package com.gregtechceu.gtceu.api.datafixer.fixes;

import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;

import java.util.Objects;
import java.util.function.Function;

public abstract class FluidRenameFix extends DataFix {

    private final String name;

    public FluidRenameFix(Schema outputSchema, String name) {
        super(outputSchema, false);
        this.name = name;
    }

    public TypeRewriteRule makeRule() {
        Type<Pair<String, String>> type = DSL.named(GTReferences.FLUID_NAME.typeName(),
                NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(GTReferences.FLUID_NAME), type)) {
            throw new IllegalStateException("item name type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, type, (ops) -> (typeAndId) -> typeAndId.mapSecond(this::fixFluid));
        }
    }

    protected abstract String fixFluid(String fluid);

    public static DataFix create(Schema outputSchema, String name, final Function<String, String> fixer) {
        return new FluidRenameFix(outputSchema, name) {

            protected String fixFluid(String fluid) {
                return fixer.apply(fluid);
            }
        };
    }
}

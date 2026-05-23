package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.util.datafix.fixes.References;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

import java.util.Map;

public class AutoOutputTraitFix extends DataFix {

    private static final Map<String, String> FIELD_RENAMES = Map.of(
            "outputFacingItems", "itemOutputDirection",
            "outputFacingFluids", "fluidOutputDirection",
            "autoOutputItems", "autoOutputItems",
            "autoOutputFluids", "autoOutputFluids",
            "allowInputFromOutputSideItems", "allowItemInputFromOutputSide",
            "allowInputFromOutputSideFluids", "allowFluidInputFromOutputSide"
    );

    public AutoOutputTraitFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("AutoOutputTraitFix", this.getInputSchema().getType(References.BLOCK_ENTITY), typed -> {
            return typed.update(DSL.remainderFinder(), remainder -> {
                Dynamic<?> traitValue = remainder.emptyMap();
                for (var entry : FIELD_RENAMES.entrySet()) {
                    String oldName = entry.getKey();
                    String newName = entry.getValue();

                    var oldField = remainder.get(oldName).result();
                    if (oldField.isPresent()) {
                        traitValue = traitValue.set(newName, oldField.get());
                        remainder = remainder.remove(oldName);
                    }
                }

                remainder.set("autoOutput", traitValue);

                return remainder;
            });
        });
    }
}

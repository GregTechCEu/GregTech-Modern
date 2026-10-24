package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.util.datafix.fixes.References;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;

import java.util.function.Function;

public class TraitHolderificationFix extends DataFix {

    public TraitHolderificationFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityType = this.getOutputSchema().getType(References.BLOCK_ENTITY);

        return this.fixTypeEverywhereTyped("TraitHolderificationFix", blockEntityType, typed -> {
            // move the auto output trait first
            typed = typed.update(DSL.remainderFinder(), dynamic -> {
                final var field = dynamic.get("autoOutput").result();
                if (field.isPresent()) {
                    dynamic = dynamic.update("traitHolder", val -> val.set("autoOutput", field.get()));
                }
                return dynamic;
            });

            // then rename the original fields
            typed = moveItemHandlerField(typed, "chargerInventory", "batterySlot");
            typed = moveItemHandlerField(typed, "circuitInventory", "circuitSlot");

            // then copy the renamed fields into the `traitHolder` field
            typed = copyFieldToTraitHolder(typed, "batterySlot");
            typed = copyFieldToTraitHolder(typed, "circuitSlot", "circuit");
            typed = copyFieldToTraitHolder(typed, "circuitSlot"); // do circuit slot twice with different names

            return typed;
        });
    }

    private static Typed<?> moveItemHandlerField(Typed<?> typed, String oldName, String newName) {
        var field = typed.getType().findFieldTypeOpt(oldName);
        if (field.isEmpty()) return typed;

        return moveItemHandlerFieldHelper(typed, oldName, newName, field.get())
                .update(DSL.remainderFinder(), dynamic -> dynamic.remove(oldName));
    }

    private static <A> Typed<?> moveItemHandlerFieldHelper(Typed<?> typed, String oldName, String newName,
                                                           Type<A> fieldType) {
        var oldField = DSL.optional(DSL.field(oldName, fieldType));
        var newField = DSL.optional(DSL.field(newName, DSL.field("storage", fieldType)));

        return typed.update(oldField.finder(), newField, Function.identity());
    }

    private static Typed<?> copyFieldToTraitHolder(Typed<?> typed, String name) {
        return copyFieldToTraitHolder(typed, name, name);
    }

    private static Typed<?> copyFieldToTraitHolder(Typed<?> typed, String oldName, String newName) {
        var field = typed.getType().findFieldTypeOpt(oldName);
        if (field.isEmpty()) return typed;
        return copyFieldToTraitHolderHelper(typed, oldName, newName, field.get());
    }

    private static <A> Typed<?> copyFieldToTraitHolderHelper(Typed<?> typed, String oldName, String newName,
                                                             Type<A> fieldType) {
        var type = DSL.optional(DSL.field(oldName, fieldType));
        var type1 = DSL.optional(DSL.field("traitHolder", DSL.optional(DSL.field(newName, fieldType))));

        return typed.update(type.finder(), type1, field -> Either.left(field));
    }
}

package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import net.minecraft.util.datafix.fixes.References;

import java.util.function.Function;

import static com.mojang.datafixers.DSL.*;

public class TraitHolderificationFix extends DataFix {

    public TraitHolderificationFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityIn = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> blockEntityOut = this.getOutputSchema().getType(References.BLOCK_ENTITY);

        return this.fixTypeEverywhereTyped("TraitHolderDataFix", blockEntityIn, blockEntityOut, typed -> {
            // move the auto output trait first
            typed = typed.update(remainderFinder(), dynamic -> {
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
        return moveItemHandlerFieldHelper(typed, oldName, newName, typed.getType().findFieldType(oldName))
                .update(DSL.remainderFinder(), dynamic -> dynamic.remove(oldName));
    }

    private static <A> Typed<?> moveItemHandlerFieldHelper(Typed<?> typed, String oldName, String newName,
                                                           Type<A> fieldType) {
        Type<Either<A, Unit>> type = DSL.optional(DSL.field(oldName, fieldType));
        Type<Either<A, Unit>> type1 = optional(field(newName, field("storage", fieldType)));

        return typed.update(type.finder(), type1, Function.identity());
    }

    private static Typed<?> copyFieldToTraitHolder(Typed<?> typed, String name) {
        return copyFieldToTraitHolder(typed, name, name);
    }

    private static Typed<?> copyFieldToTraitHolder(Typed<?> typed, String oldName, String newName) {
        return copyFieldToTraitHolderHelper(typed, oldName, newName, typed.getType().findFieldType(oldName));
    }

    private static <A> Typed<?> copyFieldToTraitHolderHelper(Typed<?> typed, String oldName, String newName,
                                                             Type<A> fieldType) {
        Type<Either<A, Unit>> type = DSL.optional(DSL.field(oldName, fieldType));
        Type<Either<Either<A, Unit>, Unit>> type1 = optional(field("traitHolder", optional(field(newName, fieldType))));

        return typed.update(type.finder(), type1, field -> Either.left(field));
    }
}

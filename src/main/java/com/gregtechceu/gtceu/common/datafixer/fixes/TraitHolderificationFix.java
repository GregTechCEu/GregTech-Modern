package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

import static com.mojang.datafixers.DSL.remainderFinder;

public class TraitHolderificationFix extends DataFix {

    public TraitHolderificationFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityIn = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> blockEntityOut = this.getOutputSchema().getType(References.BLOCK_ENTITY);

        OpticFinder<?> batterySlotFinder = blockEntityIn.findField("tag");

        // spotless:off
        return TypeRewriteRule.seq(
                this.fixTypeEverywhereTyped("TraitHolderDataFix - autoOutput", blockEntityIn, blockEntityOut, typed -> typed.update(remainderFinder(), TraitHolderificationFix::copyAutoOutputToTraitHolder)),
                this.fixTypeEverywhereTyped("TraitHolderDataFix - batterySlot", blockEntityIn, blockEntityOut, typed -> typed.update(remainderFinder(), TraitHolderificationFix::fixBatteryAndCopyToTraitHolder)),
                this.fixTypeEverywhereTyped("TraitHolderDataFix - circuitSlot", blockEntityIn, blockEntityOut, typed -> typed.update(remainderFinder(), TraitHolderificationFix::fixCircuitAndCopyToTraitHolder))
        );
        // spotless:on
    }

    private static Dynamic<?> addTraitHolderField(Dynamic<?> dynamic) {
        var traitHolder = dynamic.get("traitHolder").result();
        if (traitHolder.isEmpty()) {
            dynamic = dynamic.set("traitHolder", dynamic.emptyMap());
        }
        return dynamic;
    }

    private static Dynamic<?> copyAutoOutputToTraitHolder(Dynamic<?> dynamic) {
        dynamic = addTraitHolderField(dynamic);

        final var field = dynamic.get("autoOutput").result();
        if (field.isPresent()) {
            dynamic = dynamic.update("traitHolder", val -> val.set("autoOutput", field.get()));
        }
        return dynamic;
    }

    private static Dynamic<?> fixBatteryAndCopyToTraitHolder(Dynamic<?> dynamic) {
        dynamic = addTraitHolderField(dynamic);

        final var field = dynamic.get("chargerInventory").result();
        if (field.isPresent()) {
            Dynamic<?> batterySlot = dynamic.emptyMap()
                    .set("storage", field.get());
            // move the original battery slot field to the trait's object
            dynamic = dynamic.set("batterySlot", batterySlot)
                    // and also copy it to the trait holder
                    .update("traitHolder", val -> val.set("batterySlot", batterySlot));
        }
        return dynamic;
    }

    private static Dynamic<?> fixCircuitAndCopyToTraitHolder(Dynamic<?> dynamic) {
        dynamic = addTraitHolderField(dynamic);

        final var field = dynamic.get("circuitInventory").result();
        if (field.isPresent()) {
            Dynamic<?> circuitSlot = dynamic.emptyMap()
                    .set("storage", field.get());
            // move the original battery slot field to the trait's object
            dynamic = dynamic.set("circuitSlot", circuitSlot)
                    // and also copy it to the trait holder
                    // ...twice, to cover both the 'original' name and what SimpleTieredMachine uses
                    .update("traitHolder", val -> val.set("circuitSlot", circuitSlot)
                            .set("circuit", circuitSlot));
        }
        return dynamic;
    }
}

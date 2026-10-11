package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Objects;
import java.util.Optional;

public class ItemStackTurbineRotorFix extends DataFix {

    private static final String OLD_TURBINE_ROTOR_ITEM_ID = "gtceu:turbine_rotor";

    public ItemStackTurbineRotorFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> itemIdFinder = DSL.fieldFinder("id",
                DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> tagFinder = type.findField("tag");
        OpticFinder<?> partStatsFinder = tagFinder.type().findField("GT.PartStats");
        OpticFinder<String> materialFinder = DSL.fieldFinder("Material", NamespacedSchema.namespacedString());

        return this.fixTypeEverywhereTyped("ItemStackTurbineRotorFix", type, (typed) -> {
            Optional<Pair<String, String>> itemId = typed.getOptional(itemIdFinder);
            if (itemId.isEmpty() || !Objects.equals(itemId.get().getSecond(), OLD_TURBINE_ROTOR_ITEM_ID)) {
                return typed;
            }
            var tag = typed.getOptionalTyped(tagFinder);
            if (tag.isEmpty()) {
                // if it doesn't have a tag (registry snapshots?), make it an iron turbine rotor by default.
                return typed.set(itemIdFinder,
                        Pair.of(References.ITEM_NAME.typeName(), "gtceu:iron_turbine_rotor"));
            }
            Typed<?> partStats = tag.get().getOrCreateTyped(partStatsFinder);

            // move the Damage nbt field to the root of nbt
            var damage = partStats.getOrCreate(DSL.remainderFinder())
                    .get("Damage").result();
            if (damage.isPresent()) {
                typed = typed.update(DSL.remainderFinder(), dynamic -> {
                    return dynamic.set("Damage", damage.get());
                });
            }

            Optional<String> material = partStats.getOptional(materialFinder);
            if (material.isEmpty()) {
                // if it doesn't have a material (somehow??), make it an iron turbine rotor by default.
                return typed.set(itemIdFinder,
                        Pair.of(References.ITEM_NAME.typeName(), "gtceu:iron_turbine_rotor"));
            }

            String materialName = material.get();
            if (materialName.indexOf(':') == -1) {
                // compatibility for very old turbine rotors we might come across
                // shouldn't happen but better safe than sorry
                materialName = "gtceu:" + materialName;
            }
            // All turbine rotor items' names follow a specific pattern and are registered under the material's
            // namespace so this *should* be safe
            return typed.set(itemIdFinder,
                    Pair.of(References.ITEM_NAME.typeName(), materialName + "_turbine_rotor"));

        });
    }
}

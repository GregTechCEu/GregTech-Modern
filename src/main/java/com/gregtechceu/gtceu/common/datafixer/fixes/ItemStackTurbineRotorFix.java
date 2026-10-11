package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Objects;
import java.util.Optional;

public class ItemStackTurbineRotorFix extends DataFix {

    private static final String OLD_TURBINE_ROTOR_ITEM_ID = "gtceu:turbine_rotor";
    private static final String DEFAULT_NEW_TURBINE_ROTOR_ITEM_ID = "gtceu:iron_turbine_rotor";

    public ItemStackTurbineRotorFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    public TypeRewriteRule makeRule() {
        var type = this.getInputSchema().getType(References.ITEM_STACK);
        var itemNameType = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(References.ITEM_NAME), itemNameType)) {
            throw new IllegalStateException("item name type is not what was expected.");
        }

        var itemIdFinder = DSL.fieldFinder("id", itemNameType);
        var tagFinder = type.findField("tag");
        var partStatsFinder = tagFinder.type().findField("GT.PartStats");
        var materialFinder = DSL.fieldFinder("Material", NamespacedSchema.namespacedString());

        return TypeRewriteRule.seq(
                this.fixTypeEverywhereTyped("ItemStackTurbineRotorFix", type, (typed) -> {
                    var itemName = typed.getOptional(itemIdFinder);
                    if (itemName.isEmpty() || !Objects.equals(itemName.get().getSecond(), OLD_TURBINE_ROTOR_ITEM_ID)) {
                        return typed;
                    }
                    Typed<?> tag = typed.getOrCreateTyped(tagFinder);
                    Typed<?> partStats = tag.getOrCreateTyped(partStatsFinder);

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
                                Pair.of(References.ITEM_NAME.typeName(), DEFAULT_NEW_TURBINE_ROTOR_ITEM_ID));
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

                }),
                this.fixTypeEverywhere("Rename blank Turbine Rotor item", itemNameType,
                        ops -> itemName -> itemName.mapSecond(id -> {
                            return Objects.equals(id, OLD_TURBINE_ROTOR_ITEM_ID) ?
                                    DEFAULT_NEW_TURBINE_ROTOR_ITEM_ID : id;
                        })
                )
        );
    }
}

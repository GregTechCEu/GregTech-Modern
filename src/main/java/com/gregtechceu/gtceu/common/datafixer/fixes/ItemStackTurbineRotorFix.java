package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;

import java.util.Objects;

public class ItemStackTurbineRotorFix extends DataFix {

    private static final String OLD_TURBINE_ROTOR_ITEM_ID = "gtceu:turbine_rotor";
    private static final String DEFAULT_NEW_TURBINE_ROTOR_ITEM_ID = "gtceu:iron_turbine_rotor";

    public ItemStackTurbineRotorFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    public TypeRewriteRule makeRule() {
        Type<?> inputType = this.getInputSchema().getType(References.ITEM_STACK);
        var itemNameType = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(References.ITEM_NAME), itemNameType)) {
            throw new IllegalStateException("item name type is not what was expected.");
        }
        var materialNameType = DSL.named(GTReferences.MATERIAL_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(GTReferences.MATERIAL_NAME), materialNameType)) {
            throw new IllegalStateException("material name type is not what was expected.");
        }

        var itemIdFinder = DSL.fieldFinder("id", itemNameType);
        OpticFinder<?> tagFinder = inputType.findField("tag");
        OpticFinder<?> partStatsFinder = tagFinder.type().findField("GT.PartStats");
        var materialFinder = DSL.fieldFinder("Material", materialNameType);

        TypeRewriteRule first = this.fixTypeEverywhereTyped("ItemStackTurbineRotorFix", inputType, typed -> {
            final var itemName = typed.getOptional(itemIdFinder);
            if (itemName.isEmpty() || !Objects.equals(itemName.get().getSecond(), OLD_TURBINE_ROTOR_ITEM_ID)) {
                return typed;
            }
            Typed<?> tag = typed.getOrCreateTyped(tagFinder);
            Typed<?> partStats = tag.getOrCreateTyped(partStatsFinder);

            // move the Damage nbt field to the root nbt tag
            var damage = partStats.getOrCreate(DSL.remainderFinder()).get("Damage").result();
            if (damage.isPresent()) {
                tag = tag.update(DSL.remainderFinder(), dynamic -> {
                    dynamic = dynamic.update("GT.PartStats", d -> d.remove("Damage"));
                    return dynamic.set("Damage", damage.get());
                });
                typed = typed.set(tagFinder, tag);
            }

            var material = partStats.getOptional(materialFinder);
            if (material.isEmpty()) {
                // if it doesn't have a material (somehow??), make it an iron turbine rotor by default.
                return typed.set(itemIdFinder,
                        Pair.of(References.ITEM_NAME.typeName(), DEFAULT_NEW_TURBINE_ROTOR_ITEM_ID));
            }

            // `material` is (material_name, <material id>)
            String materialName = material.get().getSecond();
            if (materialName.indexOf(':') == -1) {
                // compatibility for very old turbine rotors we might come across
                // shouldn't happen but better safe than sorry
                materialName = "gtceu:" + materialName;
            }
            // All turbine rotor items' names follow a specific pattern and are registered under the material's
            // namespace so this *should* be safe
            typed = typed.set(itemIdFinder,
                    Pair.of(References.ITEM_NAME.typeName(), materialName + "_turbine_rotor"));
            return typed;
        });
        TypeRewriteRule second = this.writeFixAndRead("Remove GT.PartStats NBT field",
                inputType, this.getOutputSchema().getType(References.ITEM_STACK),
                dynamic -> dynamic.update("tag", tag -> tag.remove("GT.PartStats")));
        TypeRewriteRule third = this.fixTypeEverywhere("Rename blank Turbine Rotor item", itemNameType,
                ops -> itemName -> itemName.mapSecond(id -> {
                    return Objects.equals(id, OLD_TURBINE_ROTOR_ITEM_ID) ?
                            DEFAULT_NEW_TURBINE_ROTOR_ITEM_ID : id;
                }));
        return TypeRewriteRule.seq(first, second, third);
    }
}

package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class LDLibPayloadWrapperRemovalFix extends DataFix {

    public LDLibPayloadWrapperRemovalFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityIn = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> blockEntityOut = this.getOutputSchema().getType(References.BLOCK_ENTITY);
        return this.writeFixAndRead("LDLib Payload wrapper removal fix", blockEntityIn, blockEntityOut, LDLibPayloadWrapperRemovalFix::fix);
    }

    private static Dynamic<?> fix(Dynamic<?> dynamic) {
        var stream = dynamic.asStreamOpt().result();
        if (stream.isPresent()) {
            // if the entry is a list, attempt to apply the fixer to its elements
            return dynamic.createList(stream.get().map(LDLibPayloadWrapperRemovalFix::stripLDLibPayloadWrapper));
        }
        // otherwise assume it's a map and fix all children too
        // updateMapValues does nothing if the value isn't a map, so this is fine.
        return dynamic.updateMapValues(entry -> entry.mapSecond(LDLibPayloadWrapperRemovalFix::fix));
    }

    /**
     * applied to all list elements.
     *
     * <p>
     * Reading the code implies only array-like objects get the `{ "t": type, "p": value }` treatment.
     */
    public static Dynamic<?> stripLDLibPayloadWrapper(Dynamic<?> dynamic) {
        final Dynamic<?> tKey = dynamic.createString("t");
        final Dynamic<?> pKey = dynamic.createString("p");

        return DataFixUtils.orElse(dynamic.getMapValues().result()
                        .map(map -> {
                            // only allow entries with only the specific keys we expect so we don't accidentally touch other stuff
                            // tag.contains("p") && tag.contains("t")
                            if (map.size() == 2 && map.containsKey(pKey) && map.containsKey(tKey)) {
                                // return tag.get("p")
                                return map.get(pKey);
                            } else if (map.size() == 1) {
                                // I don't think this one is something that can happen, but I'm not sure about that, so
                                // it's staying.
                                //
                                // As far as I can see, the only format that's used by LDLib is the above
                                // `{ "t": type, "p": value }` one.

                                var tValue = map.get(tKey).getMapValues().result();
                                if (tValue.isPresent()) {
                                    return tValue.get().get(pKey);
                                }
                            }
                            return dynamic.createMap(map);
                        })
                        // also apply the fixer to all child objects
                        .map(LDLibPayloadWrapperRemovalFix::fix)
                , dynamic);
    }
}

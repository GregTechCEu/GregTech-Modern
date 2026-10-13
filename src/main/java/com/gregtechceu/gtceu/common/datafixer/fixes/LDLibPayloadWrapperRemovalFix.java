package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.fixes.References;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

import java.util.Optional;

public class LDLibPayloadWrapperRemovalFix extends DataFix {

    public LDLibPayloadWrapperRemovalFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityIn = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> blockEntityOut = this.getOutputSchema().getType(References.BLOCK_ENTITY);
        return this.writeFixAndRead("LDLib Payload wrapper removal fix", blockEntityIn, blockEntityOut,
                LDLibPayloadWrapperRemovalFix::fix);
    }

    private static <T> Dynamic<T> fix(Dynamic<T> dynamic) {
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
    public static <T> Dynamic<T> stripLDLibPayloadWrapper(Dynamic<T> dynamic) {
        // only allow entries with *only* the specific keys we expect so we don't accidentally touch other stuff
        var asMap = dynamic.asMapOpt().result();
        if (asMap.isEmpty() || asMap.get().count() != 2) {
            return dynamic;
        }

        Optional<Dynamic<T>> tElement = dynamic.get("t").result();
        Optional<Dynamic<T>> pElement = dynamic.get("p").result();
        // equivalent to `tag.contains("p") && tag.contains("t")`
        if (tElement.isPresent() && pElement.isPresent()) {
            return LDLibPayloadWrapperRemovalFix.fix(pElement.get());
        } else {
            return LDLibPayloadWrapperRemovalFix.fix(dynamic);
        }
    }
}

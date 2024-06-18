package com.gregtechceu.gtceu.data.datafixer;

import com.mojang.datafixers.DSL;

public class GTReferences {

    public static final DSL.TypeReference MATERIAL_NAME = reference("material_name");

    public static DSL.TypeReference reference(final String pName) {
        return new DSL.TypeReference() {

            @Override
            public String typeName() {
                return pName;
            }

            @Override
            public String toString() {
                return "@" + pName;
            }
        };
    }
}

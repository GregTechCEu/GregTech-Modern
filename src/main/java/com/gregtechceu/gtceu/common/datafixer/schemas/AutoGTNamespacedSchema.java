package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.schemas.AutomaticNamespacedSchema;
import com.mojang.datafixers.schemas.Schema;

/*package-private*/ class AutoGTNamespacedSchema extends AutomaticNamespacedSchema {

    public AutoGTNamespacedSchema(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    protected final String getAutomaticNamespace() {
        return GTCEu.MOD_ID;
    }
}

package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.syncdata.GTRecipeAccessor;
import com.gregtechceu.gtceu.syncdata.GTRecipeTypeAccessor;
import com.lowdragmc.lowdraglib.syncdata.IAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;

import static com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries.*;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote GTSyncedFieldAccessors
 */
public class GTSyncedFieldAccessors {
    public static final IAccessor GT_RECIPE_ACCESSOR = new GTRecipeAccessor();
    public static final IAccessor GT_RECIPE_TYPE_ACCESSOR = new GTRecipeTypeAccessor();

    public static void init() {
        register(FriendlyBufPayload.class, FriendlyBufPayload::new, GT_RECIPE_ACCESSOR, 1000);
        register(FriendlyBufPayload.class, FriendlyBufPayload::new, GT_RECIPE_TYPE_ACCESSOR, 1000);
    }
}

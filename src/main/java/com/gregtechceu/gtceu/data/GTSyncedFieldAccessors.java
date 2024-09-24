package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.syncdata.GTRecipePayload;
import com.gregtechceu.gtceu.syncdata.GTRecipeTypeAccessor;
import com.gregtechceu.gtceu.syncdata.MaterialPayload;

import com.lowdragmc.lowdraglib.syncdata.IAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;

import static com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries.*;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote GTSyncedFieldAccessors
 */
public class GTSyncedFieldAccessors {

    public static final IAccessor GT_RECIPE_TYPE_ACCESSOR = new GTRecipeTypeAccessor();

    public static void init() {
        register(FriendlyBufPayload.class, FriendlyBufPayload::new, GT_RECIPE_TYPE_ACCESSOR, 1000);

        registerSimple(MaterialPayload.class, MaterialPayload::new, Material.class, 1);
        registerSimple(GTRecipePayload.class, GTRecipePayload::new, GTRecipe.class, 100);
    }
}

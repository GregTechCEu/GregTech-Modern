package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.syncdata.*;

import com.lowdragmc.lowdraglib.syncdata.IAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraftforge.fluids.FluidStack;

import static com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries.*;

public class GTSyncedFieldAccessors {

    public static final IAccessor GT_RECIPE_TYPE_ACCESSOR = new GTRecipeTypeAccessor();

    public static void init() {
        register(FriendlyBufPayload.class, FriendlyBufPayload::new, GT_RECIPE_TYPE_ACCESSOR, 1000);
        register(NbtTagPayload.class, NbtTagPayload::new, VirtualTankAccessor.INSTANCE, 2);
        register(NbtTagPayload.class, NbtTagPayload::new, VirtualItemStorageAccessor.INSTANCE, 2);
        register(NbtTagPayload.class, NbtTagPayload::new, VirtualRedstoneAccessor.INSTANCE, 2);

        registerSimple(MachineRenderStatePayload.class, MachineRenderStatePayload::new, MachineRenderState.class, 1);
        registerSimple(MaterialPayload.class, MaterialPayload::new, Material.class, 1);
        registerSimple(GTRecipePayload.class, GTRecipePayload::new, GTRecipe.class, 100);
        registerSimple(FluidStackPayload.class, FluidStackPayload::new, FluidStack.class, -1);
        registerSimple(MonitorGroupPayload.class, MonitorGroupPayload::new, MonitorGroup.class, 1);
    }
}

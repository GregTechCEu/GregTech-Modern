package com.gregtechceu.gtceu.api.capability.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.Direction;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote GTCapability
 */
public class GTCapability {
    public static final BlockApiLookup<IEnergyContainer, Direction> CAPABILITY_ENERGY =
            BlockApiLookup.get(GTCEu.id("sided_energy_container"), IEnergyContainer.class, Direction.class);

    public static final BlockApiLookup<ICoverable, Direction> CAPABILITY_COVERABLE =
            BlockApiLookup.get(GTCEu.id("sided_coverable"), ICoverable.class, Direction.class);

    public static final BlockApiLookup<IToolable, Direction> CAPABILITY_TOOLABLE =
            BlockApiLookup.get(GTCEu.id("sided_toolable"), IToolable.class, Direction.class);

    public static final BlockApiLookup<IWorkable, Direction> CAPABILITY_WORKABLE =
            BlockApiLookup.get(GTCEu.id("sided_workable"), IWorkable.class, Direction.class);

    public static final BlockApiLookup<IControllable, Direction> CAPABILITY_CONTROLLABLE =
            BlockApiLookup.get(GTCEu.id("sided_controllable"), IControllable.class, Direction.class);

    public static final BlockApiLookup<RecipeLogic, Direction> CAPABILITY_RECIPE_LOGIC =
            BlockApiLookup.get(GTCEu.id("sided_recipe_logic"), RecipeLogic.class, Direction.class);

    public static final ItemApiLookup<IElectricItem, ContainerItemContext> CAPABILITY_ELECTRIC_ITEM =
            ItemApiLookup.get(GTCEu.id("sided_recipe_logic"), IElectricItem.class, ContainerItemContext.class);

    public static final BlockApiLookup<ICleanroomReceiver, Direction> CAPABILITY_CLEANROOM_RECEIVER =
            BlockApiLookup.get(GTCEu.id("cleanroom_receiver"), ICleanroomReceiver.class, Direction.class);
}

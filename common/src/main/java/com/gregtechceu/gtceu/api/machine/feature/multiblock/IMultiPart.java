package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote IMultiPart
 */
public interface IMultiPart extends IMachineFeature {

    /**
     * Can it be shared among multi multiblock.
     */
    default boolean canShared() {
        return true;
    }

    /**
     * Whether it belongs to...
     */
    boolean hasController(BlockPos controllerPos);

    /**
     * Whether it belongs to a formed Multiblock.
     */
    boolean isFormed();

    /**
     * Get all attached controllers
     */
    List<IMultiController> getControllers();

    /**
     * Called when it was removed from a multiblock.
     */
    void removedFromController(IMultiController controller);

    /**
     * Called when it was added to a multiblock.
     */
    void addedToController(IMultiController controller);

    /**
     * Get all available traits for recipe logic.
     */
    List<IRecipeHandlerTrait> getRecipeHandlers();
}

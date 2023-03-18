package com.lowdragmc.gtceu.api.machine.feature.multiblock;

import com.lowdragmc.gtceu.api.machine.MultiblockMachineDefinition;
import com.lowdragmc.gtceu.api.machine.feature.IMachineFeature;
import com.lowdragmc.gtceu.api.pattern.BlockPattern;
import com.lowdragmc.gtceu.api.pattern.MultiblockState;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote IControllerComponent
 */
public interface IMultiController extends IMachineFeature {

    /**
     * Check MultiBlock Pattern. Just checking pattern without any other logic.
     * @return whether it can be formed.
     */
    default boolean checkPattern() {
        BlockPattern pattern = getPattern();
        return pattern != null && pattern.checkPatternAt(getMultiblockState(), false);
    }


    /**
     * Get structure pattern.
     * You can override it to create dynamic patterns.
     */
    default BlockPattern getPattern() {
        return ((MultiblockMachineDefinition)self().getDefinition()).getPatternFactory().get();
    }

    /**
     * Whether Multiblock Formed.
     * <br>
     * NOTE: even machine is formed, it doesn't mean to workable!
     * Its parts maybe invalid due to chunk unload.
     */
    boolean isFormed();


    /**
     * Get MultiblockState. It records all structure-related information.
     */
    @Nonnull
    MultiblockState getMultiblockState();

    /**
     * Called in an async thread. It's unsafe, Don't modify anything of world but checking information.
     * It will be called per 5 tick.
     * @param periodID period Tick
     */
    void asyncCheckPattern(long periodID);

    /**
     * Called when structure is formed, have to be called after {@link #checkPattern()}. (server-side / fake scene only)
     * <br>
     * Trigger points:
     * <br>
     * 1 - Blocks in structure changed but still formed.
     * <br>
     * 2 - Literally, structure formed.
     */
    void onStructureFormed();

    /**
     * Called when structure is invalid. (server-side / fake scene only)
     * <br>
     * Trigger points:
     * <br>
     * 1 - Blocks in structure changed.
     * <br>
     * 2 - Before controller machine removed.
     */
    void onStructureInvalid();

    /**
     * Whether it has front face.
     * false means structure of all sides are available.
     */
    boolean hasFrontFacing();

    /**
     * Get all parts
     */
    List<IMultiPart> getParts();

    /**
     * Called from part, when part is invalid due to chunk unload or broken.
     */
    void onPartUnload();
}

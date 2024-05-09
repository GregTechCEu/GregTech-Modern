package com.gregtechceu.gtceu.api.machines.feature.multiblock;

import com.gregtechceu.gtceu.api.guis.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machines.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machines.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.machines.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machines.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machines.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote IMultiPart
 */
public interface IMultiPart extends IMachineFeature, IFancyUIMachine {

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

    /**
     * whether its base model can be replaced by controller when it is formed.
     */
    default boolean replacePartModelWhenFormed() {
        return true;
    }

    /**
     * get part's Appearance. same as IForgeBlock.getAppearance() / IFabricBlock.getAppearance()
     */
    @Nullable
    default BlockState getFormedAppearance(BlockState sourceState, BlockPos sourcePos, Direction side) {
        for (IMultiController controller : getControllers()) {
            var appearance = controller.getPartAppearance(this, side, sourceState, sourcePos);
            if (appearance != null) return appearance;
        }
        return null;
    }

    /**
     * Called per tick in {@link RecipeLogic#handleRecipeWorking()}
     */
    default boolean onWorking(IWorkableMultiController controller) {
        return true;
    }

    /**
     * Called per tick in {@link RecipeLogic#handleRecipeWorking()}
     */
    default boolean onWaiting(IWorkableMultiController controller) {
        return true;
    }

    /**
     * Called in {@link WorkableMultiblockMachine#setWorkingEnabled(boolean)}
     */
    default boolean onPaused(IWorkableMultiController controller) {
        return true;
    }

    /**
     * Called in {@link RecipeLogic#onRecipeFinish()} before outputs are produced
     */
    default boolean afterWorking(IWorkableMultiController controller) {
        return true;
    }

    /**
     * Called in {@link RecipeLogic#setupRecipe(GTRecipe)} ()}
     */
    default boolean beforeWorking(IWorkableMultiController controller) {
        return true;
    }

    /**
     * Override it to modify recipe on the fly e.g. applying overclock, change chance, etc
     * @param recipe recipe from detected from GTRecipeType
     * @return modified recipe.
     *         null -- this recipe is unavailable
     */
    default GTRecipe modifyRecipe(GTRecipe recipe) {
        return recipe;
    }

    /**
     * Add text to the multiblock's screen.
     * @param textList the text list to add to.
     */
    default void addMultiText(List<Component> textList) {

    }

    /**
     * Attach part's tooltips to the controller.
     */
    default void attachFancyTooltipsToController(IMultiController controller, TooltipsPanel tooltipsPanel) {}
}

package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.SortedSet;

public interface IMultiPart extends IMachineFeature, IFancyUIMachine {

    /**
     * @return If this multi part can be shared between multiple multiblocks.
     */
    default boolean canShared() {
        return true;
    }

    /**
     * If this multi part belongs to a controller at the given position
     * 
     * @param controllerPos Controller position
     * @return If this multi part belongs to a controller at the given position
     */
    boolean hasController(BlockPos controllerPos);

    /**
     * @return If this multi part belongs to a formed multiblock.
     */
    boolean isFormed();

    /**
     * Gets all controllers this multi part belongs to
     * 
     * @return An unmodifiable set containing the controllers.
     */
    @UnmodifiableView
    SortedSet<MultiblockControllerMachine> getControllers();

    /**
     * Called when this part is removed from a multiblock.
     * 
     * @param controller The controller which this part has been removed from.
     */
    void removedFromController(MultiblockControllerMachine controller);

    /**
     * Called when this part is added to a multiblock.
     * 
     * @param controller The controller which this part has been added to
     */
    void addedToController(MultiblockControllerMachine controller);

    /**
     * Get all available traits for recipe logic.
     */
    List<RecipeHandlerList> getRecipeHandlers();

    /**
     * If this part's base model can be replaced by controller when it is formed.
     */
    default boolean replacePartModelWhenFormed() {
        return true;
    }

    /**
     * Called to get block appearance when this multi part is in a formed multiblock.
     * 
     * @see MetaMachine#getBlockAppearance(BlockState, BlockAndTintGetter, BlockPos, Direction, BlockState, BlockPos)
     */
    @Nullable
    default BlockState getFormedAppearance(BlockState sourceState, BlockPos sourcePos, Direction side) {
        for (MultiblockControllerMachine controller : getControllers()) {
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
     * Called in {@link RecipeLogic#setupRecipe(GTRecipe)}
     */
    default boolean beforeWorking(IWorkableMultiController controller) {
        return true;
    }

    /**
     * Override it to modify recipe on the fly e.g. applying overclock, change chance, etc
     * 
     * @param recipe recipe from detected from GTRecipeType
     * @return modified recipe.
     *         null -- this recipe is unavailable
     */
    default GTRecipe modifyRecipe(GTRecipe recipe) {
        return recipe;
    }

    /**
     * Add text to the multiblock's screen.
     * 
     * @param textList the text list to add to.
     */
    default void addMultiText(List<Component> textList) {}

    /**
     * Attach part's tooltips to the controller.
     */
    default void attachFancyTooltipsToController(MultiblockControllerMachine controller, TooltipsPanel tooltipsPanel) {}
}

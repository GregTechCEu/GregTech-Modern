package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeMultiblockMachine extends WorkableMultiblockMachine
                                              implements IRecipeLogicMachine {

    @Nullable
    @Getter
    @Setter
    private ICleanroomProvider cleanroom;
    @Getter
    public final RecipeLogic recipeLogic;
    @Getter
    private final GTRecipeType[] recipeTypes;
    @Getter
    @Setter
    @Persisted
    private int activeRecipeType;
    @Getter
    protected final List<RecipeHandlerList> recipeHandlerLists;
    @Getter
    @Persisted
    @DescSynced
    protected VoidingMode voidingMode = VoidingMode.VOID_NONE;

    public RecipeMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.recipeLogic = (RecipeLogic) this.workLogic;
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.recipeHandlerLists = new ArrayList<>();
    }

    @Override
    protected WorkLogic createWorkLogic(Object... args) {
        return createRecipeLogic(args);
    }

    @Override
    public final void serverRunningTick() {}

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        recipeLogic.inValid();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        recipeHandlerLists.clear();
        recipeLogic.resetLastGroup();

        for (IMultiPart part : getParts()) {
            var handlerLists = part.getRecipeHandlers();
            handlerLists.forEach(h -> traitSubscriptions.add(h.subscribe(recipeLogic::updateTickSubscription)));
            recipeHandlerLists.addAll(handlerLists);
        }

        List<IRecipeHandler<?>> list = new ArrayList<>();
        for (MachineTrait trait : getTraits()) {
            if (trait instanceof IRecipeHandler<?> handlerTrait) {
                list.add(handlerTrait);
            }
        }
        var selfHandlerList = RecipeHandlerList.of(list);
        recipeHandlerLists.add(selfHandlerList);
        traitSubscriptions.add(selfHandlerList.subscribe(recipeLogic::updateTickSubscription));
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        recipeHandlerLists.clear();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        recipeHandlerLists.clear();
        recipeLogic.resetLastGroup();
    }

    @Nullable
    @Override
    public final Component modifyRecipe(GTRecipe recipe, RecipeHandlerGroup group) {
        for (IMultiPart part : getParts()) {
            var failReason = part.modifyRecipe(recipe);
            if (failReason != null) return failReason;
        }
        for (var modifier : self().getDefinition().getRecipeModifiers()) {
            var failReason = modifier.apply(self(), group, recipe);
            if (failReason != null) return failReason;
        }
        return null;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        IRecipeLogicMachine.super.afterWorking();
    }

    @Override
    public Component beforeWorking(@Nullable GTRecipe recipe) {
        for (IMultiPart part : getParts()) {
            Component failReason = part.beforeWorking(this);
            if (failReason != null) {
                return failReason;
            }
        }
        return IRecipeLogicMachine.super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        return super.onWorking() && IRecipeLogicMachine.super.onWorking();
    }

    @Override
    public void onWaiting() {
        super.onWaiting();
        IRecipeLogicMachine.super.onWaiting();
    }

    @NotNull
    public GTRecipeType getRecipeType() {
        return recipeTypes[activeRecipeType];
    }

    @ApiStatus.Internal
    @VisibleForTesting
    public void setRecipeType(GTRecipeType newType) {
        recipeTypes[activeRecipeType] = newType;
    }

    @Override
    public void setVoidingMode(VoidingMode mode) {
        voidingMode = mode;
        recipeLogic.updateTickSubscription();
    }
}

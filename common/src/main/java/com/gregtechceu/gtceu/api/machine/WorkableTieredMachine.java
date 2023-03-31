package com.gregtechceu.gtceu.api.machine;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote WorkableTieredMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class WorkableTieredMachine extends TieredEnergyMachine implements IRecipeLogicMachine, ICleanroomReceiver, IMachineModifyDrops, IMufflableMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WorkableTieredMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted @DescSynced
    public final RecipeLogic recipeLogic;
    @Getter
    public final GTRecipeType recipeType;
    @Getter
    public final Int2LongFunction tankScalingFunction;
    @Getter @Setter
    private ICleanroomProvider cleanroom;
    @Persisted
    public final NotifiableItemStackHandler importItems;
    @Persisted
    public final NotifiableItemStackHandler exportItems;
    @Persisted
    public final NotifiableFluidTank importFluids;
    @Persisted
    public final NotifiableFluidTank exportFluids;
    @Getter
    protected final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy;
    @Persisted
    protected int overclockTier;
    protected final List<ISubscription> traitSubscriptions;
    @Persisted @DescSynced @Getter @Setter
    protected boolean isMuffled;

    public WorkableTieredMachine(IMachineBlockEntity holder, int tier, Int2LongFunction tankScalingFunction, Object... args) {
        super(holder, tier, args);
        this.overclockTier = getMaxOverclockTier();
        this.recipeType = getDefinition().getRecipeType();
        this.tankScalingFunction = tankScalingFunction;
        this.capabilitiesProxy = Tables.newCustomTable(new EnumMap<>(IO.class), HashMap::new);
        this.traitSubscriptions = new ArrayList<>();
        this.recipeLogic = createRecipeLogic(args);
        this.importItems = createImportItemHandler(args);
        this.exportItems = createExportItemHandler(args);
        this.importFluids = createImportFluidHandler(args);
        this.exportFluids = createExportFluidHandler(args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        long tierVoltage = GTValues.V[getTier()];
        if (isEnergyEmitter()) {
            return NotifiableEnergyContainer.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else {
            return new NotifiableEnergyContainer(this, tierVoltage * 64L, tierVoltage, 2, 0L, 0L) {
                @Override
                public long getInputAmperage() {
                    if (getEnergyCapacity() / 2 > getEnergyStored() && recipeLogic.isActive()) {
                        return 2;
                    }
                    return 1;
                }
            };
        }
    }

    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
    }

    protected NotifiableFluidTank createImportFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, getRecipeType().getMaxInputs(FluidRecipeCapability.CAP), this.tankScalingFunction.apply(this.getTier()), IO.IN);
    }

    protected NotifiableFluidTank createExportFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, getRecipeType().getMaxOutputs(FluidRecipeCapability.CAP), this.tankScalingFunction.apply(this.getTier()), IO.OUT);
    }

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        for (MachineTrait trait : getTraits()) {
            if (trait instanceof IRecipeHandlerTrait<?> handlerTrait) {
                if (!capabilitiesProxy.contains(handlerTrait.getHandlerIO(), handlerTrait.getCapability())) {
                    capabilitiesProxy.put(handlerTrait.getHandlerIO(), handlerTrait.getCapability(), new ArrayList<>());
                }
                capabilitiesProxy.get(handlerTrait.getHandlerIO(), handlerTrait.getCapability()).add(handlerTrait);
                traitSubscriptions.add(handlerTrait.addChangedListener(recipeLogic::updateTickSubscription));
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        recipeLogic.inValid();
    }

    //////////////////////////////////////
    //**********     MISC    ***********//
    //////////////////////////////////////

    @Override
    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, importItems.storage);
        clearInventory(drops, exportItems.storage);
    }

    //////////////////////////////////////
    //******     RECIPE LOGIC    *******//
    //////////////////////////////////////

    public int getMaxOverclockTier() {
        return GTUtil.getTierByVoltage(Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage()));
    }

    @Override
    @Nullable
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (RecipeHelper.getRecipeEUtTier(recipe) > getTier()) {
            return null;
        }
        return RecipeHelper.applyOverclock(getDefinition().getOverclockingLogic(), recipe, Math.min(GTValues.V[overclockTier], Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage())));
    }

    @Override
    public boolean keepSubscribing() {
        return false;
    }
}

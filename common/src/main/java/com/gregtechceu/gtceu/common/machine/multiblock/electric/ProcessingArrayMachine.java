package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/7/23
 * @implNote ProcessingArrayMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingArrayMachine extends TieredWorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ProcessingArrayMachine.class, TieredWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableItemStackHandler machineStorage;
    //runtime
    @Nullable
    private GTRecipeType recipeTypeCache;

    public ProcessingArrayMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
        this.machineStorage = createMachineStorage(args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableItemStackHandler createMachineStorage(Object... args) {
        var storage = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.NONE, slots -> new ItemStackTransfer(1) {
            @Override
            public int getSlotLimit(int slot) {
                return getMachineLimit(getTier());
            }
        });
        storage.setFilter(this::isMachineStack);
        return storage;
    }

    protected boolean isMachineStack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
            var recipeType =  metaMachineItem.getDefinition().getRecipeType();
            return recipeType != null && recipeType != GTRecipeTypes.DUMMY_RECIPES;
        }
        return false;
    }

    @Nullable
    public MachineDefinition getMachineDefinition() {
        if (machineStorage.storage.getStackInSlot(0).getItem() instanceof MetaMachineItem metaMachineItem) {
            return metaMachineItem.getDefinition();
        }
        return null;
    }

    @Override
    @Nonnull
    public GTRecipeType getRecipeType() {
        if (recipeTypeCache == null) {
            var definition = getMachineDefinition();
            recipeTypeCache = definition == null ? null : definition.getRecipeType();
        }
        if (recipeTypeCache == null) {
            recipeTypeCache = GTRecipeTypes.DUMMY_RECIPES;
        }
        return recipeTypeCache;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            machineStorage.addChangedListener(this::onMachineChanged);
        }
    }

    protected void onMachineChanged() {
        if (isFormed) {
            if (getRecipeLogic().getLastRecipe() != null) {
                getRecipeLogic().markLastRecipeDirty();
            }
            getRecipeLogic().updateTickSubscription();
            scheduleRenderUpdate();
        }
    }

    //////////////////////////////////////
    //*******    Recipe Logic    *******//
    //////////////////////////////////////
    @Override
    public long getOverclockVoltage() {
        if (maxVoltage < 0)  {
            maxVoltage = 0L;
            var capabilities = capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP);
            if (capabilities != null) {
                for (IRecipeHandler<?> handler : capabilities) {
                    if (handler instanceof IEnergyContainer container) {
                        maxVoltage += container.getInputVoltage() * container.getInputAmperage();
                    }
                }
            } else {
                capabilities = capabilitiesProxy.get(IO.OUT, EURecipeCapability.CAP);
                if (capabilities != null) {
                    for (IRecipeHandler<?> handler : capabilities) {
                        if (handler instanceof IEnergyContainer container) {
                            maxVoltage += container.getOutputVoltage() * container.getOutputAmperage();
                        }
                    }
                }
            }
        }
        return maxVoltage;
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof ProcessingArrayMachine processingArray && processingArray.machineStorage.storage.getStackInSlot(0).getCount() > 0) {
            var limit = processingArray.machineStorage.storage.getStackInSlot(0).getCount();
            // apply parallel first
            recipe = GTRecipeModifiers.accurateParallel(machine, recipe, Math.min(limit, getMachineLimit(processingArray.getTier())), false).getA();
            // apply overclock later
            recipe = GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK).apply(machine, recipe);
            return recipe;
        }

        return null;
    }

    //////////////////////////////////////
    //********     Structure    ********//
    //////////////////////////////////////
    public static Block getCasingState(int tier) {
        if (tier <= GTValues.IV) {
            return GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
        } else {
            return GTBlocks.CASING_HSSE_STURDY.get();
        }
    }

    public static int getMachineLimit(Integer tier) {
        return tier <= GTValues.IV ? 16 : 64;
    }

}

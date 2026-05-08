package com.gregtechceu.gtceu.api.machine.trait;

import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProgrammableCircuitSlotTrait extends NotifiableRecipeHandlerTrait<Ingredient> implements IAttachConfiguratorsTrait, ICapabilityTrait {

    public static final MachineTraitType<ProgrammableCircuitSlotTrait> TYPE = new MachineTraitType<>(ProgrammableCircuitSlotTrait.class);

    @SaveField
    public final CustomItemStackHandler storage;

    @Getter
    @Setter
    private boolean enabled = true;

    public ProgrammableCircuitSlotTrait() {
        storage = new CustomItemStackHandler(1);
        storage.setFilter(IntCircuitBehaviour::isIntegratedCircuit);
    }

    @Override
    public MachineTraitType<?> getTraitType() {
        return TYPE;
    }

    // Returns the current circuit value
    public int getCurrentCircuit() {
        return IntCircuitBehaviour.getCircuitConfiguration(storage.getStackInSlot(0));
    }

    public void setCurrentCircuit(int circuit) {
        storage.setStackInSlot(0, IntCircuitBehaviour.stack(circuit));
    }

    @Override
    public void attachLeftConfigurators(Flow flow, ModularPanel<?> panel, PanelSyncManager syncManager) {
        if (!enabled) return;
        flow.child(GTMuiWidgets.createCircuitSlotPanel(this, panel, syncManager));
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        if (!controller.allowCircuitSlots()) setEnabled(false);
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        var allControllersAllowCircuits = true;
        for (var c : ((MultiblockPartMachine)getMachine()).getControllers()) {
            if (!c.allowCircuitSlots()) {
                allControllersAllowCircuits = false;
                break;
            }
        }
        setEnabled(allControllersAllowCircuits);
    }

    // Capability handler stuff

    @Override
    public IO getHandlerIO() {
        return enabled ? IO.IN : IO.NONE;
    }

    @Override
    public IO getCapabilityIO() {
        return IO.NONE;
    }

    @Override
    public @Nullable List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        if (!enabled) return left;
        return NotifiableItemStackHandler.handleRecipe(io, recipe, left, simulate, getHandlerIO(), storage);
    }

    @Override
    public List<Object> getContents() {
        return List.of(storage.getStackInSlot(0));
    }

    @Override
    public double getTotalContentAmount() {
        return 1;
    }

    @Override
    public RecipeCapability<Ingredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }
}

package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.neoforged.neoforge.common.crafting.SizedIngredient;

import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Machine trait which adds a programmable circuit input slot to the side of a machine.
 */
public class ProgrammableCircuitSlotTrait extends NotifiableRecipeHandlerTrait<SizedIngredient>
                                          implements IAttachConfiguratorsTrait, ICapabilityTrait {

    public static final MachineTraitType<ProgrammableCircuitSlotTrait> TYPE = new MachineTraitType<>(
            ProgrammableCircuitSlotTrait.class);

    @SaveField
    public final CustomItemStackHandler storage;

    @Getter
    @Setter
    private boolean enabled = true;
    @Getter
    private boolean controllerAllowsCircuits = true;

    public ProgrammableCircuitSlotTrait() {
        setEnabled(ConfigHolder.INSTANCE.machines.ghostCircuit);
        storage = new CustomItemStackHandler(1);
        storage.setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        storage.setOnContentsChanged(this::notifyListeners);
    }

    @Override
    public MachineTraitType<ProgrammableCircuitSlotTrait> getTraitType() {
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
        if (!controllerAllowsCircuits || !enabled || !ConfigHolder.INSTANCE.machines.ghostCircuit) return;
        flow.child(GTMuiWidgets.createCircuitSlotPanel(this, panel, syncManager));
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        if (!controller.allowCircuitSlots()) controllerAllowsCircuits = false;
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        var allControllersAllowCircuits = true;
        for (var c : ((MultiblockPartMachine) getMachine()).getControllers()) {
            if (!c.allowCircuitSlots()) {
                allControllersAllowCircuits = false;
                break;
            }
        }
        controllerAllowsCircuits = allControllersAllowCircuits;
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
    public List<SizedIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                   boolean simulate) {
        if (!controllerAllowsCircuits || !enabled || !ConfigHolder.INSTANCE.machines.ghostCircuit) return left;
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
    public RecipeCapability<SizedIngredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }
}

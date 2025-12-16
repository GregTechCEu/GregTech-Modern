package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Dummy machine BE used for wrapping {@link DummyRecipeLogicMachine}s
 */
public class DummyMachineBlockEntity implements IMachineBlockEntity {

    @Getter
    public final DummyRecipeLogicMachine metaMachine;
    @Getter
    private final MachineDefinition definition;
    @Getter
    @Setter
    private MachineRenderState renderState;

    // TODO: Fix the proxy parameter
    public DummyMachineBlockEntity(int tier, GTRecipeType type, Int2IntFunction tankScalingFunction,
                                   Collection<RecipeHandlerList> handlers,
                                   Object... args) {
        this.definition = new MachineDefinition(GTCEu.id("dummy"));
        this.definition.setRecipeTypes(new GTRecipeType[] { type });
        this.definition.setTier(tier);

        this.renderState = getDefinition().defaultRenderState();
        this.metaMachine = new DummyRecipeLogicMachine(this, tier, tankScalingFunction, handlers, args);
    }

    @Override
    public long getOffset() {
        return 0;
    }

    @Override
    public MultiManagedStorage getRootStorage() {
        return null;
    }

    @Override
    public CompoundTag getPersistentData() {
        return new CompoundTag();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }
}

package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.WrappedRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MEPatternBufferProxy extends TieredIOPartMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferProxy.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);
    private final WrappedRecipeHandlerTrait<Ingredient> itemInputHandler;
    private final WrappedRecipeHandlerTrait<Ingredient> itemOutputHandler;
    private final WrappedRecipeHandlerTrait<FluidIngredient> fluidInputHandler;
    private final WrappedRecipeHandlerTrait<FluidIngredient> fluidOutputHandler;
    private final WrappedRecipeHandlerTrait<Ingredient> shareItemHandler;
    private final WrappedRecipeHandlerTrait<FluidIngredient> shareFluidHandler;
    private final WrappedRecipeHandlerTrait<Ingredient> circuitHandler;

    @Persisted
    private BlockPos pos;

    public MEPatternBufferProxy(IMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.BOTH);
        this.itemInputHandler = new WrappedRecipeHandlerTrait<>(IO.IN, ItemRecipeCapability.CAP);
        this.itemOutputHandler = new WrappedRecipeHandlerTrait<>(IO.OUT, ItemRecipeCapability.CAP);
        this.fluidInputHandler = new WrappedRecipeHandlerTrait<>(IO.IN, FluidRecipeCapability.CAP);
        this.fluidOutputHandler = new WrappedRecipeHandlerTrait<>(IO.OUT, FluidRecipeCapability.CAP);
        this.shareFluidHandler = new WrappedRecipeHandlerTrait<>(IO.IN, FluidRecipeCapability.CAP);
        this.shareItemHandler = new WrappedRecipeHandlerTrait<>(IO.IN, ItemRecipeCapability.CAP);
        this.circuitHandler = new WrappedRecipeHandlerTrait<>(IO.IN, ItemRecipeCapability.CAP);
    }

    public boolean setIOBuffer(BlockPos pos) {
        if (pos == null) return false;
        if (MetaMachine.getMachine(getLevel(), pos) instanceof MEPatternBufferPartMachine) {
            this.pos = pos;
            itemInputHandler.setHandlerSupplier(() -> getIOBuffer().recipeHandler.getItemInputHandler());
            itemOutputHandler.setHandlerSupplier(
                    () -> getIOBuffer().recipeHandler.getItemOutputHandler());
            fluidInputHandler.setHandlerSupplier(
                    () -> getIOBuffer().recipeHandler.getFluidInputHandler());
            fluidOutputHandler.setHandlerSupplier(
                    () -> getIOBuffer().recipeHandler.getFluidOutputHandler());
            shareFluidHandler.setHandlerSupplier(() -> getIOBuffer().shareTank);
            shareItemHandler.setHandlerSupplier(() -> getIOBuffer().shareInventory);
            circuitHandler.setHandlerSupplier(() -> getIOBuffer().circuitInventory);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private MEPatternBufferPartMachine getIOBuffer() {
        if (pos == null) return null;
        if (MetaMachine.getMachine(getLevel(), pos) instanceof MEPatternBufferPartMachine buffer) {
            return buffer;
        } else {
            this.pos = null;
            return null;
        }
    }

    @Override
    public MetaMachine self() {
        var buffer = getIOBuffer();
        return buffer != null ? buffer.self() : super.self();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        var buffer = getIOBuffer();
        return buffer != null && super.shouldOpenUI(player, hand, hit);
    }

    @Override
    public @Nullable ModularUI createUI(Player entityPlayer) {
        GTCEu.LOGGER.warn("'createUI' of the Crafting Buffer Proxy was incorrectly called!");
        return null;
    }

    @Override
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return List.of(
                itemInputHandler,
                itemOutputHandler,
                fluidInputHandler,
                fluidOutputHandler,
                shareItemHandler,
                shareFluidHandler,
                circuitHandler);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

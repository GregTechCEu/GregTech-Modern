package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MEPatternBufferProxyRecipeHandler;
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
    private final MEPatternBufferProxyRecipeHandler<Ingredient> itemInputHandler;
    private final MEPatternBufferProxyRecipeHandler<Ingredient> itemOutputHandler;
    private final MEPatternBufferProxyRecipeHandler<FluidIngredient> fluidInputHandler;
    private final MEPatternBufferProxyRecipeHandler<FluidIngredient> fluidOutputHandler;
    private final MEPatternBufferProxyRecipeHandler<Ingredient> shareItemHandler;
    private final MEPatternBufferProxyRecipeHandler<FluidIngredient> shareFluidHandler;
    private final MEPatternBufferProxyRecipeHandler<Ingredient> circuitHandler;

    @Persisted
    private BlockPos pos;

    public MEPatternBufferProxy(IMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.BOTH);
        this.itemInputHandler = new MEPatternBufferProxyRecipeHandler<>(IO.IN, ItemRecipeCapability.CAP);
        this.itemOutputHandler = new MEPatternBufferProxyRecipeHandler<>(IO.OUT, ItemRecipeCapability.CAP);
        this.fluidInputHandler = new MEPatternBufferProxyRecipeHandler<>(IO.IN, FluidRecipeCapability.CAP);
        this.fluidOutputHandler = new MEPatternBufferProxyRecipeHandler<>(IO.OUT, FluidRecipeCapability.CAP);
        this.shareFluidHandler = new MEPatternBufferProxyRecipeHandler<>(IO.IN, FluidRecipeCapability.CAP);
        this.shareItemHandler = new MEPatternBufferProxyRecipeHandler<>(IO.IN, ItemRecipeCapability.CAP);
        this.circuitHandler = new MEPatternBufferProxyRecipeHandler<>(IO.IN, ItemRecipeCapability.CAP);
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
            circuitHandler.setHandlerSupplier(() -> getIOBuffer().circuitInventorySimulated);
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

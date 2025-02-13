package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.MEPatternBufferProxyRecipeHandler;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEPatternBufferProxyPartMachine extends TieredIOPartMachine
                                             implements IMachineLife, IDataStickInteractable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferProxyPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    protected MEPatternBufferProxyRecipeHandler<Ingredient> itemProxyHandler;

    @Getter
    protected MEPatternBufferProxyRecipeHandler<FluidIngredient> fluidProxyHandler;

    @Persisted
    @Getter
    @DescSynced
    private BlockPos bufferPos;

    public MEPatternBufferProxyPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.IN);
        this.itemProxyHandler = new MEPatternBufferProxyRecipeHandler<>(this, IO.IN, ItemRecipeCapability.CAP);
        this.fluidProxyHandler = new MEPatternBufferProxyRecipeHandler<>(this, IO.IN, FluidRecipeCapability.CAP);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel level) {
            level.getServer().tell(new TickTask(0, () -> this.setBuffer(bufferPos)));
        }
    }

    public boolean setBuffer(@Nullable BlockPos pos) {
        var level = getLevel();
        if (pos == null || level == null) return false;
        if (MetaMachine.getMachine(getLevel(), pos) instanceof MEPatternBufferPartMachine machine) {
            this.bufferPos = pos;

            List<NotifiableRecipeHandlerTrait<Ingredient>> itemHandlers = new ArrayList<>();
            List<NotifiableRecipeHandlerTrait<FluidIngredient>> fluidHandlers = new ArrayList<>();
            for (var handler : machine.getRecipeHandlers()) {
                if (handler.isProxy()) continue;

                if (handler.getCapability() == ItemRecipeCapability.CAP) {
                    itemHandlers.add((NotifiableRecipeHandlerTrait<Ingredient>) handler);
                } else {
                    fluidHandlers.add((NotifiableRecipeHandlerTrait<FluidIngredient>) handler);
                }
            }
            itemProxyHandler.setHandlers(itemHandlers);
            fluidProxyHandler.setHandlers(fluidHandlers);

            machine.addProxy(this);

            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private MEPatternBufferPartMachine getBuffer() {
        var level = getLevel();
        if (level == null || bufferPos == null) return null;
        if (MetaMachine.getMachine(level, bufferPos) instanceof MEPatternBufferPartMachine buffer) {
            return buffer;
        } else {
            this.bufferPos = null;
            return null;
        }
    }

    @Override
    public MetaMachine self() {
        var buffer = getBuffer();
        return buffer != null ? buffer.self() : super.self();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        var buffer = getBuffer();
        return buffer != null;
    }

    @Override
    public @Nullable ModularUI createUI(Player entityPlayer) {
        GTCEu.LOGGER.warn("'createUI' of the Crafting Buffer Proxy was incorrectly called!");
        return null;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachineRemoved() {
        var level = getLevel();
        if (level == null || bufferPos == null) return;
        if (MetaMachine.getMachine(getLevel(), this.bufferPos) instanceof MEPatternBufferPartMachine machine) {
            machine.removeProxy(this);
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        if (dataStick.hasTag()) {
            if (dataStick.getOrCreateTag().contains("pos", Tag.TAG_INT_ARRAY)) {
                var posArray = dataStick.getOrCreateTag().getIntArray("pos");
                var bufferPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
                setBuffer(bufferPos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}

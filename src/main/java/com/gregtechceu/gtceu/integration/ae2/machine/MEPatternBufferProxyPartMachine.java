package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.ProxySlotRecipeHandler;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEPatternBufferProxyPartMachine extends TieredIOPartMachine
                                             implements IMachineLife, IDataStickInteractable {

    @Getter
    private final ProxySlotRecipeHandler proxySlotRecipeHandler;

    @SaveField
    @Getter
    @SyncToClient
    private @Nullable BlockPos bufferPos;

    private @Nullable MEPatternBufferPartMachine buffer = null;
    private boolean bufferResolved = false;

    public MEPatternBufferProxyPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.IN);
        proxySlotRecipeHandler = new ProxySlotRecipeHandler(this, MEPatternBufferPartMachine.MAX_PATTERN_COUNT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel level) {
            level.getServer().tell(new TickTask(0, () -> this.setBuffer(bufferPos)));
        }
    }

    @Override
    public List<RecipeHandlerList> getRecipeHandlers() {
        return proxySlotRecipeHandler.getProxySlotHandlers();
    }

    public void setBuffer(@Nullable BlockPos pos) {
        bufferResolved = true;
        var level = getLevel();
        if (level == null || pos == null) {
            buffer = null;
        } else if (MetaMachine.getMachine(level, pos) instanceof MEPatternBufferPartMachine machine) {
            bufferPos = pos;
            buffer = machine;
            machine.addProxy(this);
            if (!isRemote()) proxySlotRecipeHandler.updateProxy(machine);
        } else {
            buffer = null;
        }
        syncDataHolder.markClientSyncFieldDirty("bufferPos");
    }

    @Nullable
    public MEPatternBufferPartMachine getBuffer() {
        if (!bufferResolved) setBuffer(bufferPos);
        return buffer;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return getBuffer() != null;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        assert getBuffer() != null; // UI should never be able to be opened when buffer is null
        return getBuffer().createUI(entityPlayer);
    }

    @Override
    public void onMachineRemoved() {
        var buf = getBuffer();
        if (buf != null) {
            buf.removeProxy(this);
            proxySlotRecipeHandler.clearProxy();
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        if (dataStick.hasTag()) {
            assert dataStick.getTag() != null;
            if (dataStick.getTag().contains("pos", Tag.TAG_INT_ARRAY)) {
                var posArray = dataStick.getOrCreateTag().getIntArray("pos");
                var bufferPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
                setBuffer(bufferPos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}

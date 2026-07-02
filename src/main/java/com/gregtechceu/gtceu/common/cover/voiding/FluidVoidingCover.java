package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidVoidingCover extends PumpCover {

    public FluidVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, 0);
        setWorkingEnabled(false);
    }

    @Override
    protected boolean isSubscriptionActive() {
        return isWorkingEnabled();
    }

    //////////////////////////////////////////////
    // *********** COVER LOGIC ***********//
    //////////////////////////////////////////////

    @Override
    protected void update() {
        if (coverHolder.getOffsetTimer() % 5 != 0)
            return;

        doVoidFluids();
        subscriptionHandler.updateSubscription();
    }

    protected void doVoidFluids() {
        IFluidHandlerModifiable fluidHandler = getOwnFluidHandler();
        if (fluidHandler == null) {
            return;
        }
        voidAny(fluidHandler);
    }

    void voidAny(IFluidHandlerModifiable fluidHandler) {
        Object2LongMap<FluidStack> fluidAmounts = enumerateDistinctFluids(fluidHandler, TransferDirection.EXTRACT);

        for (var entry : Object2LongMaps.fastIterable(fluidAmounts)) {
            var stack = entry.getKey();
            if (!filterHandler.test(stack)) continue;

            for (int op : GTMath.split(entry.getLongValue())) {
                var toDrain = new FluidStack(stack, op);
                fluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        var row = coverUIRow()
                .child(GTMuiWidgets.createPowerButton(this));
        GTMuiWidgets.createFilterRow(row, filterHandler, data, syncManager, settings);

        column.child(row);
    }

    @Override
    public InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            setWorkingEnabled(!isWorkingEnabled);
            context.getPlayer().sendSystemMessage(Component.translatable(isWorkingEnabled() ?
                    "cover.voiding.message.enabled" : "cover.voiding.message.disabled"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    public @Nullable UITexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                        Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return isWorkingEnabled() ? GTGuiTextures.TOOL_START : GTGuiTextures.TOOL_PAUSE;
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }
}

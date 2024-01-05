package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlungerBehavior implements IToolBehavior {

    public static final PlungerBehavior INSTANCE = PlungerBehavior.create();

    protected PlungerBehavior() {/**/}

    @ExpectPlatform
    protected static PlungerBehavior create() {
        throw new AssertionError();
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        IFluidTransfer fluidHandler = FluidTransferHelper.getFluidTransfer(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        IFluidTransfer handlerToRemoveFrom = fluidHandler;
//                player.isCrouching() ?
//                (fluidHandler instanceof IOFluidTransferList ? ((IOFluidTransferList) fluidHandler).input : null) :
//                (fluidHandler instanceof IOFluidTransferList ? ((IOFluidTransferList) fluidHandler).output : fluidHandler);

        if (handlerToRemoveFrom != null && handlerToRemoveFrom.drain(FluidHelper.getBucket(), true) != null) {
            ToolHelper.onActionDone(context.getPlayer(), context.getLevel(), context.getHand());
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.plunger"));
    }
}
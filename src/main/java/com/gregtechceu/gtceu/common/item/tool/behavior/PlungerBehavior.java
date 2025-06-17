package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.misc.forge.VoidFluidHandlerItemStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlungerBehavior implements IToolBehavior, IComponentCapability, IInteractionItem {

    public static final PlungerBehavior INSTANCE = PlungerBehavior.create();

    protected PlungerBehavior() {/**/}

    protected static PlungerBehavior create() {
        return new PlungerBehavior();
    }

    @Override
    public boolean shouldOpenUIAfterUse(UseOnContext context) {
        return !(context.getPlayer() != null && context.getPlayer().isShiftKeyDown());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null && !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        IFluidHandler fluidHandler;
        if (level.getBlockEntity(context.getClickedPos()) instanceof IMachineBlockEntity mmbe) {
            fluidHandler = mmbe.getMetaMachine().getFluidHandlerCap(context.getClickedFace(), false);
        } else {
            // noinspection DataFlowIssue
            fluidHandler = FluidUtil.getFluidHandler(level, context.getClickedPos(), context.getClickedFace())
                    .orElse(null);
        }
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        FluidStack drained = fluidHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty()) {
            fluidHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
            ToolHelper.onActionDone(player, stack, level, context.getClickLocation());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.plunger"));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap,
                    LazyOptional.of(() -> new VoidFluidHandlerItemStack(itemStack) {

                        @Override
                        public int fill(FluidStack resource, FluidAction doFill) {
                            int result = super.fill(resource, doFill);
                            if (result > 0) {
                                ToolHelper.damageItem(getContainer(), null);
                            }
                            return result;
                        }
                    }));
        }
        return LazyOptional.empty();
    }
}

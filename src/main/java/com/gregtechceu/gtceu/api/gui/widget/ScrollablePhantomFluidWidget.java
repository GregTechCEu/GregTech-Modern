package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.widget.PhantomFluidWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScrollablePhantomFluidWidget extends PhantomFluidWidget {

    private static final int SCROLL_ACTION_ID = 0x0001_0001;
    private static final int MILLIBUCKETS = FluidHelper.getBucket() / 1000;

    public ScrollablePhantomFluidWidget(@Nullable IFluidHandlerModifiable fluidTank, int tank, int x, int y, int width,
                                        int height, Supplier<FluidStack> phantomFluidGetter,
                                        Consumer<FluidStack> phantomFluidSetter) {
        super(fluidTank, tank, x, y, width, height, phantomFluidGetter, phantomFluidSetter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOverElement(mouseX, mouseY))
            return false;

        int delta = getModifiedChangeAmount((scrollY > 0) ? 1 : -1) * MILLIBUCKETS;
        writeClientAction(SCROLL_ACTION_ID, buf -> buf.writeVarInt(delta));

        return true;
    }

    private int getModifiedChangeAmount(int amount) {
        if (GTUtil.isShiftDown())
            amount *= 10;

        if (GTUtil.isCtrlDown())
            amount *= 100;

        if (!GTUtil.isAltDown())
            amount *= 1000;

        return amount;
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        switch (id) {
            case SCROLL_ACTION_ID -> handleScrollAction(buffer.readVarInt());
            default -> super.handleClientAction(id, buffer);
        }

        detectAndSendChanges();
    }

    private void handleScrollAction(int delta) {
        IFluidHandlerModifiable fluidTank = (IFluidHandlerModifiable) getFluidTank();
        if (fluidTank == null)
            return;

        FluidStack fluid = tank.getFluidInTank(0);
        if (fluid.isEmpty())
            return;

        fluid.setAmount(Math.min(Math.max(fluid.getAmount() + delta, 0), tank.getTankCapacity(0)));
        if (fluid.getAmount() <= 0L) {
            tank.drain(fluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }
    }
}

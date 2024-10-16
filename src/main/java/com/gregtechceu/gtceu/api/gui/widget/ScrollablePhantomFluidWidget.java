package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScrollablePhantomFluidWidget extends PhantomFluidWidget {

    private static final int SCROLL_ACTION_ID = 0x0001_0001;

    public ScrollablePhantomFluidWidget(@Nullable IFluidHandlerModifiable fluidTank, int tank, int x, int y, int width,
                                        int height, Supplier<FluidStack> phantomFluidGetter,
                                        Consumer<FluidStack> phantomFluidSetter) {
        super(fluidTank, tank, x, y, width, height, phantomFluidGetter, phantomFluidSetter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY))
            return false;

        var delta = getModifiedChangeAmount((wheelDelta > 0) ? 1 : -1);
        writeClientAction(SCROLL_ACTION_ID, buf -> buf.writeInt(delta));

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
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        switch (id) {
            case SCROLL_ACTION_ID -> handleScrollAction(buffer.readInt());
            default -> super.handleClientAction(id, buffer);
        }

        detectAndSendChanges();
    }

    private void handleScrollAction(int delta) {
        IFluidHandlerModifiable fluidTank = (IFluidHandlerModifiable) getFluidTank();
        if (fluidTank == null)
            return;

        FluidStack fluid = fluidTank.getFluidInTank(tank);
        if (fluid.isEmpty())
            return;

        if (fluid.isEmpty())
            return;

        fluid.setAmount(Math.min(Math.max(fluid.getAmount() + delta, 0), fluidTank.getTankCapacity(tank)));
        if (fluid.getAmount() <= 0L) {
            fluidTank.setFluidInTank(tank, FluidStack.EMPTY);
        }
    }
}
